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

import static org.easymock.EasyMock.createMock;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.protocol.data.MSEmailEncoder;
import org.obm.push.protocol.data.TimeZoneConverter;
import org.obm.push.protocol.data.TimeZoneEncoder;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.IntEncoder;
import org.obm.push.utils.SerializableInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Charsets;

@RunWith(SlowFilterRunner.class)
public class MSEmailEncoderTest {
	
	private MSEmailEncoder msEmailEncoder;

	@Before
	public void setup(){
		IntEncoder intEncoder = createMock(IntEncoder.class);
		TimeZoneEncoder timeZoneEncoder = createMock(TimeZoneEncoder.class);
		TimeZoneConverter timeZoneConverter = createMock(TimeZoneConverter.class);
		
		msEmailEncoder = new MSEmailEncoder(intEncoder, timeZoneEncoder, timeZoneConverter);
	}
	
	@Test
	public void testBodyTagsOrder() throws IOException, TransformerException {
		Document reply = DOMUtils.createDoc(null, "Sync");
		Element root = reply.getDocumentElement();
		
		msEmailEncoder.encode(root, applicationData("text", MSEmailBodyType.PlainText));
		String result = DOMUtils.serialize(reply);
		
		String expectedTagsOrder = "<AirSyncBase:Type>1</AirSyncBase:Type>" +
				"<AirSyncBase:Truncated>1</AirSyncBase:Truncated>" +
				"<AirSyncBase:EstimatedDataSize>10</AirSyncBase:EstimatedDataSize>" +
				"<AirSyncBase:Data>";
		assertThat(result).contains(expectedTagsOrder);
	}

	private MSEmail applicationData(String message, MSEmailBodyType emailBodyType) {
		return MSEmail.builder()
			.uid(1l)
			.header(MSEmailHeader.builder().build())
			.body(MSEmailBody.builder()
					.mimeData(new SerializableInputStream(new ByteArrayInputStream(message.getBytes())))
					.bodyType(emailBodyType)
					.estimatedDataSize(10)
					.charset(Charsets.UTF_8)
					.truncated(true)
					.build())
			.build();
	}
}
