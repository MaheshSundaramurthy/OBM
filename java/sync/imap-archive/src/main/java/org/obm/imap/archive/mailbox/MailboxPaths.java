/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.mailbox;

import java.util.Iterator;
import java.util.List;

import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.sync.base.DomainName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MailboxPaths {

	public static final char IMAP_FOLDER_SEPARATOR = '/';
	public static final String AT = "@";
	public static final String INBOX = "INBOX";
	
	public static MailboxPaths from(String mailbox) throws MailboxFormatException {
		return parse(mailbox, null, new Builder());
	}
	
	public static class Builder {
		
		private String mainPath;
		private String user;
		private String subPaths;
		private DomainName domainName;
		
		@VisibleForTesting Builder() {
		}
		
		public Builder mainPath(String mainPath) {
			Preconditions.checkNotNull(mainPath);
			this.mainPath = mainPath;
			return this;
		}
		
		public Builder user(String user) {
			Preconditions.checkNotNull(user);
			this.user = user;
			return this;
		}
		
		public Builder subPaths(String subPaths) {
			Preconditions.checkNotNull(subPaths);
			this.subPaths = subPaths;
			return this;
		}
		
		public Builder domainName(DomainName domainName) {
			Preconditions.checkNotNull(domainName);
			this.domainName = domainName;
			return this;
		}
	
		public MailboxPaths build() {
			Preconditions.checkState(!Strings.isNullOrEmpty(mainPath));
			Preconditions.checkState(!Strings.isNullOrEmpty(user));
			Preconditions.checkState(!Strings.isNullOrEmpty(subPaths));
			Preconditions.checkState(domainName != null);
			return new MailboxPaths(mainPath, user, subPaths, domainName);
		}
	}

	@VisibleForTesting static MailboxPaths parse(String mailbox, String mainSubPath, Builder builder) throws MailboxFormatException {
		Iterator<String> split = Splitter.on(IMAP_FOLDER_SEPARATOR).split(mailbox).iterator();
		builder.mainPath(nextMandatoryElement(split, mailbox));
		
		String userPart = nextMandatoryElement(split, mailbox);
		if (userPart.contains(AT)) {
			Iterator<String> splitPathAtDomain = splitPathAtDomain(userPart);
			builder.user(nextMandatoryElement(splitPathAtDomain, mailbox));
			builder.subPaths(inbox(mainSubPath));
			builder.domainName(new DomainName(nextMandatoryElement(splitPathAtDomain, mailbox)));
		} else {
			parseMailboxWithSubPaths(mailbox, mainSubPath, userPart, builder, split);
		}
		return builder.build();
	}

	private static void parseMailboxWithSubPaths(String mailbox, String mainSubPath, String userPart, Builder builder, Iterator<String> split) throws MailboxFormatException {
		builder.user(userPart);
		
		List<String> subPaths = Lists.newArrayList();
		if (!Strings.isNullOrEmpty(mainSubPath)) {
			subPaths.add(mainSubPath);
		}
		
		while (split.hasNext()) {
			String subPath = split.next();
			if (subPath.contains(AT)) {
				Iterator<String> splitPathAtDomain = splitPathAtDomain(subPath);
				subPaths.add(nextMandatoryElement(splitPathAtDomain, mailbox));
				builder.domainName(new DomainName(nextMandatoryElement(splitPathAtDomain, mailbox)));
				break;
			}
			subPaths.add(subPath);
		}
		builder.subPaths(Joiner.on(IMAP_FOLDER_SEPARATOR).join(subPaths));
	}

	private static String inbox(String mainSubPath) {
		if (!Strings.isNullOrEmpty(mainSubPath)) {
			return Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainSubPath, INBOX);
		}
		return INBOX;
	}

	private static Iterator<String> splitPathAtDomain(String pathAtDomain) {
		return Splitter.on(AT).split(pathAtDomain).iterator();
	}
	
	private static String nextMandatoryElement(Iterator<String> iterator, String mailbox) throws MailboxFormatException {
		if (!iterator.hasNext()) {
			throw new MailboxFormatException(mailbox);
		}
		return iterator.next();
	}
	
	private final String mainPath;
	private final String user;
	private final String subPaths;
	private final DomainName domainName;
	
	protected MailboxPaths(String mainPath, String user, String subPaths, DomainName domainName) {
		this.mainPath = mainPath;
		this.user = user;
		this.subPaths = subPaths;
		this.domainName = domainName;
	}
	
	public String getMainPath() {
		return mainPath;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getSubPaths() {
		return subPaths;
	}
	
	public DomainName getDomainName() {
		return domainName;
	}
	
	public String getName() {
		if (subPaths != null) {
			return Joiner.on(AT).join(Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainPath, user, subPaths), domainName.get());
		}
		return Joiner.on(AT).join(Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainPath, user), domainName.get());
	}
	
	public String getUserAtDomain() {
		return Joiner.on(AT).join(user, domainName.get());
	}
	
	public MailboxPaths prepend(String mainSubPath) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(mainSubPath));
		return new Builder()
			.mainPath(getMainPath())
			.user(getUser())
			.subPaths(Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainSubPath, getSubPaths()))
			.domainName(getDomainName())
			.build();
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(mainPath, user, subPaths, domainName);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof MailboxPaths) {
			MailboxPaths that = (MailboxPaths) object;
			return Objects.equal(this.mainPath, that.mainPath)
				&& Objects.equal(this.user, that.user)
				&& Objects.equal(this.subPaths, that.subPaths)
				&& Objects.equal(this.domainName, that.domainName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("mainPath", mainPath)
			.add("user", user)
			.add("subPaths", subPaths)
			.add("domainName", domainName)
			.toString();
	}
}
