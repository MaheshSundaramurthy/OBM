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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.obm.push.mail.bean.FastFetch;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.ImapMessageSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * FAST
 *        Macro equivalent to: (FLAGS INTERNALDATE RFC822.SIZE)
 */
public class UIDFetchFastCommand extends Command<Collection<FastFetch>> {

	private ImapMessageSet imapMessageSet;
	DateFormat df;

	public UIDFetchFastCommand(MessageSet messages) {
		this.imapMessageSet = ImapMessageSet.wrap(messages);
		//22-Mar-2010 14:26:18 +0100
		df = new SimpleDateFormat("d-MMM-yyyy HH:mm:ss Z", Locale.ENGLISH);
	}

	@Override
	protected CommandArgument buildCommand() {

		StringBuilder sb = new StringBuilder();
		if (!imapMessageSet.isEmpty()) {
			sb.append("UID FETCH ");
			sb.append(imapMessageSet.asString());
			sb.append(" (FLAGS INTERNALDATE RFC822.SIZE)");
		} else {
			sb.append("NOOP");
		}
		String cmd = sb.toString();
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void handleResponses(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);
		
		if (imapMessageSet.isEmpty()) {
			data = ImmutableSet.of();
			return;
		}
		
		Builder<FastFetch> buildSet = ImmutableSet.builder();
		if (isOK) {
			Iterator<IMAPResponse> it = rs.iterator();
			for (int i = 0; it.hasNext() && i < imapMessageSet.size(); ) {
				org.obm.push.mail.bean.FastFetch.Builder builder = FastFetch.builder();
				IMAPResponse r = it.next();
				String payload = r.getPayload();
				if (!payload.contains(" FETCH (")) {
					logger.debug("not a fetch: {}", payload);
					continue;
				}
				
				builder.uid(getUid(payload));
				Date internalDate = getInternalDate(payload);
				if (internalDate == null) {
					logger.error("Failed to get internaldate in fetch response: {}", payload);
				}
				builder.internalDate(internalDate);
				builder.flags(getFlags(payload));
				builder.size(getSize(payload));
				buildSet.add(builder.build());
			}
		} else {
			IMAPResponse ok = rs.get(rs.size() - 1);
			logger.warn("error on fetch: {}", ok.getPayload());
		}
		
		data = buildSet.build();
	}

	private int getSize(String payload) {
		int uidIdx = payload.indexOf("RFC822.SIZE ") + "RFC822.SIZE ".length();
		int endUid = getIntEnd(payload, uidIdx);
		return Integer.parseInt(payload.substring(uidIdx, endUid));
	}

	private long getUid(String payload) {
		int uidIdx = payload.indexOf("UID ") + "UID ".length();
		int endUid = getIntEnd(payload, uidIdx);
		return Long.parseLong(payload.substring(uidIdx, endUid));
	}

	private int getIntEnd(String payload, int endUid) {
		while (Character.isDigit(payload.charAt(endUid))) {
			endUid++;
		}
		return endUid;
	}

	private Date getInternalDate(String payload) {
		int fidx = payload.indexOf("INTERNALDATE \"") + "INTERNALDATE \"".length();
		
		if (fidx == -1 + "INTERNALDATE \"".length()) {
			return null;
		}
		int endDate = payload.indexOf("\"", fidx);
		String internalDate = "";
		if (fidx > 0 && endDate >= fidx) {
			internalDate = payload.substring(fidx, endDate);
		} else {
			logger.error("Failed to get flags in fetch response: "
					+ payload);
		}
		return parseDate(internalDate);
	}
	
	private Set<Flag> getFlags(String payload) {
		int fidx = payload.indexOf("FLAGS (") + "FLAGS (".length();
		if (fidx == -1 + "FLAGS (".length()) {
			return new HashSet<Flag>(0);
		}
		
		int endFlags = payload.indexOf(")", fidx);
		String flags = "";
		if (fidx > 0 && endFlags >= fidx) {
			flags = payload.substring(fidx, endFlags);
		} else {
			logger.error("Failed to get flags in fetch response: "
					+ payload);
		}
		return parseFlags(flags);
	}
	
	private Date parseDate(String date) {
		try {
			return df .parse(date);
		} catch (ParseException e) {
			logger.error("Can't parse internal date["+date+"]");
		}
		return null;
	}
	
	private Set<Flag> parseFlags(String flags) {
		Set<Flag> flagsList = new HashSet<Flag>();
		if (flags.contains("\\Seen")) {
			flagsList.add(Flag.SEEN);
		}
		if (flags.contains("\\Flagged")) {
			flagsList.add(Flag.FLAGGED);
		}
		if (flags.contains("\\Deleted")) {
			flagsList.add(Flag.DELETED);
		}
		if (flags.contains("\\Answered")) {
			flagsList.add(Flag.ANSWERED);
		}
		return flagsList;
	}
	
}
