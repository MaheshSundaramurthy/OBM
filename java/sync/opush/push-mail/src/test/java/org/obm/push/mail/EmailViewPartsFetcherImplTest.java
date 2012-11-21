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

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.james.mime4j.codec.Base64InputStream;
import org.easymock.IMocksControl;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.minig.imap.Address;
import org.minig.imap.Envelope;
import org.minig.imap.Flag;
import org.minig.imap.UIDEnvelope;
import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.BodyParams;
import org.minig.imap.mime.ContentType;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeMessage;
import org.minig.imap.mime.MimePart;
import org.obm.DateUtils;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.mail.conversation.EmailView;
import org.obm.mail.conversation.EmailViewAttachment;
import org.obm.mail.conversation.EmailViewInvitationType;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.imap.ImapMailboxService;
import org.obm.push.mail.transformer.TestIdentityTransformerFactory;
import org.obm.push.mail.transformer.Transformer.TransformersFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

@RunWith(SlowFilterRunner.class)
public class EmailViewPartsFetcherImplTest {

	public static class MessageFixture {

		long uid = 1l;
		
		boolean answered = false;
		boolean read = false;
		boolean starred = false;
		
		List<Address> from = ImmutableList.<Address>of(new Address("from@domain.test")); 
		List<Address> to = ImmutableList.<Address>of(new Address("to@domain.test")); 
		List<Address> cc = ImmutableList.<Address>of(new Address("cc@domain.test"));
		String subject = "a subject";
		Date date = DateUtils.date("2004-12-14T22:00:00");

		int estimatedDataSize = 1000;
		MSEmailBodyType bodyType = MSEmailBodyType.PlainText;
		String bodyPrimaryType = "text";
		String bodySubType = "plain";
		String fullMimeType = bodyPrimaryType + "/" + bodySubType;
		String bodyCharset = Charsets.UTF_8.displayName();
		InputStream bodyData = StreamMailTestsUtils.newInputStreamFromString("message data");
		InputStream bodyDataDecoded = StreamMailTestsUtils.newInputStreamFromString("message data");
		InputStream attachment;
		InputStream attachmentDecoded;
		String contentId = "contentId";
		boolean isAttachment = false;
		boolean isInvitation = false;
		boolean isICSAttachment = false;
		String encoding = null;
		Boolean truncated = false;
	}
	
	private MessageFixture messageFixture;
	private String messageCollectionName;
	private Integer messageCollectionId;
	private String mailbox;
	private String password;
	private UserDataRequest udr;
	private MimeAddress mimeAddress;

	@Before
	public void setUp() throws IOException {
		mailbox = "to@localhost.com";
		password = "password";
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		
		messageFixture = new MessageFixture();
		messageFixture.attachment = Resources.getResource("ics/attendee.ics").openStream();
		messageFixture.attachmentDecoded = Resources.getResource("ics/attendee.ics").openStream();
		messageCollectionName = IMAP_INBOX_NAME;
		messageCollectionId = 1;
		mimeAddress = new MimeAddress("address");
	}
	
	@Test
	public void testFlagAnsweredTrue() throws Exception {
		messageFixture.answered = true;
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getFlags()).contains(Flag.ANSWERED);
	}
	
	@Test
	public void testFlagAnsweredFalse() throws Exception {
		messageFixture.answered = false;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getFlags()).doesNotContain(Flag.ANSWERED);
	}
	
	@Test
	public void testFlagReadTrue() throws Exception {
		messageFixture.read = true;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getFlags()).contains(Flag.SEEN);
	}
	
	@Test
	public void testFlagReadFalse() throws Exception {
		messageFixture.read = false;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getFlags()).doesNotContain(Flag.SEEN);
	}
	
	@Test
	public void testFlagStarredTrue() throws Exception {
		messageFixture.starred = true;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getFlags()).contains(Flag.FLAGGED);
	}
	
	@Test
	public void testFlagStarredFalse() throws Exception {
		messageFixture.starred = false;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getFlags()).doesNotContain(Flag.FLAGGED);
	}
	
	@Test
	public void testHeaderFromNull() throws Exception {
		messageFixture.from = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getFrom()).isEmpty();
	}

	@Test
	public void testHeaderFromEmpty() throws Exception {
		messageFixture.from = ImmutableList.<Address>of(newEmptyAddress());

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getFrom()).containsOnly(newEmptyAddress());
	}
	
	@Test
	public void testHeaderFrom() throws Exception {
		messageFixture.from = ImmutableList.<Address>of(new Address("from@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getFrom()).containsOnly(new Address("from@domain.test"));
	}
	
	@Test
	public void testHeaderToNull() throws Exception {
		messageFixture.to = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getTo()).isEmpty();
	}

	@Test
	public void testHeaderToEmpty() throws Exception {
		messageFixture.to = ImmutableList.<Address>of(newEmptyAddress());

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getTo()).containsOnly(newEmptyAddress());
	}
	
	@Test
	public void testHeaderToSingle() throws Exception {
		messageFixture.to = ImmutableList.<Address>of(new Address("to@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getTo()).containsOnly(new Address("to@domain.test"));
	}
	
	@Test
	public void testHeaderToMultiple() throws Exception {
		messageFixture.to = ImmutableList.<Address>of(
				new Address("to@domain.test"), new Address("to2@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getTo()).containsOnly(
				new Address("to@domain.test"), new Address("to2@domain.test"));
	}
	
	@Test
	public void testHeaderCcNull() throws Exception {
		messageFixture.cc = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getCc()).isEmpty();
	}

	@Test
	public void testHeaderCcEmpty() throws Exception {
		messageFixture.cc = ImmutableList.<Address>of(newEmptyAddress());

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getCc()).containsOnly(newEmptyAddress());
	}
	
	@Test
	public void testHeaderCcSingle() throws Exception {
		messageFixture.cc = ImmutableList.<Address>of(new Address("cc@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getCc()).containsOnly(new Address("cc@domain.test"));
	}
	
	@Test
	public void testHeaderCcMultiple() throws Exception {
		messageFixture.cc = ImmutableList.<Address>of(
				new Address("cc@domain.test"), new Address("cc2@domain.test")); 

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getCc()).containsOnly(
				new Address("cc@domain.test"), new Address("cc2@domain.test"));
	}
	
	@Test
	public void testHeaderSubjectNull() throws Exception {
		messageFixture.subject = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getSubject()).isNull();
	}
	
	@Test
	public void testHeaderSubjectEmpty() throws Exception {
		messageFixture.subject = "";

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getSubject()).isEmpty();
	}
	
	@Test
	public void testHeaderSubject() throws Exception {
		messageFixture.subject = "a subject";

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getSubject()).isEqualTo("a subject");
	}
	
	@Test
	public void testHeaderDateNull() throws Exception {
		messageFixture.date = null;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getDate()).isNull();
	}
	
	@Test
	public void testHeaderDate() throws Exception {
		messageFixture.date = DateUtils.date("2004-12-14T22:00:00");

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getDate()).isEqualTo(DateUtils.date("2004-12-14T22:00:00"));
	}
	
	@Test
	public void testUid() throws Exception {
		messageFixture.uid = 165l; 

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getUid()).isEqualTo(165l);
	}
	
	@Test
	public void testBodyTruncationNull() throws Exception {
		messageFixture.estimatedDataSize = 0;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getEstimatedDataSize()).isEqualTo(0);
	}
	
	@Test
	public void testBodyTruncation() throws Exception {
		messageFixture.estimatedDataSize = 1505;

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);
		
		assertThat(emailView.getEstimatedDataSize()).isEqualTo(1505);
	}
	
	@Test
	public void testBodyMimePartDataNull() throws Exception {
		messageFixture.bodyData = null;
		messageFixture.bodyDataDecoded = null;

		Assertions.assertThat(newFetcherFromExpectedFixture()
				.fetch(messageFixture.uid)).isNull();
	}
	
	@Test
	public void testBodyMimePartData() throws Exception {
		messageFixture.bodyData = StreamMailTestsUtils.newInputStreamFromString("email data");
		messageFixture.bodyDataDecoded = StreamMailTestsUtils.newInputStreamFromString("email data");

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getBodyMimePartData())
			.hasContentEqualTo(StreamMailTestsUtils.newInputStreamFromString("email data"));
	}
	
	@Test
	public void testWithoutAttachment() throws Exception {
		messageFixture.isAttachment = false;
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getAttachments()).isEmpty();
	}
	
	@Test
	public void testAttachment() throws Exception {
		messageFixture.isAttachment = true;
		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getAttachments()).hasSize(1);
		EmailViewAttachment emailViewAttachment = Iterables.getOnlyElement(emailView.getAttachments());
		assertThat(emailViewAttachment.getId()).isEqualTo("at_" + messageFixture.uid + "_0");
	}
	
	@Test @Slow
	public void testInvitation() throws Exception {
		messageFixture.isAttachment = true;
		messageFixture.isInvitation = true;
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getICalendar()).isNotNull();
		assertThat(emailView.getInvitationType()).isEqualTo(EmailViewInvitationType.REQUEST);
	}
	
	@Test
	public void testContentType() throws Exception {
		String mimeType = "text/html";
		messageFixture.bodyType = MSEmailBodyType.fromMimeType(mimeType);
		
		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getBodyType().getMimeType()).isEqualTo(mimeType);
	}

	@Test
	public void testInvitationInBASE64() throws Exception {
		messageFixture.isAttachment = true;
		messageFixture.isInvitation = true;
		messageFixture.encoding = "BASE64";
		messageFixture.attachment = Resources.getResource("ics/base64.ics").openStream();
		messageFixture.attachmentDecoded = new Base64InputStream(Resources.getResource("ics/base64.ics").openStream());

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getICalendar()).isNotNull();
		assertThat(emailView.getICalendar().getICalendar()).contains("DESCRIPTION:Encoding Invitation to BASE64 !");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvitationInBadEncodingFormat() throws Exception {
		messageFixture.isAttachment = true;
		messageFixture.isInvitation = true;
		messageFixture.encoding = "QUOTED-PRINTABLE";
		messageFixture.attachment = Resources.getResource("ics/base64.ics").openStream();
		messageFixture.attachmentDecoded = new QuotedPrintableInputStream(Resources.getResource("ics/base64.ics").openStream());

		newFetcherFromExpectedFixture().fetch(messageFixture.uid);
	}
	
	@Test
	public void testBodyDataInBASE64() throws Exception {
		messageFixture.encoding = "BASE64";
		messageFixture.bodyData = StreamMailTestsUtils.newInputStreamFromString("RW5jb2RpbmcgYm9keURhdGEgdG8gQkFTRTY0ICE=");
		messageFixture.bodyDataDecoded = new Base64InputStream( 
				StreamMailTestsUtils.newInputStreamFromString("RW5jb2RpbmcgYm9keURhdGEgdG8gQkFTRTY0ICE="));

		EmailView emailView = newFetcherFromExpectedFixture().fetch(messageFixture.uid);

		assertThat(emailView.getBodyMimePartData()).hasContentEqualTo(
				StreamMailTestsUtils.newInputStreamFromString("Encoding bodyData to BASE64 !"));
	}
	
	@Test
	public void testNoAttachmentFoundWhenLeafIsNestedMultipartMixed() {
		testNoAttachmentFoundWhenLeafHasContentTypeOf("multipart/mixed");
	}

	@Test
	public void testNoAttachmentFoundWhenLeafIsNestedMultipartAlternative() {
		testNoAttachmentFoundWhenLeafHasContentTypeOf("multipart/alternative");
	}

	private void testNoAttachmentFoundWhenLeafHasContentTypeOf(String contentType) {
		EmailView.Builder shouldGetEmptyAttachmentListViewBuilder = createStrictMock(EmailView.Builder.class);
		expect(shouldGetEmptyAttachmentListViewBuilder.attachments(Collections.<EmailViewAttachment>emptyList()))
			.andReturn(shouldGetEmptyAttachmentListViewBuilder)
			.once();
		
		IMimePart multipartLeaf = MimePart.builder().contentType(contentType).build();
		int multipartLeafIndex = 5;

		IMimePart parentMimePart = createMock(IMimePart.class);
		expect(parentMimePart.findRootMimePartInTree()).andReturn(parentMimePart);
		expect(parentMimePart.listLeaves(true, true)).andReturn(ImmutableList.of(multipartLeaf));
		multipartLeaf.defineParent(parentMimePart, multipartLeafIndex);
		
		FetchInstruction fetchInstruction = FetchInstruction.builder()
			.mimePart(parentMimePart)
			.mailTransformation(MailTransformation.NONE)
			.build();
		
		replay(parentMimePart, shouldGetEmptyAttachmentListViewBuilder);

		long messageUid = 1l;
		EmailViewPartsFetcherImpl partsFetcher = new EmailViewPartsFetcherImpl(identityMailTransformerFactory(), null, null, null, null, null);
		partsFetcher.fetchAttachments(shouldGetEmptyAttachmentListViewBuilder, fetchInstruction, messageUid);
		
		verify(parentMimePart, shouldGetEmptyAttachmentListViewBuilder);
	}
	
	@Test
	public void testEmlAttachmentIsALeaf() {
		String attachmentName = "attachment.eml";
		long attachmentMailUid = 3;
		int attachmentCollection = 12;
		int attachmentSize = 1337;
		
		MimePart textPlain = MimePart.builder().contentType("text/plain").build();
		MimePart rfc822EmbeddedAttachment = MimePart.builder()
				.contentType("message/rfc822")
				.contentDisposition("attachment")
				.size(attachmentSize)
				.bodyParams(BodyParams.builder().add(new BodyParam("name", attachmentName)).build())
				.addChild(MimePart.builder()
						.contentType("multipart/alternative")
						.addChild(MimePart.builder().contentType("text/plain").build())
						.addChild(MimePart.builder().contentType("text/html").build())
						.build())
				.build();
		MimeMessage message = MimeMessage.builder().addChild(
				MimePart.builder()
					.contentType("multipart/mixed")
					.addChild(textPlain)
					.addChild(rfc822EmbeddedAttachment)
					.build())
				.build();

		String expectedId = "at_" + attachmentMailUid + "_" + "0";
		String expectedFileReference = AttachmentHelper
				.getAttachmentId(
					String.valueOf(attachmentCollection), String.valueOf(attachmentMailUid), 
					rfc822EmbeddedAttachment.getAddress().getAddress(),
					rfc822EmbeddedAttachment.getFullMimeType(),
					rfc822EmbeddedAttachment.getContentTransfertEncoding());
		EmailViewAttachment expectedAttachment = EmailViewAttachment.builder()
				.id(expectedId)
				.displayName(attachmentName)
				.fileReference(expectedFileReference)
				.size(attachmentSize)
				.contentType(ContentType.builder()
						.contentType("message/rfc822")
						.add(BodyParams.builder().add(new BodyParam("name", attachmentName)).build())
						.build())
				.build();
		
		EmailView.Builder emailViewBuilder = createStrictMock(EmailView.Builder.class);
		expect(emailViewBuilder.attachments(Lists.newArrayList(expectedAttachment))).andReturn(emailViewBuilder);
		replay(emailViewBuilder);

		FetchInstruction fetchInstruction = FetchInstruction.builder().mimePart(message).build();
		new EmailViewPartsFetcherImpl(identityMailTransformerFactory(), null, null, null, null, attachmentCollection)
				.fetchAttachments(emailViewBuilder, fetchInstruction, attachmentMailUid);
		
		verify(emailViewBuilder);
	}
	
	@Test
	public void testDisplayNameWhenNoNameNoContentId() {
		Optional<String> displayName = getDisplayNameOfMimePart(MimePart.builder()
				.contentType("text/plain")
				.build());
		
		assertThat(displayName.isPresent()).isFalse();
	}
	
	@Test
	public void testDisplayNameWhenName() {
		Optional<String> displayName = getDisplayNameOfMimePart(MimePart.builder()
				.contentType("text/plain")
				.bodyParams(BodyParams.builder().add(
						new BodyParam("name", "hello"))
						.build())
				.build());
		
		assertThat(displayName.isPresent()).isTrue();
		assertThat(displayName.get()).isEqualTo("hello");
	}
	
	@Test
	public void testDisplayNameWhenContentId() {
		Optional<String> displayName = getDisplayNameOfMimePart(MimePart.builder()
				.contentType("text/plain")
				.contentId("hello")
				.build());
		
		assertThat(displayName.isPresent()).isTrue();
		assertThat(displayName.get()).isEqualTo("hello");
	}
	
	@Test
	public void testDisplayNameGetNameWhenBoth() {
		Optional<String> displayName = getDisplayNameOfMimePart(MimePart.builder()
				.contentType("text/plain")
				.contentId("hello contentId")
				.bodyParams(BodyParams.builder().add(
						new BodyParam("name", "hello Name"))
						.build())
				.build());
		
		assertThat(displayName.isPresent()).isTrue();
		assertThat(displayName.get()).isEqualTo("hello Name");
	}

	private Optional<String> getDisplayNameOfMimePart(IMimePart attachment) {
		IMocksControl mocks = createControl();
		TransformersFactory transformer = mocks.createMock(TransformersFactory.class);
		PrivateMailboxService mailboxService = mocks.createMock(PrivateMailboxService.class);
		List<BodyPreference> preferences = mocks.createMock(List.class);
		
		
		mocks.replay();
		Optional<String> displayName = new EmailViewPartsFetcherImpl(transformer, mailboxService, preferences, udr, "name", 15)
			.selectAttachmentDisplayName(attachment);
	
		mocks.verify();
		return displayName;
	}
	
	private ImapMailboxService messageFixtureToMailboxServiceMock() throws Exception {
		ImapMailboxService mailboxService = createStrictMock(ImapMailboxService.class);
		mockMailboxServiceFlags(mailboxService);
		mockMailboxServiceEnvelope(mailboxService);
		mockMailboxServiceBody(mailboxService);
		replay(mailboxService);
		return mailboxService;
	}

	private void mockMailboxServiceFlags(ImapMailboxService mailboxService) throws MailException {
		Builder<Flag> flagsListBuilder = ImmutableList.builder();
		if (messageFixture.answered) {
			flagsListBuilder.add(Flag.ANSWERED);
		}
		if (messageFixture.read) {
			flagsListBuilder.add(Flag.SEEN);
		}
		if (messageFixture.starred) {
			flagsListBuilder.add(Flag.FLAGGED);
		}
		expect(mailboxService.fetchFlags(udr, messageCollectionName, messageFixture.uid))
				.andReturn(flagsListBuilder.build()).once();
	}

	private void mockMailboxServiceEnvelope(ImapMailboxService mailboxService) throws MailException {
		Envelope envelope = Envelope.builder()
			.from(messageFixture.from)
			.to(messageFixture.to)
			.cc(messageFixture.cc)
			.subject(messageFixture.subject)
			.date(messageFixture.date)
			.build();
		
		expect(mailboxService.fetchEnvelope(udr, messageCollectionName, messageFixture.uid))
			.andReturn(new UIDEnvelope(messageFixture.uid, envelope)).once();
	}
	
	private void mockMailboxServiceBody(ImapMailboxService mailboxService) throws MailException {
		expect(mailboxService.fetchBodyStructure(udr, messageCollectionName, messageFixture.uid))
			.andReturn(mockAggregateMimeMessage()).once();

		expect(mailboxService.fetchMimePartData(
				anyObject(UserDataRequest.class),
				anyObject(String.class),
				anyLong(),
				anyObject(FetchInstruction.class)))
			.andReturn(messageFixture.bodyData).once();
		
		expect(mailboxService.findAttachment(udr, messageCollectionName, messageFixture.uid, mimeAddress))
			.andReturn(messageFixture.attachment);
	}

	private MimeMessage mockAggregateMimeMessage() {
		
		MimePart mimePart = createMock(MimePart.class);
		expect(mimePart.getCharset()).andReturn(messageFixture.bodyCharset).anyTimes();
		expect(mimePart.getPrimaryType()).andReturn(messageFixture.bodyPrimaryType).anyTimes();
		expect(mimePart.getSubtype()).andReturn(messageFixture.bodySubType).anyTimes();
		expect(mimePart.findRootMimePartInTree()).andReturn(mimePart);
		expect(mimePart.listLeaves(true, true)).andReturn(ImmutableList.<IMimePart> of(mimePart));
		expect(mimePart.isAttachment()).andReturn(messageFixture.isAttachment);
		expect(mimePart.getName()).andReturn(messageFixture.subject);
		expect(mimePart.getAddress()).andReturn(mimeAddress).anyTimes();
		expect(mimePart.getFullMimeType()).andReturn(messageFixture.fullMimeType).anyTimes();
		expect(mimePart.getContentType()).andReturn(ContentType.builder().contentType(messageFixture.fullMimeType).build()).anyTimes();
		expect(mimePart.getContentTransfertEncoding()).andReturn(messageFixture.encoding).anyTimes();
		expect(mimePart.getSize()).andReturn(messageFixture.estimatedDataSize).anyTimes();
		expect(mimePart.isInvitation()).andReturn(messageFixture.isInvitation);
		expect(mimePart.isCancelInvitation()).andReturn(false);
		expect(mimePart.getContentId()).andReturn(messageFixture.contentId);
		expect(mimePart.isICSAttachment()).andReturn(messageFixture.isICSAttachment);
		expect(mimePart.decodeMimeStream(anyObject(InputStream.class)))
			.andReturn(messageFixture.bodyDataDecoded);
		expect(mimePart.decodeMimeStream(anyObject(InputStream.class)))
			.andReturn(messageFixture.attachmentDecoded);

		MimeMessage mimeMessage = createMock(MimeMessage.class);
		expect(mimeMessage.getMimePart()).andReturn(null);
		expect(mimeMessage.findMainMessage(anyObject(ContentType.class))).andReturn(mimePart).anyTimes();
		expect(mimeMessage.findRootMimePartInTree()).andReturn(mimeMessage);
		expect(mimeMessage.listLeaves(true, true)).andReturn(ImmutableList.<IMimePart> of(mimePart));

		replay(mimeMessage, mimePart);
		return mimeMessage;
	}

	private EmailViewPartsFetcherImpl newFetcherFromExpectedFixture() throws Exception {
		return new EmailViewPartsFetcherImpl(
				identityMailTransformerFactory(),
				messageFixtureToMailboxServiceMock(), bodyPreferences(),
				udr, messageCollectionName, messageCollectionId);
	}

	private TransformersFactory identityMailTransformerFactory() {
		TransformersFactory transformersFactory = createMock(TransformersFactory.class);
		expect(transformersFactory.create(anyObject(FetchInstruction.class))).andDelegateTo(new TestIdentityTransformerFactory()).anyTimes();
		replay(transformersFactory);
		return transformersFactory;
	}

	public Address newEmptyAddress() {
		return new Address("");
	}

	private ArrayList<BodyPreference> bodyPreferences() {
		BodyPreference.Builder builder = BodyPreference.builder()
			.bodyType(messageFixture.bodyType);
		if (messageFixture.estimatedDataSize != 0) {
			builder.truncationSize(messageFixture.estimatedDataSize);
		}
		return Lists.newArrayList(builder.build());
	}

}
