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
package org.obm.sync.client.login;

import javax.inject.Singleton;

import org.apache.http.client.HttpClient;
import org.obm.breakdownduration.bean.Watch;
import org.obm.configuration.DomainConfiguration;
import org.obm.configuration.module.LoggerModule;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.BreakdownGroups;
import org.obm.sync.Parameter;
import org.obm.sync.PasswordParameter;
import org.obm.sync.StringParameter;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.client.exception.SIDNotFoundException;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientAssert;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.UserPassword;

@Watch(BreakdownGroups.CLIENT_LOGIN)
public class LoginClient extends AbstractClientImpl implements LoginService {

	@Singleton
	public static class Factory {

		protected final String origin;
		protected final DomainConfiguration domainConfiguration;
		protected final SyncClientAssert syncClientAssert;
		protected final Locator locator;
		protected final Logger obmSyncLogger;

		@Inject
		protected Factory(@Named("origin")String origin,
				DomainConfiguration domainConfiguration,
				SyncClientAssert syncClientAssert, 
				Locator locator, 
				@Named(LoggerModule.OBM_SYNC)Logger obmSyncLogger) {
			
			this.origin = origin;
			this.domainConfiguration = domainConfiguration;
			this.syncClientAssert = syncClientAssert;
			this.locator = locator;
			this.obmSyncLogger = obmSyncLogger;
		}
		
		public LoginClient create(HttpClient httpClient) {
			return new LoginClient(origin, domainConfiguration, syncClientAssert, locator, obmSyncLogger, httpClient);
		}
	}
	
	private final Locator locator;
	private final String origin;
	private final DomainConfiguration domainConfiguration;

	protected LoginClient(@Named("origin")String origin,
			DomainConfiguration domainConfiguration,
			SyncClientAssert syncClientAssert, 
			Locator locator, 
			@Named(LoggerModule.OBM_SYNC)Logger obmSyncLogger, 
			HttpClient httpClient) {
		
		super(syncClientAssert, obmSyncLogger, httpClient);
		this.origin = origin;
		this.domainConfiguration = domainConfiguration;
		this.locator = locator;
	}
	
	@Override
	public AccessToken login(String loginAtDomain, UserPassword password) throws AuthFault {
		Multimap<String, Parameter> params = ArrayListMultimap.create();
		params.put("login", new StringParameter(loginAtDomain));
		params.put("password", new PasswordParameter(password));
		params.put("origin", new StringParameter(origin));

		AccessToken token = newAccessToken(loginAtDomain, origin);
		
		Document doc = execute(token, "/login/doLogin", params);
		exceptionFactory.checkLoginException(doc);
		
		return fillToken(token, doc);
	}

	private AccessToken fillToken(AccessToken token, Document doc) {
		Element root = doc.getDocumentElement();
		String email = DOMUtils.getElementText(root, "email");
		String displayname = DOMUtils.getElementText(root, "displayname");
		String sid = DOMUtils.getElementText(root, "sid");
		Element v = DOMUtils.getUniqueElement(root, "version");
		Element domain = DOMUtils.getUniqueElement(root, "domain");
		token.setDomain(getDomain(domain));
		token.setSessionId(sid);
		token.setVersion(getVersion(v));
		token.setUserEmail(email);
		token.setUserDisplayName(displayname);
		return token;
	}
	
	@Override
	public AccessToken trustedLogin(String loginAtDomain, UserPassword password) throws AuthFault {
		Multimap<String, Parameter> params = ArrayListMultimap.create();
		params.put("login", new StringParameter(loginAtDomain));
		params.put("password", new PasswordParameter(password));
		params.put("origin", new StringParameter(origin));

		AccessToken token = newAccessToken(loginAtDomain, origin);
		
		Document doc = execute(token, "/login/trustedLogin", params);
		exceptionFactory.checkLoginException(doc);
		
		return fillToken(token, doc);
	}

	private MavenVersion getVersion(Element v) {
		MavenVersion version = new MavenVersion();
		if (v != null) {
			version.setMajor(v.getAttribute("major"));
			version.setMinor(v.getAttribute("minor"));
			version.setRelease(v.getAttribute("release"));
		}
		return version;
	}

	@Override
	public AccessToken authenticate(String loginAtDomain, UserPassword password) throws AuthFault {
		AccessToken token = login(loginAtDomain, password);
		if (token == null || token.getSessionId() == null) {
			throw new AuthFault(loginAtDomain + " can't log on obm-sync. The username or password isn't valid");
		}
		return token;
	}

	@Override
	public boolean authenticateGlobalAdmin(String login, UserPassword password) throws AuthFault {
		ImmutableMultimap<String, Parameter> params = ImmutableMultimap.of(
				"login", new StringParameter(login), 
				"password", new PasswordParameter(password), 
				"origin", new StringParameter(origin));

		AccessToken token = newAccessToken(login, domainConfiguration.getGlobalDomain(), origin);
		
		Document doc = execute(token, "/login/authenticateGlobalAdmin", params);
		exceptionFactory.checkLoginException(doc);
		return Boolean.valueOf(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}

	@Override
	public boolean authenticateAdmin(String login, UserPassword password, String domainName) throws AuthFault {
		ImmutableMultimap<String, Parameter> params = ImmutableMultimap.of(
				"login", new StringParameter(login), 
				"password", new PasswordParameter(password), 
				"origin", new StringParameter(origin), 
				"domainName", new StringParameter(domainName));

		AccessToken token = newAccessToken(login, domainConfiguration.getGlobalDomain(), origin);
		
		Document doc = execute(token, "/login/authenticateAdmin", params);
		exceptionFactory.checkLoginException(doc);
		return Boolean.valueOf(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}
	
	@Override
	public void logout(AccessToken at) {
		try {
			Multimap<String, Parameter> params = ArrayListMultimap.create();
			setToken(params, at);
			executeVoid(at, "/login/doLogout", params);
		} catch (SIDNotFoundException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	private ObmDomain getDomain(Element domain) {
		return ObmDomain
				.builder()
				.uuid(ObmDomainUuid.of(domain.getAttribute("uuid")))
				.name(DOMUtils.getElementText(domain))
				.build();
	}
	
	private AccessToken newAccessToken(String loginAtDomain, String origin) {
		String[] splitLoginAtDomain = loginAtDomain.split("@", 2);
		if (splitLoginAtDomain.length > 1) {
			return newAccessToken(splitLoginAtDomain[0], splitLoginAtDomain[1], origin);
		} else {
			return newAccessToken(splitLoginAtDomain[0], null, origin); 
		}
	}

	private AccessToken newAccessToken(String login, String domain, String origin) {
		AccessToken token = new AccessToken(0, origin);
		ObmDomain obmDomain = ObmDomain
                				.builder()
                				.name(domain)
                				.build();

		token.setUserLogin(login);
		token.setDomain(obmDomain);
		
		return token;
	}

	
	@Override
	protected Locator getLocator() {
		return locator;
	}
	
}
