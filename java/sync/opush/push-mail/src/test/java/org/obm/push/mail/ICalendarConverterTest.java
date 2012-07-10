/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

import junit.framework.Assert;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VEvent;

import org.fest.assertions.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.icalendar.ICalendar;
import org.obm.icalendar.ical4jwrapper.ICalendarEvent;
import org.obm.push.bean.MSEventExtId;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestInstanceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestIntDBusyStatus;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceDayOfWeek;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceType;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


public class ICalendarConverterTest {

	private ICalendarConverter icalendarConverter;

	@Before
	public void before() {
		icalendarConverter = new ICalendarConverter();
	}
	
	@Test
	public void testICalendarConverterSingleFreeEvent() throws IOException, ParserException {
		ICalendar icalendar = icalendar("single_event_free.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T12:26:16Z").toDate())
					.endTime(new DateTime("2012-04-24T07:30:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.SINGLE)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.intDBusyStatus(MSMeetingRequestIntDBusyStatus.FREE)
					.build());
	}
	
	@Test
	public void testICalendarConverterSingleBusyEvent() throws IOException, ParserException {
		ICalendar icalendar = icalendar("single_event_busy.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T12:26:16Z").toDate())
					.endTime(new DateTime("2012-04-24T07:30:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.SINGLE)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.intDBusyStatus(MSMeetingRequestIntDBusyStatus.BUSY)
					.build());
	}
	
	@Test
	public void testICalendarConverterSingleBusyOOFEvent() throws IOException, ParserException {
		ICalendar icalendar = icalendar("single_event_busy-oof.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T12:26:16Z").toDate())
					.endTime(new DateTime("2012-04-24T07:30:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.SINGLE)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.intDBusyStatus(MSMeetingRequestIntDBusyStatus.OUT_OF_OFFICE)
					.build());
	}
	
	@Test
	public void testICalendarConverterSingleBusyTentativeEvent() throws IOException, ParserException {
		ICalendar icalendar = icalendar("single_event_busy-tentative.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T12:26:16Z").toDate())
					.endTime(new DateTime("2012-04-24T07:30:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.SINGLE)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.intDBusyStatus(MSMeetingRequestIntDBusyStatus.TENTATIVE)
					.build());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testICalendarConverterSingleEventBadOrganizerFormat() throws IOException, ParserException {
		ICalendar icalendar = icalendar("single_event_organizer-bad-format.zimbra.ics");
		icalendarConverter.convertToMSMeetingRequest(icalendar);
	}
	
	@Test
	public void testICalendarConverterSingleEventWithAlarm() throws IOException, ParserException {
		ICalendar icalendar = icalendar("single_event_valarm.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T07:57:33Z").toDate())
					.endTime(new DateTime("2012-04-24T07:30:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.SINGLE)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reminder(600l)
					.reponseRequested(true)
					.intDBusyStatus(MSMeetingRequestIntDBusyStatus.FREE)
					.build());
	}
	
	@Test
	public void testICalendarConverterSingleEventWithAlarmAndAllDay() throws IOException, ParserException {
		ICalendar icalendar = icalendar("single_event_valarm_allDay.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T02:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T09:44:07Z").toDate())
					.endTime(new DateTime("2012-04-25T02:00:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.SINGLE)
					.msEventExtId(new MSEventExtId("3c428e8d-efee-413a-8a5b-d340feb21187"))
					.reminder(600l)
					.reponseRequested(true)
					.intDBusyStatus(MSMeetingRequestIntDBusyStatus.BUSY)
					.allDayEvent(true)
					.build());
	}
	
	@Test
	public void testICalendarConverterDailyEventWithInterval() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-daily_interval.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T13:04:28Z").toDate())
					.endTime(new DateTime("2012-04-24T07:30:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2012-04-24T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(2)
							.type(MSMeetingRequestRecurrenceType.DAILY)
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterDailyEventWithUntil() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-daily_interval_until.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T13:32:54Z").toDate())
					.endTime(new DateTime("2012-04-24T07:30:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2012-04-24T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(1)
							.until(new DateTime("2012-04-24T23:59:59").toDate())
							.type(MSMeetingRequestRecurrenceType.DAILY)
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterDailyEventWithIntervalAndWorkingDays() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-daily_interval_workingdays.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T15:04:23Z").toDate())
					.endTime(new DateTime("2012-04-24T07:15:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2012-04-24T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(1)
							.type(MSMeetingRequestRecurrenceType.DAILY)
							.dayOfWeek(Lists.newArrayList(
									MSMeetingRequestRecurrenceDayOfWeek.MONDAY,
									MSMeetingRequestRecurrenceDayOfWeek.TUESDAY,
									MSMeetingRequestRecurrenceDayOfWeek.WEDNESDAY,
									MSMeetingRequestRecurrenceDayOfWeek.THURSDAY,
									MSMeetingRequestRecurrenceDayOfWeek.FRIDAY))
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterDailyEventWithIntervalAndWorkingDaysAndCountOccurences() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-daily_interval_workingdays_countoccurences.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T15:18:17Z").toDate())
					.endTime(new DateTime("2012-04-24T07:15:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2012-04-24T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(1)
							.occurrences(15)
							.type(MSMeetingRequestRecurrenceType.DAILY)
							.dayOfWeek(Lists.newArrayList(
									MSMeetingRequestRecurrenceDayOfWeek.MONDAY,
									MSMeetingRequestRecurrenceDayOfWeek.TUESDAY,
									MSMeetingRequestRecurrenceDayOfWeek.WEDNESDAY,
									MSMeetingRequestRecurrenceDayOfWeek.THURSDAY,
									MSMeetingRequestRecurrenceDayOfWeek.FRIDAY))
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterWeeklyEventWithByDay() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-weekly_interval_byday.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T15:25:04Z").toDate())
					.endTime(new DateTime("2012-04-24T07:15:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2012-04-24T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(1)
							.type(MSMeetingRequestRecurrenceType.WEEKLY)
							.dayOfWeek(Lists.newArrayList(
									MSMeetingRequestRecurrenceDayOfWeek.TUESDAY,
									MSMeetingRequestRecurrenceDayOfWeek.THURSDAY))
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterMonthlyEventWithByMonthDay() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-monthly_interval_bymonthday.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-04-24T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T15:28:46Z").toDate())
					.endTime(new DateTime("2012-04-24T07:15:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2012-04-24T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(1)
							.type(MSMeetingRequestRecurrenceType.MONTHLY)
							.dayOfMonth(24)
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterMonthlyEventWithByDayAndBySetPos() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-monthly_interval_byday_bysetpos.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-05-12T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-23T15:51:05Z").toDate())
					.endTime(new DateTime("2012-05-12T07:15:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2012-05-12T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(1)
							.type(MSMeetingRequestRecurrenceType.MONTHLY_NTH_DAY)
							.weekOfMonth(2)
							.dayOfWeek(Lists.newArrayList(
									MSMeetingRequestRecurrenceDayOfWeek.SUNDAY,
									MSMeetingRequestRecurrenceDayOfWeek.SATURDAY))
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterMonthlyEventWithByDayNumber() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-weekly_interval_byday-1.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-07-10T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-24T07:57:07Z").toDate())
					.endTime(new DateTime("2012-07-10T07:15:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2012-07-10T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(1)
							.type(MSMeetingRequestRecurrenceType.MONTHLY_NTH_DAY)
							.dayOfMonth(2)
							.dayOfWeek(Lists.newArrayList(
									MSMeetingRequestRecurrenceDayOfWeek.TUESDAY))
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterYearlyEventByMonthDayAndByMonth() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-yearly_interval_bymonthday_bymonth.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-07-10T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-24T08:08:09Z").toDate())
					.endTime(new DateTime("2012-07-10T07:15:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2012-07-10T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(1)
							.type(MSMeetingRequestRecurrenceType.YEARLY)
							.dayOfMonth(10)
							.monthOfYear(7)
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterYearlyEventWithByMonthAndByDayAndBySetPos() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-yearly_interval_bymonthday_byday_byset.zimbra.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2013-10-01T07:00:00").toDate())
					.dtStamp(new DateTime("2012-04-24T08:19:39Z").toDate())
					.endTime(new DateTime("2013-10-01T07:15:00").toDate())
					.organizer("user@obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
					.timeZone(TimeZone.getTimeZone("Europe/Brussels"))
					.msEventExtId(new MSEventExtId("f28d13af-a5b5-44cf-83c9-3e76aa743179"))
					.reponseRequested(true)
					.recurrenceId(new DateTime("2013-10-01T07:00:00").toDate())
					.recurrences(Lists.newArrayList(
							new MSMeetingRequestRecurrence.Builder()
							.interval(1)
							.type(MSMeetingRequestRecurrenceType.YEARLY_NTH_DAY)
							.monthOfYear(10)
							.weekOfMonth(1)
							.dayOfWeek(Lists.newArrayList(
									MSMeetingRequestRecurrenceDayOfWeek.MONDAY,
									MSMeetingRequestRecurrenceDayOfWeek.TUESDAY,
									MSMeetingRequestRecurrenceDayOfWeek.WEDNESDAY,
									MSMeetingRequestRecurrenceDayOfWeek.THURSDAY,
									MSMeetingRequestRecurrenceDayOfWeek.FRIDAY))
							.build()))
					.build());
	}
	
	@Test
	public void testICalendarConverterSingleAllDayEventFromOBMUI() throws IOException, ParserException {
		ICalendar icalendar = icalendar("single_event_allDay.obmui.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		
		Assertions.assertThat(msMeetingRequest).isEqualTo(
				new MSMeetingRequest.MsMeetingRequestBuilder()
					.startTime(new DateTime("2012-06-15T02:00:00").toDate())
					.dtStamp(new DateTime("2012-06-04T12:59:20Z").toDate())
					.allDayEvent(true)
					.endTime(new DateTime("2012-06-16T02:00:00").toDate())
					.organizer("xavier.niel@jri.obm.lng.org")
					.location("Lyon")
					.instanceType(MSMeetingRequestInstanceType.SINGLE)
					.msEventExtId(new MSEventExtId("4ca827c0eb819956ede718d75cd4c7239f43950a1762a8a2d1378e9061bf2a" +
							"ba3fabf0ed4247ad4d3068078724708ada16134995df87089a0250bfa0f7ff88cd3da9ee5ebc750260"))
					.reponseRequested(true)
					.intDBusyStatus(MSMeetingRequestIntDBusyStatus.FREE)
					.build());
	}

	@Test
	public void testICalendarConverterOrphanedEventException() throws IOException, ParserException {
		ICalendar icalendar = icalendar("orphaned_event_exception.ics");
		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);
		Assertions.assertThat(msMeetingRequest).isNull();
	}

	@Test
	public void testICalendarConverterYearlyWithoutBYMONTHGetTheStartDateOne() throws IOException, ParserException {
		ICalendar icalendar = icalendar("recur_event_freq-yearly_bymonth_unset.ics");

		MSMeetingRequest msMeetingRequest = icalendarConverter.convertToMSMeetingRequest(icalendar);

		int julyMonthIndex = 7;
		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(msMeetingRequest.getRecurrences());
		Assertions.assertThat(recurrence.getMonthOfYear()).isEqualTo(julyMonthIndex);
	}
	
	@Test
	public void testRetrieveMonthForNullTimeZone() {
		ComponentList components = new ComponentList();
		components.add(new VEvent(new Date(DateUtils.date("2013-01-01T01:00:00+00")), "event summary"));
		TimeZone iCalendarTimeZone = null;

		Integer retreiveMonthFromStartTime = icalendarConverter.retrieveMonthFromStartTime(new ICalendarEvent(new Calendar(components)), iCalendarTimeZone);
		
		Assertions.assertThat(retreiveMonthFromStartTime).isEqualTo(1);
	}
	
	@Test
	public void testRetrieveMonthForEarlyerTimeZone() {
		ComponentList components = new ComponentList();
		net.fortuna.ical4j.model.DateTime vEventStartTime = new net.fortuna.ical4j.model.DateTime(DateUtils.date("2013-01-01T05:00:00+00"));
		components.add(new VEvent(vEventStartTime, "event summary"));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(new Calendar(components));
		TimeZone iCalendarTimeZone = TimeZone.getTimeZone("GMT-8:00");

		Integer retreiveMonthFromStartTime = icalendarConverter.retrieveMonthFromStartTime(iCalendarEvent, iCalendarTimeZone);
		
		Assertions.assertThat(retreiveMonthFromStartTime).isEqualTo(12);
	}
	
	@Test
	public void testRetrieveMonthForLaterTimeZone() {
		ComponentList components = new ComponentList();
		net.fortuna.ical4j.model.DateTime vEventStartTime = new net.fortuna.ical4j.model.DateTime(DateUtils.date("2013-12-31T20:00:00+00"));
		components.add(new VEvent(vEventStartTime, "event summary"));
		ICalendarEvent iCalendarEvent = new ICalendarEvent(new Calendar(components));
		TimeZone iCalendarTimeZone = TimeZone.getTimeZone("GMT+8:00");

		Integer retreiveMonthFromStartTime = icalendarConverter.retrieveMonthFromStartTime(iCalendarEvent, iCalendarTimeZone);
		
		Assertions.assertThat(retreiveMonthFromStartTime).isEqualTo(1);
	}
	
	private ICalendar icalendar(String filename) throws IOException, ParserException {
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("ics/" + filename);
		if (in == null) {
			Assert.fail("Cannot load " + filename);
		}
		return new ICalendar.Builder().inputStream(in).build();	
	}
}
