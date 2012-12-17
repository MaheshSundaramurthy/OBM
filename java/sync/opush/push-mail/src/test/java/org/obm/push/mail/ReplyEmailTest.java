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

import static org.obm.push.mail.MSMailTestsUtils.loadMimeMessage;
import static org.obm.push.mail.MSMailTestsUtils.mockOpushConfigurationService;

import java.io.IOException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmail;
import org.obm.push.mail.exception.NotQuotableEmailException;
import org.obm.push.utils.Mime4jUtils;

import com.google.common.collect.ImmutableMap;


import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class ReplyEmailTest {

	private Mime4jUtils mime4jUtils;

	@Before
	public void setUp() {
		mime4jUtils = new Mime4jUtils();
	}
	
	@Test
	public void testJira2362() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailPlainText("origin");
		Message reply = loadMimeMessage("jira-2362.eml");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		String messageAsString = mime4jUtils.toString(message.getBody());
		Assertions.assertThat(messageAsString).contains("Envoyé depuis mon HTC").contains("> origin");
	}

	@Test
	public void testReplyCopyOfAddress() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailPlainText("origin");
		Message reply = loadMimeMessage("plainText.eml");
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
	
		Assertions.assertThat(replyEmail.getFrom()).isEqualToIgnoringCase("from@linagora.test");
		Assertions.assertThat(replyEmail.getTo()).containsOnly(MSMailTestsUtils.addr("a@test"), MSMailTestsUtils.addr("b@test"));
		Assertions.assertThat(replyEmail.getCc()).containsOnly(MSMailTestsUtils.addr("c@test"));
		Assertions.assertThat(replyEmail.getCci()).containsOnly(MSMailTestsUtils.addr("d@test"));
	}
	
	@Test
	public void testReplyEncodingShouldBeUTF8() throws IOException, MimeException, NotQuotableEmailException {
		Message reply = loadMimeMessage("plainText.eml");
		MSEmail original = MSMailTestsUtils.createMSEmailPlainTextASCII("origin");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.getCharset()).isEqualToIgnoringCase("UTF-8");
	}

	@Test
	public void testReplyTextToText() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		Message reply = MSMailTestsUtils.createMessagePlainText(mime4jUtils,"response text");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("response text").contains("\n> origin").contains("\n> Cordialement");
	}
	
	@Test
	public void testReplyTextToTextWithAttachment() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		Message reply = loadMimeMessage("MAIL-WITH-ATTACHMENT.eml");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/mixed");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		Multipart multipart = (Multipart) message.getBody();
		Entity textPlainPart = mime4jUtils.getFirstTextPlainPart(multipart);
		String messageAsString = mime4jUtils.toString(textPlainPart.getBody());
		Assertions.assertThat(messageAsString).contains("C'est le message ;-)").contains("\n> origin").contains("\n> Cordialement");
	}
	
	@Test
	public void testReplyTextToTextFormated() throws IOException, MimeException, NotQuotableEmailException {
		String replyText = "\nresponse text\r\r\rEnvoyé à partir de mon SuperPhone\n\n\n";
		String replyTextExpected = "\r\nresponse text\r\n\r\n\r\nEnvoyé à partir de mon SuperPhone\r\n\r\n\r\n"; 
		MSEmail original = MSMailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		Message reply = MSMailTestsUtils.createMessagePlainText(mime4jUtils,replyText);

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).startsWith(replyTextExpected);
		Assertions.assertThat(messageAsString).contains("\r\n> origin").contains("\r\n> Cordialement");
	}
	
	@Test
	public void testReplyHtmlToHtml() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailHtmlText("origin\nCordialement");
		Message reply = MSMailTestsUtils.createMessageHtml(mime4jUtils,"response text");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/html");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("origin").contains("Cordialement");
		Assertions.assertThat(messageAsString).contains("response text").containsIgnoringCase("</blockquote>");
	}
	
	@Test
	public void testReplyTextToBoth() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailMultipartAlt("origin\nCordialement");
		Message reply = MSMailTestsUtils.createMessagePlainText(mime4jUtils,"response text");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("response text").contains("\n> origin").contains("\n> Cordialement");
	}

	@Test
	public void testReplyHtmlToBoth() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailMultipartAlt("origin\nCordialement");
		Message reply = MSMailTestsUtils.createMessageHtml(mime4jUtils, "<b>response html</b>");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/html");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("origin").contains("Cordialement");
		Assertions.assertThat(messageAsString).containsIgnoringCase("<b>response html</b>").containsIgnoringCase("</blockquote>");
	}

	@Test
	public void testReplyBothToText() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		Message reply = MSMailTestsUtils.createMessageTextAndHtml(mime4jUtils, "response text", "response html");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		Multipart multipart = (Multipart) message.getBody();
		Assertions.assertThat(multipart.getBodyParts()).hasSize(2);
		Entity plainTextPart = multipart.getBodyParts().get(0);
		Entity htmlTextPart = multipart.getBodyParts().get(1);
		Assertions.assertThat(plainTextPart.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(htmlTextPart.getMimeType()).isEqualTo("text/html");
		String textPlainAsString = mime4jUtils.toString(plainTextPart.getBody());
		Assertions.assertThat(textPlainAsString).contains("response text").contains("origin").contains("Cordialement");
		String textHtmlAsString = mime4jUtils.toString(htmlTextPart.getBody());
		Assertions.assertThat(textHtmlAsString).containsIgnoringCase("</blockquote>").contains("response html").contains("origin").contains("Cordialement");
	}
	
	@Test
	public void testReplyBothToHtml() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailHtmlText("origin\nCordialement");
		Message reply = MSMailTestsUtils.createMessageTextAndHtml(mime4jUtils, "response text", "response html");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/html");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		String messageAsString = mime4jUtils.toString(message.getBody());
		Assertions.assertThat(messageAsString).contains("origin").contains("Cordialement");
		Assertions.assertThat(messageAsString).containsIgnoringCase("</blockquote>").contains("response html");
	}
	
	@Test
	public void testReplyBothToBoth() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailMultipartAlt("origin\nCordialement");
		Message reply = MSMailTestsUtils.createMessageTextAndHtml(mime4jUtils, "response text","response html");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		Multipart multipart = (Multipart) message.getBody();
		Assertions.assertThat(multipart.getBodyParts()).hasSize(2);
		Entity plainTextPart = multipart.getBodyParts().get(0);
		Entity htmlTextPart = multipart.getBodyParts().get(1);
		Assertions.assertThat(plainTextPart.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(htmlTextPart.getMimeType()).isEqualTo("text/html");
		String textPlainAsString = mime4jUtils.toString(plainTextPart.getBody());
		Assertions.assertThat(textPlainAsString).contains("response text").contains("origin").contains("Cordialement");
		String textHtmlAsString = mime4jUtils.toString(htmlTextPart.getBody());
		Assertions.assertThat(textHtmlAsString).containsIgnoringCase("</blockquote>").contains("response html").contains("origin").contains("Cordialement");
	}
	
	@Test
	public void testReplyTextToMixed() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailMultipartMixed("origin\nCordialement");
		Message reply = MSMailTestsUtils.createMessagePlainText(mime4jUtils, "response text");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("response text").contains("\n> origin").contains("\n> Cordialement");
	}
	
	@Test
	public void testReplyMixedToText() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		byte[] dataToSend = new byte[]{0,1,2,3,4};
		Message reply = MSMailTestsUtils.createMessageMultipartMixed(mime4jUtils, "response text", dataToSend);

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/mixed");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		
		Multipart parts = (Multipart) message.getBody();
		Assertions.assertThat(parts.getCount()).isEqualTo(2);
		Assertions.assertThat(mime4jUtils.getAttachmentCount(parts)).isEqualTo(1);
		Assertions.assertThat(mime4jUtils.getAttachmentContentTypeList(parts)).containsOnly("image/png");
		
		BinaryBody binaryPart = (BinaryBody) parts.getBodyParts().get(0).getBody();
        byte[] dataRead = new byte[5];
        binaryPart.getInputStream().read(dataRead);
        Assertions.assertThat(dataRead).isEqualTo(dataToSend);

		Entity plainTextPart = parts.getBodyParts().get(1);
		Assertions.assertThat(plainTextPart.getMimeType()).isEqualTo("text/plain");
		String textPlainAsString = mime4jUtils.toString(plainTextPart.getBody());
		Assertions.assertThat(textPlainAsString).contains("response text").contains("origin").contains("Cordialement");
	}
	
	@Test
	public void testTerminationSequenceEndLineInHTMLReplyEmail() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MSMailTestsUtils.createMSEmailPlainText("origin");
		Message reply = loadMimeMessage("jira-2362.eml");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply,
				ImmutableMap.<String, MSAttachementData>of());
		
		Message message = replyEmail.getMimeMessage();
		String messageAsString = mime4jUtils.toString(message.getBody());

		String badReplyFormatMessage = buildReplyEmailWithEndLineCharacter("\n");
		String goodReplyFormatMessage = buildReplyEmailWithEndLineCharacter(ReplyEmail.EMAIL_LINEBREAKER);
		
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		
		Assertions.assertThat(messageAsString).doesNotContain(badReplyFormatMessage);
		Assertions.assertThat(messageAsString).contains(goodReplyFormatMessage );
	}
	
	private String buildReplyEmailWithEndLineCharacter(String endCharacter) {
		StringBuilder str = new StringBuilder();
		str.append("<HTML>").append(endCharacter);
		str.append("<HEAD>").append(endCharacter);
		str.append("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">").append(endCharacter);
		str.append("</HEAD>").append(endCharacter);
		str.append("<BODY xmlns=\"http://www.w3.org/1999/xhtml\">Test<BR>").append(endCharacter);
		str.append("<BR>Envoy&eacute; depuis mon HTC<BR>").append(endCharacter);
		str.append("<BR>").append(endCharacter);
		str.append("<DIV id=\"htc_header\" style=\"\">----- Reply message -----<BR>De : \"Jean Jaures\" &lt;jaures@obm.matthieu.lng&gt;<BR>Pour&nbsp;: \"L&eacute;on Blum\" &lt;blum@obm.matthieu.lng&gt;<BR>Objet : mail de test<BR>Date : lun., nov. 7, 2011 09:32<BR>").append(endCharacter);
		str.append("<BR>").append(endCharacter);
		str.append("</DIV>").append(endCharacter);
		str.append("<BR>").append(endCharacter);
		str.append("<BR>").append(endCharacter);
		str.append(endCharacter);
		str.append("<BLOCKQUOTE style=\"border-left:1px solid black; padding-left:1px;\">origin<BR>").append(endCharacter);
		str.append("</BLOCKQUOTE>").append(endCharacter);
		str.append("</BODY>").append(endCharacter);
		str.append("</HTML>").append(endCharacter);
		return str.toString();
	}
	
}
