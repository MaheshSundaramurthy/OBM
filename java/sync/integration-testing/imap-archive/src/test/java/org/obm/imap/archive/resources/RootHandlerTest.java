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

package org.obm.imap.archive.resources;

import static com.jayway.restassured.RestAssured.given;
import static org.obm.imap.archive.ExpectAuthorization.expectAdmin;
import static org.obm.imap.archive.ExpectAuthorization.expectSimpleUser;

import javax.ws.rs.core.Response.Status;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.domain.dao.DomainDao;
import org.obm.domain.dao.UserDao;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.Expectations;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.inject.Inject;
import com.google.inject.Provider;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class RootHandlerTest {

	private ClientDriverRule driver = new ClientDriverRule();
	
	@Rule public TestRule chain = RuleChain
			.outerRule(driver)
			.around(new TemporaryFolder())
			.around(new GuiceRule(this, new TestImapArchiveModules.Simple(driver, new Provider<TemporaryFolder>() {

				@Override
				public TemporaryFolder get() {
					return temporaryFolder;
				}
				
			})))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/initial.sql"));

	@Inject DomainDao domainDao;
	@Inject UserDao userDao;
	@Inject TemporaryFolder temporaryFolder;
	@Inject H2InMemoryDatabase db;
	@Inject WebServer server;
	@Inject IMocksControl control;
	Expectations expectations;
	
	@Before
	public void setUp() throws Exception {
		expectations = new Expectations(driver);
		server.start();
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void testStatusOk() {
		expectations.expectTrustedLogin(ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309"));
		
		expectAdmin(domainDao, "mydomain.org", userDao, "admin");
		
		control.replay();
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/status");
		
		control.verify();
	}
	
	@Test
	public void unauthorizedWhenNotAdmin() {
		expectations.expectTrustedLoginForUser(ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309"), "user");
		
		expectSimpleUser(domainDao, "mydomain.org", userDao, "user");
		
		control.replay();
		
		given()
			.port(server.getHttpPort())
			.auth().basic("user@mydomain.org", "trust3dToken").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/status");
		
		control.verify();
	}
	
	@Test
	public void testStatusUnauthorizedWhenBadCredentials() {
		expectations.expectTrustedLoginThrowAuthFault();
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken").
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/status");
	}
	
	@Test
	public void testStatusOkOnHealthcheckRoot() {
		given()
			.port(server.getHttpPort())
			.auth().basic("cyrus@mydomain.org", "cyrus").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/healthcheck/status");
	}
}
