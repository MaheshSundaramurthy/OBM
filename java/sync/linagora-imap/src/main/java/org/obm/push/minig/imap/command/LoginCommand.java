/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class LoginCommand extends SimpleCommand<Boolean> {

	private final String passwordFilteredCommand;
	
	public LoginCommand(String login, char[] password) {
		super("LOGIN \"" + escapeString(login) + "\" \"" + escapeString(String.valueOf(password))+"\"");
		passwordFilteredCommand = "LOGIN \"" + escapeString(login) + "\" \"***\"";
	}
	
	private static String escapeString(String s) {
		StringBuilder ret = new StringBuilder(48);
		char[] toEsc = s.toCharArray();
		for (char c : toEsc) {
			if (c == '\\' || c == '"' ) {
				ret.append('\\');
			}
			ret.append(c);
		}
		return ret.toString();
	}

	@Override
	protected String commandToBeLogged(String sent) {
		String requestId = Iterables.getFirst(Splitter.on(" ").split(sent), "");
		return requestId + " " + passwordFilteredCommand;
	}

	@Override
	public String getImapCommand() {
		return passwordFilteredCommand;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		data = response.isOk();
	}
	
	@Override
	public void setDataInitialValue() {
		data = false;
	}
}
