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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.minig.imap.mime.IMimePart;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class FetchInstructionTest {

	@Test
	public void testHasMimePartAddressDefined() {
		IMimePart mimePart = createStrictMock(IMimePart.class);
		expect(mimePart.getAddress()).andReturn(new MimeAddress("address"));
		
		replay(mimePart);
		
		FetchInstruction fetchInstruction = createFetchInstruction(100, mimePart);
		assertThat(fetchInstruction.hasMimePartAddressDefined()).isTrue();
		
		verify(mimePart);
	}

	@Test
	public void testHasNoMimePartAddressDefined() {
		IMimePart mimePart = createStrictMock(IMimePart.class);
		expect(mimePart.getAddress()).andReturn(null);
		
		replay(mimePart);
		
		FetchInstruction fetchInstruction = createFetchInstruction(100, mimePart);
		assertThat(fetchInstruction.hasMimePartAddressDefined()).isFalse();
		
		verify(mimePart);
	}

	@Test
	public void testMustTruncate() {
		IMimePart mimePart = createStrictMock(IMimePart.class);
		expect(mimePart.getSize()).andReturn(10000);
		
		replay(mimePart);
		
		FetchInstruction fetchInstruction = createFetchInstruction(100, mimePart);
		assertThat(fetchInstruction.mustTruncate()).isTrue();
		
		verify(mimePart);
	}

	@Test
	public void testNullTruncation() {
		IMimePart mimePart = createStrictMock(IMimePart.class);
		
		replay(mimePart);
		
		FetchInstruction fetchInstruction = createFetchInstruction(null, mimePart);
		assertThat(fetchInstruction.mustTruncate()).isFalse();
		
		verify(mimePart);
	}

	@Test
	public void testNotTruncated() {
		IMimePart mimePart = createStrictMock(IMimePart.class);
		expect(mimePart.getSize()).andReturn(10);
		
		replay(mimePart);
		
		FetchInstruction fetchInstruction = createFetchInstruction(100, mimePart);
		assertThat(fetchInstruction.mustTruncate()).isFalse();
		
		verify(mimePart);
	}

	private FetchInstruction createFetchInstruction(Integer truncation, IMimePart mimePart) {
		return FetchInstruction.builder()
			.truncation(truncation)
			.mimePart(mimePart)
			.mailTransformation(MailTransformation.NONE)
			.build();
	}
}
