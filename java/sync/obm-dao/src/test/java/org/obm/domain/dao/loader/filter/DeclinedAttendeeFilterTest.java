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
package org.obm.domain.dao.loader.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.util.Map;

import org.junit.Test;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.UserAttendee;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DeclinedAttendeeFilterTest {

	private Attendee kimPhilby(Participation participation) {
		return UserAttendee.builder().email("kim.philby@mi6.gov.uk").participation(participation).build();
	}

	private Attendee guyBurgess(Participation participation) {
		return UserAttendee.builder().email("guy.burgess@mi6.gov.uk").participation(participation).build();
	}

	@Test
	public void testNonRecurrentEvent() {
		Attendee acceptingPhilby = kimPhilby(Participation.accepted());
		Attendee decliningPhilby = kimPhilby(Participation.declined());
		Attendee burgess = guyBurgess(Participation.accepted());

		EventObmId evWithPhilbyId = new EventObmId(1);
		Event evWithPhilby = new Event();
		evWithPhilby.setUid(evWithPhilbyId);
		evWithPhilby.addAttendee(acceptingPhilby);
		evWithPhilby.addAttendee(burgess);

		EventObmId evWithoutPhilbyId = new EventObmId(2);
		Event evWithoutPhilby = new Event();
		evWithoutPhilby.setUid(evWithoutPhilbyId);
		evWithoutPhilby.addAttendee(decliningPhilby);
		evWithPhilby.addAttendee(burgess);

		Map<EventObmId, Event> expectedEvents = new ImmutableMap.Builder<EventObmId, Event>().put(
				evWithPhilbyId, evWithPhilby).build();
		Map<EventObmId, Event> unfilteredEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evWithoutPhilbyId, evWithoutPhilby).build();

		EventFilter filter = new DeclinedAttendeeFilter(acceptingPhilby);
		assertThat(filter.filter(unfilteredEvents)).isEqualTo(expectedEvents);
	}

	@Test
	public void testNoDeclinedEvent() {
		Attendee acceptingPhilby = kimPhilby(Participation.accepted());
		Attendee burgess = guyBurgess(Participation.accepted());

		EventObmId evWithPhilby1Id = new EventObmId(1);
		Event evWithPhilby1 = new Event();
		evWithPhilby1.setUid(evWithPhilby1Id);
		evWithPhilby1.addAttendee(acceptingPhilby);
		evWithPhilby1.addAttendee(burgess);

		EventObmId evWithPhilbyId2 = new EventObmId(2);
		Event evWithPhilby2 = new Event();
		evWithPhilby2.setUid(evWithPhilbyId2);
		evWithPhilby2.addAttendee(acceptingPhilby);
		evWithPhilby2.addAttendee(burgess);

		Map<EventObmId, Event> events = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilby1Id, evWithPhilby1).put(evWithPhilbyId2, evWithPhilby2).build();

		EventFilter filter = new DeclinedAttendeeFilter(acceptingPhilby);
		assertThat(filter.filter(events)).isEqualTo(events);
	}

	@Test
	public void testRecurrentEvent() {

		Attendee acceptingPhilby = kimPhilby(Participation.accepted());
		Attendee decliningPhilby = kimPhilby(Participation.declined());
		Attendee burgess = guyBurgess(Participation.accepted());

		EventObmId evWithPhilbyId = new EventObmId(1);
		Event evWithPhilby = new Event();
		evWithPhilby.setUid(evWithPhilbyId);
		evWithPhilby.addAttendee(acceptingPhilby);
		evWithPhilby.addAttendee(burgess);

		EventObmId evWithSomePhilbyId = new EventObmId(2);
		Event evWithSomePhilby = new Event();
		evWithSomePhilby.setUid(evWithSomePhilbyId);
		evWithSomePhilby.addAttendee(acceptingPhilby);
		evWithSomePhilby.addAttendee(burgess);
		evWithSomePhilby.getRecurrence().setKind(RecurrenceKind.weekly);

		EventObmId evExWithoutPhilbyId = new EventObmId(3);
		Event evExWithoutPhilby = new Event();
		evExWithoutPhilby.setUid(evExWithoutPhilbyId);
		evExWithoutPhilby.addAttendee(decliningPhilby);
		evExWithoutPhilby.addAttendee(burgess);
		evExWithoutPhilby.setRecurrenceId(date("2013-03-20T12:00:00"));

		EventObmId evExWithPhilbyId = new EventObmId(4);
		Event evExWithPhilby = new Event();
		evExWithPhilby.setUid(evExWithPhilbyId);
		evExWithPhilby.addAttendee(acceptingPhilby);
		evExWithPhilby.addAttendee(burgess);
		evExWithPhilby.setRecurrenceId(date("2013-03-13T12:00:00"));

		evWithSomePhilby.getRecurrence().setEventExceptions(
				Sets.newHashSet(evExWithoutPhilby, evExWithPhilby));

		Event cloneOfEvWithSomePhilby = evWithSomePhilby.clone();
		cloneOfEvWithSomePhilby.getRecurrence().setEventExceptions(
				Sets.newHashSet(evExWithPhilby));
		cloneOfEvWithSomePhilby.getRecurrence().setExceptions(
				Lists.newArrayList(evExWithoutPhilby.getRecurrenceId()));

		Map<EventObmId, Event> expectedEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evWithSomePhilbyId, cloneOfEvWithSomePhilby)
				.build();
		Map<EventObmId, Event> unfilteredEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evWithSomePhilbyId, evWithSomePhilby)
				.build();

		EventFilter filter = new DeclinedAttendeeFilter(acceptingPhilby);
		assertThat(filter.filter(unfilteredEvents)).isEqualTo(expectedEvents);
	}

	@Test
	public void testDeclinedRecurrentEvent() {

		Attendee acceptingPhilby = kimPhilby(Participation.accepted());
		Attendee decliningPhilby = kimPhilby(Participation.declined());
		Attendee burgess = guyBurgess(Participation.accepted());

		EventObmId evWithPhilbyId = new EventObmId(1);
		Event evWithPhilby = new Event();
		evWithPhilby.setUid(evWithPhilbyId);
		evWithPhilby.addAttendee(acceptingPhilby);
		evWithPhilby.addAttendee(burgess);

		EventObmId evWithSomePhilbyId = new EventObmId(2);
		Event evWithSomePhilby = new Event();
		evWithSomePhilby.setUid(evWithSomePhilbyId);
		evWithSomePhilby.addAttendee(decliningPhilby);
		evWithSomePhilby.addAttendee(burgess);
		evWithSomePhilby.getRecurrence().setKind(RecurrenceKind.weekly);

		EventObmId evExWithoutPhilbyId = new EventObmId(3);
		Event evExWithoutPhilby = new Event();
		evExWithoutPhilby.setUid(evExWithoutPhilbyId);
		evExWithoutPhilby.addAttendee(decliningPhilby);
		evExWithoutPhilby.addAttendee(burgess);
		evExWithoutPhilby.setRecurrenceId(date("2013-03-13T12:00:00"));

		EventObmId evExWithPhilbyId = new EventObmId(4);
		Event evExWithPhilby = new Event();
		evExWithPhilby.setUid(evExWithPhilbyId);
		evExWithPhilby.addAttendee(acceptingPhilby);
		evExWithPhilby.addAttendee(burgess);
		evExWithPhilby.setRecurrenceId(date("2013-03-20T12:00:00"));

		evWithSomePhilby.getRecurrence().setEventExceptions(
				Sets.newHashSet(evExWithoutPhilby, evExWithPhilby));

		Map<EventObmId, Event> expectedEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evExWithPhilbyId, evExWithPhilby).build();
		Map<EventObmId, Event> unfilteredEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evWithSomePhilbyId, evWithSomePhilby)
				.build();

		EventFilter filter = new DeclinedAttendeeFilter(acceptingPhilby);
		assertThat(filter.filter(unfilteredEvents)).isEqualTo(expectedEvents);
	}
}
