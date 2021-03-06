/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2016 Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.service.calendar;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.obm.domain.dao.CalendarDao;
import org.obm.domain.dao.ResourceDao;
import org.obm.icalendar.ICalendarFactory;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.FindException;
import org.obm.service.user.UserService;
import org.obm.sync.Right;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.services.AttendeeService;
import org.obm.sync.services.ImportICalendarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.resource.Resource;
import fr.aliacom.obm.common.user.ObmUser;
import net.fortuna.ical4j.data.ParserException;

@Singleton
public class CalendarService {

	private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);

	private static final Set<Right> ACCESS_READ_RIGHTS = ImmutableSet.of(Right.ACCESS, Right.READ);
	
	private final CalendarDao calendarDao;
	private final ResourceDao resourceDao;
	private final Ical4jHelper ical4jHelper;
	private final UserService userService;
	private final AttendeeService attendeeService;
	private final ICalendarFactory calendarFactory;

	@Inject
	public CalendarService(CalendarDao calendarDao, ResourceDao resourceDao, 
			Ical4jHelper ical4jHelper, 
			UserService userService, AttendeeService attendeeService,
			ICalendarFactory calendarFactory) {
		this.calendarDao = calendarDao;
		this.resourceDao = resourceDao;
		this.ical4jHelper = ical4jHelper;
		this.userService = userService;
		this.attendeeService = attendeeService;
		this.calendarFactory = calendarFactory;
	}
	
	public int importICalendar(AccessToken token, String calendar, String ics) 
			throws ImportICalendarException, ServerFault {

		
		ObmUser calendarUser = null;
		
		try {
			calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		}
		catch (Exception e) {
			throw new ServerFault(e);
		}
		
		int countEvent = 0;
		List<Event> events = parseICSEvent(token, ics, calendarUser.getUid());
		LoadingCache<Attendee, Optional<ObmUser>> cache = newObmUserCache(token.getDomain().getName());

		logger.info(events.size() + " event(s) found in the ICS"); 
		
		for (Event event: events) {

			removeAttendeeWithNoEmail(event);
			if (!isAttendeeExistForCalendarOwner(calendarUser, event.getAttendees(), cache)) {
				addAttendeeForCalendarOwner(token, calendar, event);
			}
			
			if(event.isEventInThePast()){
				logger.info("event {} is in the past", event.getExtId().getExtId());
				changeCalendarOwnerAttendeeParticipationToAccepted(calendarUser, event, cache);
			}

			Optional<Event> alreadyInDbEvent = findEventByExtId(token, calendarUser, event);
			if (event.isAnException()) {
				if (importException(token, calendarUser, alreadyInDbEvent, event)) {
					countEvent += 1;
				}
			} else if (createEventIfNotExists(token, calendarUser, event, alreadyInDbEvent)) {
				countEvent += 1;
			} else if (updateEventIfExistsWithLowerSequence(token, calendarUser, event, alreadyInDbEvent)) {
				countEvent += 1;
			}
		}
		return countEvent;
	}

	private boolean importException(AccessToken token, ObmUser calendarUser, Optional<Event> parentEvent, Event event) throws ImportICalendarException {
		if (!parentEvent.isPresent()) {
			throw new ImportICalendarException("Trying to import a event exception but the parent can't be found: " + event.getExtId().getExtId());
		}
		
		return addExceptionToParent(token, calendarUser, parentEvent.get(), event);
	}
	
	private boolean addExceptionToParent(AccessToken token, ObmUser calendarUser, Event parentEvent, Event eventException) throws ImportICalendarException {
		try {
			Event alreadyInDbException = parentEvent.getRecurrence().getEventExceptionWithRecurrenceId(eventException.getRecurrenceId());
			if (alreadyInDbException == null || alreadyInDbException.getSequence() < eventException.getSequence()) {
				parentEvent.getRecurrence().addOrReplaceEventException(eventException);
				calendarDao.modifyEvent(token, calendarUser.getLogin(), parentEvent, true);
				return true;
			}
			logger.warn("event exception {} seems to already exist with sequence {}, newly imported exception sequence is {}, skipping update",
					eventException.getExtId().getExtId(), alreadyInDbException.getSequence(), eventException.getSequence());
			return false;
		} catch (FindException | SQLException | EventNotFoundException | ServerFault e) {
			throw new ImportICalendarException(e);
		}
	}

	private LoadingCache<Attendee, Optional<ObmUser>> newObmUserCache(final String domainName) {
		return CacheBuilder.newBuilder().build(new CacheLoader<Attendee, Optional<ObmUser>>() {
			@Override
			public Optional<ObmUser> load(Attendee key) throws Exception {
				return Optional.fromNullable(userService.getUserFromAttendee(key, domainName));
			}
		});
	}

	private void changeCalendarOwnerAttendeeParticipationToAccepted(ObmUser calendarUser, Event event, LoadingCache<Attendee, Optional<ObmUser>> cache) {
		for (Attendee attendee: event.getAttendees()) {
			if (isAttendeeExistForCalendarOwner(calendarUser, attendee, cache)) {
				attendee.setParticipation(Participation.accepted());
			}	
		}
	}

	private void removeAttendeeWithNoEmail(Event event) { 
		List<Attendee> newAttendees = new ArrayList<Attendee>();
		for (Attendee attendee: event.getAttendees()) {
			if (attendee.getEmail() != null) {
				newAttendees.add(attendee);
			}
		}
		event.setAttendees(newAttendees);
	}

	private boolean createEventIfNotExists(AccessToken token, ObmUser calendarUser, Event event, Optional<Event> alreadyInDbEvent)
			throws ImportICalendarException {
		try {
			if (alreadyInDbEvent.isPresent()) {
				logger.warn("event {} seems to already exist, skipping creation", event.getExtId().getExtId());
			} else {
				Event newEvent = calendarDao.createEvent(token, calendarUser.getLogin(), event);
				if (newEvent != null) {
					return true;
				}
			}
		} catch (FindException | SQLException | ServerFault e) {
			throw new ImportICalendarException(e);
		}
		return false;
	}

	public boolean isEventExists(AccessToken token, String calendar, Event event) throws FindException {
		ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());

		return findEventByExtId(token, calendarUser, event).isPresent();
	}

	public Optional<Event> findEventByExtId(AccessToken token, ObmUser calendarUser, Event event) {
		if (event.getExtId() != null && event.getExtId().getExtId() != null) {
			return Optional.fromNullable(calendarDao.findEventByExtId(token, calendarUser, event.getExtId()));
		}
	
		return Optional.absent();
	}

	private boolean updateEventIfExistsWithLowerSequence(AccessToken token, ObmUser calendarUser, Event event, Optional<Event> alreadyInDbEvent)
			throws ImportICalendarException {
		try {
			if (alreadyInDbEvent.isPresent() && alreadyInDbEvent.get().getSequence() < event.getSequence()) {
				event.setUid(alreadyInDbEvent.get().getUid()); // Parsed events do not have their internal OBM id
				return calendarDao.modifyEvent(token, calendarUser.getLogin(), event, true) != null;
			}
			if (alreadyInDbEvent.isPresent()) {
				logger.warn("event {} seems to already exist with sequence {}, newly imported event sequence is {}, skipping update",
						event.getExtId().getExtId(), alreadyInDbEvent.get().getSequence(), event.getSequence());
			}
		} catch (EventNotFoundException e) {
			logger.error("A previous version of the event seems to exist but is not findable anymore", e);
		} catch (SQLException|ServerFault|FindException e) {
			throw new ImportICalendarException(e);
		}
		return false;
	}

	private List<Event> parseICSEvent(AccessToken token, String icsToString, Integer ownerId) throws ImportICalendarException {
		try {
			return ical4jHelper.parseICS(icsToString, createIcal4jUserFrom(token), ownerId);
		} catch (IOException e) {
			throw new ImportICalendarException(e);
		} catch (ParserException e) {
			throw new ImportICalendarException(e);
		}
	}
	
	private void addAttendeeForCalendarOwner(AccessToken token, String calendar, Event event) {
		Attendee attendee = attendeeService.findUserAttendee(null, calendar, token.getDomain());
			
		if (Iterables.isEmpty(event.getAttendees())) {
			attendee = UserAttendee
					.builder()
					.asOrganizer()
					.canWriteOnCalendar(attendee.isCanWriteOnCalendar())
					.displayName(attendee.getDisplayName())
					.email(attendee.getEmail())
					.entityId(attendee.getEntityId())
					.participation(Participation.accepted())
					.participationRole(attendee.getParticipationRole())
					.percent(attendee.getPercent())
					.build();

			// Because we're handling the case where noone attends the event yet
			// So we're adding the calendar owner as the organizer and as such, he's the owner of the event
			event.setOwnerEmail(attendee.getEmail());
		}

		event.getAttendees().add(attendee);
	}

	private boolean isAttendeeExistForCalendarOwner(ObmUser calendarUser, List<Attendee> attendees, LoadingCache<Attendee, Optional<ObmUser>> cache) {
		for (Attendee attendee: attendees) {
			if (isAttendeeExistForCalendarOwner(calendarUser, attendee, cache)) {
				return true;
			}
		}

		return false;
	}
	
	private boolean isAttendeeExistForCalendarOwner(ObmUser calendarUser, Attendee attendee, LoadingCache<Attendee, Optional<ObmUser>> cache) {
		ObmUser obmUser = cache.getUnchecked(attendee).orNull();

		if (obmUser != null) {
			if (obmUser.getLogin().equals(calendarUser.getLogin())) {
				return true;
			}
		}

		return false;
	}
	
	public Ical4jUser createIcal4jUserFrom(AccessToken accessToken) {
		ObmUser user = userService.getUserFromAccessToken(accessToken);
		return calendarFactory.createIcal4jUserFromObmUser(user);
	}

	public Set<Resource> parseResources(String ics) throws IOException, ParserException {
		return ical4jHelper.parseResources(ics);
	}

	public void assertDomainResourcesExist(AccessToken token, Set<Resource> resources) throws DaoException {
		for (Resource resource : filterUnexistingAndDomainOnlyResources(token, resources)) {
			resourceDao.createWithPublicRights(token, resource, ACCESS_READ_RIGHTS);
		}
	}

	private ImmutableList<Resource> filterUnexistingAndDomainOnlyResources(final AccessToken token, Set<Resource> resources) {
		return FluentIterable
			.from(resources)
			.filter(new Predicate<Resource>() {
	
				@Override
				public boolean apply(Resource res) {
					return res.getEmail().endsWith(token.getDomain().getName());
				}
				
			}).filter(new Predicate<Resource>() {

				@Override
				public boolean apply(Resource res) {
					return attendeeService.findResourceAttendee(res.getName(), res.getEmail(), token.getDomain(), token.getObmId()) == null;
				}
				
			}).toList();
	}
}
