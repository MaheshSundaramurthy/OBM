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
package fr.aliacom.obm.ldap;

/**
 * Un annuaire ldap pour l'authentification
 */
public class LDAPDirectory {

	private String uri;

	private String userPattern;

	private String rootDN;

	private String rootPW;

	private String matchField;

	private String baseDN;

	private String obmDomain;

	public LDAPDirectory(String uri, String userPattern, String rootDN,
			String rootPW, String baseDN, String matchField, String obmDomain) {
		this.uri = uri;
		this.userPattern = userPattern;
		this.rootDN = rootDN;
		this.rootPW = rootPW;
		this.baseDN = baseDN;
		this.matchField = matchField;
		this.obmDomain = obmDomain;

	}

	public String getUri() {
		return uri;
	}

	public String getUserPattern() {
		return userPattern;
	}

	public String getRootDN() {
		return rootDN;
	}

	public String getRootPW() {
		return rootPW;
	}

	public String getBaseDN() {
		return baseDN;
	}

	public String getMatchField() {
		return matchField;
	}

	public String getObmDomain() {
		return obmDomain;
	}

}
