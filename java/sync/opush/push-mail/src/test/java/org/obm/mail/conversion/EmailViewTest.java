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
package org.obm.mail.conversion;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;
import net.fortuna.ical4j.data.ParserException;

import org.junit.Test;
import org.minig.imap.Address;
import org.minig.imap.Envelope;
import org.minig.imap.Flag;
import org.obm.DateUtils;
import org.obm.icalendar.ICalendar;
import org.obm.mail.conversation.EmailView;
import org.obm.mail.conversation.EmailViewAttachment;
import org.obm.mail.conversation.EmailViewInvitationType;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.exception.EmailViewBuildException;

import com.google.common.collect.Lists;

public class EmailViewTest {

	@Test(expected=EmailViewBuildException.class)
	public void testUidDefault() {
		new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.bodyType(MSEmailBodyType.PlainText)
			.build();
	}

	@Test
	public void testUid() {
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		assertThat(emailView.getUid()).isEqualTo(155);
	}
	
	@Test
	public void testUidNegativeValue() {
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(-115)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		assertThat(emailView.getUid()).isEqualTo(-115);
	}

	@Test(expected=EmailViewBuildException.class)
	public void testEnvelopeRequired() {
		new EmailView.Builder().envelope(null)
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.bodyType(MSEmailBodyType.PlainText)
			.build();
	}

	@Test(expected=EmailViewBuildException.class)
	public void testBodyMimePartDataRequired() {
		new EmailView.Builder().bodyMimePartData(null)
			.envelope(anyEnvelope())
			.uid(155)
			.bodyType(MSEmailBodyType.PlainText)
			.build();
	}
	
	@Test(expected=NullPointerException.class)
	public void testFlagsAtNull() {
		new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.flags(null)
			.bodyType(MSEmailBodyType.PlainText);
	}

	@Test
	public void testFlags() {
		List<Flag> mutableFlagsList = Lists.newArrayList(Flag.ANSWERED);
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(mutableFlagsList)
			.uid(155)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		assertThat(emailView.getFlags()).containsOnly(Flag.ANSWERED);
	}

	@Test
	public void testFlagsDefault() {
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		assertThat(emailView.getFlags()).isEmpty();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testFlagsDefaultIsImmutable() {
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		emailView.getFlags().add(Flag.ANSWERED);
	}

	@Test
	public void testFlagsIsNotLinkedToCollectionArg() {
		List<Flag> mutableFlagsList = Lists.newArrayList(Flag.ANSWERED);
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(mutableFlagsList)
			.uid(155)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		mutableFlagsList.add(Flag.DELETED);
		
		assertThat(emailView.getFlags()).containsOnly(Flag.ANSWERED);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testFlagsIsImmutable() {
		List<Flag> mutableFlagsList = Lists.newArrayList(Flag.ANSWERED);
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(mutableFlagsList)
			.uid(155)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		emailView.getFlags().add(Flag.DELETED);
	}
	
	@Test
	public void testAttachments() {
		EmailViewAttachment emailViewAttachment = anyEmailViewAttachment("id");
		List<EmailViewAttachment> attachments = Lists.newArrayList(emailViewAttachment);
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(Lists.newArrayList(Flag.ANSWERED))
			.uid(155)
			.attachments(attachments)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		assertThat(emailView.getAttachments()).containsOnly(emailViewAttachment);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testAttachmentsIsImmutable() {
		List<EmailViewAttachment> attachments = Lists.newArrayList(anyEmailViewAttachment("id"));
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(Lists.newArrayList(Flag.ANSWERED))
			.uid(155)
			.attachments(attachments)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		emailView.getAttachments().add(anyEmailViewAttachment("id2"));
	}
	
	@Test
	public void testAttachmentsNotLinkedToCollectionArg() {
		EmailViewAttachment emailViewAttachment = anyEmailViewAttachment("id");
		List<EmailViewAttachment> attachments = Lists.newArrayList(emailViewAttachment);
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(Lists.newArrayList(Flag.ANSWERED))
			.uid(155)
			.attachments(attachments)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		attachments.add(anyEmailViewAttachment("id2"));
		
		assertThat(emailView.getAttachments()).containsOnly(emailViewAttachment);
	}
	
	@Test
	public void testICalendar() throws IOException, ParserException, EmailViewBuildException {
		ICalendar iCalendar = anyICalendar("attendee.ics");
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(Lists.newArrayList(Flag.ANSWERED))
			.uid(155)
			.iCalendar(iCalendar)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		assertThat(emailView.getICalendar()).equals(iCalendar);
	}
	
	@Test
	public void testInvitationType() {
		EmailViewInvitationType invitationType = EmailViewInvitationType.REQUEST;
		
		EmailView emailView = new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.flags(Lists.newArrayList(Flag.ANSWERED))
			.uid(155)
			.invitationType(invitationType)
			.bodyType(MSEmailBodyType.PlainText)
			.truncated(false)
			.build();
		
		assertThat(emailView.getInvitationType()).equals(invitationType);
	}
	
	@Test(expected=EmailViewBuildException.class)
	public void testMimeTypeRequired() {
		new EmailView.Builder()
			.envelope(anyEnvelope())
			.bodyMimePartData(anyBodyMimePartData())
			.uid(155)
			.build();
	}
	
	@Test(expected=EmailViewBuildException.class)
	public void testTruncatedRequired() {
		new EmailView.Builder()
		.envelope(anyEnvelope())
		.bodyMimePartData(anyBodyMimePartData())
		.flags(Lists.newArrayList(Flag.ANSWERED))
		.uid(155)
		.bodyType(MSEmailBodyType.PlainText)
		.build();
	}
	
	@Test
	public void testTruncated() {
		EmailView emailView = new EmailView.Builder()
		.envelope(anyEnvelope())
		.bodyMimePartData(anyBodyMimePartData())
		.flags(Lists.newArrayList(Flag.ANSWERED))
		.uid(155)
		.bodyType(MSEmailBodyType.PlainText)
		.truncated(true)
		.build();
		
		assertThat(emailView.isTruncated()).isTrue();
	}
	
	private InputStream anyBodyMimePartData() {
		return StreamMailTestsUtils.newInputStreamFromString("data test");
	}

	private Envelope anyEnvelope() {
		Envelope envelope = new Envelope.Builder()
			.from(Lists.newArrayList(new Address("from@domain.org")))
			.to(Lists.newArrayList(new Address("to@domain.org")))
			.subject("subject")
			.date(DateUtils.date("2004-12-14T22:00:00"))
			.build();
		return envelope;
	}
	
	private EmailViewAttachment anyEmailViewAttachment(String id) {
		return new EmailViewAttachment(id, "Name", "/file", 20);
	}
	
	private ICalendar anyICalendar(String filename) throws IOException, ParserException {
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("ics/" + filename);
		if (in == null) {
			Assert.fail("Cannot load " + filename);
		}
		return new ICalendar.Builder().inputStream(in).build();	
	}
}
