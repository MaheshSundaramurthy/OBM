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
package org.obm.push.minig.imap.command;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.MapEntry.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.minig.imap.command.UIDFetchFlagsCommand;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.collect.Lists;


public class UIDFetchFlagsCommandTest {

	@Before
	public void setup() {
		
	}

	@Test
	public void validResponses() {
		UIDFetchFlagsCommand uidFetchFlagsCommand = new UIDFetchFlagsCommand(Arrays.asList(12l));
		uidFetchFlagsCommand.handleResponses(responses("* 98 FETCH (FLAGS (\\Seen) UID 12)"));
		Map<Long, FlagsList> receivedData = uidFetchFlagsCommand.getReceivedData();
		assertThat(receivedData).hasSize(1).contains(entry(12l, new FlagsList(Arrays.asList(Flag.SEEN))) );
	}
	
	@Test
	public void severalUntaggedResponses() {
		UIDFetchFlagsCommand uidFetchFlagsCommand = new UIDFetchFlagsCommand(Arrays.asList(12l));
		uidFetchFlagsCommand.handleResponses(
				responses(
						"* 98 FETCH (FLAGS (\\Seen) UID 12)", 
						"* 98 FETCH (FLAGS (\\Seen) UID 13)"));
		Map<Long, FlagsList> receivedData = uidFetchFlagsCommand.getReceivedData();
		assertThat(receivedData).hasSize(1).contains(entry(12l, new FlagsList(Arrays.asList(Flag.SEEN))));
	}
	
	@Test
	public void severalIdenticalUntaggedResponses() {
		UIDFetchFlagsCommand uidFetchFlagsCommand = new UIDFetchFlagsCommand(Arrays.asList(12l));
		uidFetchFlagsCommand.handleResponses(
				responses(
						"* 98 FETCH (FLAGS (\\Seen) UID 12)", 
						"* 98 FETCH (FLAGS (\\Seen) UID 12)"));
		Map<Long, FlagsList> receivedData = uidFetchFlagsCommand.getReceivedData();
		assertThat(receivedData).hasSize(1).contains(entry(12l, new FlagsList(Arrays.asList(Flag.SEEN))));
	}
	
	private List<IMAPResponse> responses(String... lines) {
		List<IMAPResponse> responses = Lists.newArrayList();
		for (String line: lines) {
			responses.add(new IMAPResponse("*", line));
		}
		responses.add(new IMAPResponse("OK", null));
		return responses;
	}

}
