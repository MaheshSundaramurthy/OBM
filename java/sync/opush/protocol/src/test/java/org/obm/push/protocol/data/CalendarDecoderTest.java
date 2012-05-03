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

package org.obm.push.protocol.data;

import static org.obm.push.TestUtils.getXml;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.protocol.data.CalendarDecoder;
import org.w3c.dom.Document;

import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class CalendarDecoderTest {
	
	private CalendarDecoder decoder;
	
	@Before
	public void prepareEventConverter(){
		decoder = new CalendarDecoder();
	}
	
	@Test
	public void testDecodeAttendees() throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<TimeZone>xP///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==</TimeZone>");
		builder.append("<AllDayEvent>0</AllDayEvent>");
		builder.append("<BusyStatus>2</BusyStatus>");
		builder.append("<DTStamp>20110228T144758Z</DTStamp>");
		builder.append("<EndTime>20110306T120000Z</EndTime>");
		builder.append("<Location>Toulouse</Location>");
		builder.append("<MeetingStatus>1</MeetingStatus>");
		builder.append("<Sensitivity>0</Sensitivity>");
		builder.append("<Subject>opush2cccttra</Subject>");
		builder.append("<StartTime>20110306T110000Z</StartTime>");
		builder.append("<UID>d68eb415</UID>");
		builder.append("<Attendees>");
		builder.append("<Attendee>");
		builder.append("<AttendeeName>Poupard Adrien</AttendeeName>");
		builder.append("<AttendeeEmail>adrien@test.tlse.lng</AttendeeEmail>");
		builder.append("<AttendeeStatus>3</AttendeeStatus>");
		builder.append("<AttendeeType>1</AttendeeType>");
		builder.append("</Attendee>");
		builder.append("<Attendee>");
		builder.append("<AttendeeName>Admin instrator</AttendeeName>");
		builder.append("<AttendeeEmail>administrator@test.tlse.lng</AttendeeEmail>");
		builder.append("<AttendeeStatus>5</AttendeeStatus>");
		builder.append("<AttendeeType>2</AttendeeType>");
		builder.append("</Attendee>");
		builder.append("<Attendee>");
		builder.append("<AttendeeName>Sara Connor</AttendeeName>");
		builder.append("<AttendeeEmail>sara@test.tlse.lng</AttendeeEmail>");
		builder.append("<AttendeeStatus>4</AttendeeStatus>");
		builder.append("<AttendeeType>1</AttendeeType>");
		builder.append("</Attendee>");
		builder.append("</Attendees>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		Assert.assertTrue(data instanceof MSEvent);
		MSEvent event = (MSEvent)data;
		Assert.assertEquals(3, event.getAttendees().size());
		MSAttendee adrien = null;
		MSAttendee administrator = null;
		MSAttendee sara = null;
		for(MSAttendee att : event.getAttendees()){
			if("adrien@test.tlse.lng".equals(att.getEmail())){
				adrien = att;
			} else if("administrator@test.tlse.lng".equals(att.getEmail())){
				administrator = att;
			} else if("sara@test.tlse.lng".equals(att.getEmail())){
				sara = att;
			}
		}
		
		checkAttendee(adrien, "Poupard Adrien", "adrien@test.tlse.lng", AttendeeStatus.ACCEPT, AttendeeType.REQUIRED);
		checkAttendee(administrator, "Admin instrator", "administrator@test.tlse.lng", AttendeeStatus.NOT_RESPONDED, AttendeeType.OPTIONAL);
		checkAttendee(sara, "Sara Connor", "sara@test.tlse.lng", AttendeeStatus.DECLINE, AttendeeType.REQUIRED);
		
		
	}
	
	private void checkAttendee(MSAttendee att, String name,
			String email, AttendeeStatus status, AttendeeType type) {
		Assert.assertNotNull(att);
		Assert.assertEquals(name, att.getName());
		Assert.assertEquals(email, att.getEmail());
		Assert.assertEquals(status, att.getAttendeeStatus());
		Assert.assertEquals(type, att.getAttendeeType());
	}
	
}
