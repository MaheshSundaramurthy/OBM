/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.domain.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.ProfileName;
import org.obm.sync.host.ObmHost;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(UserDaoJdbcImplTest.Env.class)
public class UserDaoJdbcImplTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(Names.named("initialSchema")).to("sql/initial.sql");

			bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
			bind(DomainDao.class);
		}

	}

	@Inject
	private UserDao dao;

	@Rule
	@Inject
	public H2InMemoryDatabase db;

	private final ObmDomain domain = ObmDomain
			.builder()
			.id(1)
			.uuid(ObmDomainUuid.of("3af47236-3638-458e-9c3e-5eebbaa8f9ae"))
			.name("domain")
			.build();
	private final ObmHost mailHost = ObmHost
			.builder()
			.id(1)
			.name("mail")
			.fqdn("mail.tlse.lng")
			.ip("1.2.3.4")
			.domainId(domain.getId())
			.build();

	@Test
	public void testList() throws Exception {
		List<ObmUser> users = ImmutableList.of(
				sampleUser(1, 3),
				sampleUser(2, 4),
				sampleUser(3, 5),
				sampleUserWithoutMail(4, 6));

		assertThat(dao.list(domain)).isEqualTo(users);
	}

	@Test
	public void testFindUserById() {
		ObmUser user = sampleUser(1, 3);

		assertThat(dao.findUserById(1, domain)).isEqualTo(user);
	}

	@Test
	public void testFindUserByIdWhenUserHasNoMail() {
		ObmUser user = sampleUserWithoutMail(4, 6);

		assertThat(dao.findUserById(4, domain)).isEqualTo(user);
	}

	private ObmUser.Builder sampleUserBuilder(int id, int entityId) {
		return ObmUser
				.builder()
				.login("user" + id)
				.uid(id)
				.entityId(entityId)
				.lastName("Lastname")
				.firstName("Firstname")
				.commonName("")
				.domain(domain)
				.profileName(ProfileName.builder().name("user").build())
				.password("user" + id)
				.countryCode("0");
	}
	private ObmUser sampleUser(int id, int entityId) {
		return sampleUserBuilder(id, entityId)
				.mailHost(mailHost)
				.emailAndAliases("user" + id)
				.build();
	}

	private ObmUser sampleUserWithoutMail(int id, int entityId) {
		return sampleUserBuilder(id, entityId)
				.emailAndAliases("")
				.build();
	}
}
