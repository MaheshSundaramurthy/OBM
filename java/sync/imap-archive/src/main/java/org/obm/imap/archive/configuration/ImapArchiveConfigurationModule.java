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

package org.obm.imap.archive.configuration;

import org.obm.configuration.ConfigurationModule;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.ConfigurationServiceImpl;
import org.obm.configuration.DatabaseConfigurationImpl;
import org.obm.configuration.DefaultTransactionConfiguration;
import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.GlobalAppConfiguration;
import org.obm.configuration.LocatorConfigurationImpl;
import org.obm.configuration.TransactionConfiguration;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;

public class ImapArchiveConfigurationModule extends AbstractModule {

	@VisibleForTesting static final String APPLICATION_NAME = "obm-imap-archive";
	private static final String GLOBAL_CONFIGURATION_FILE = ConfigurationService.GLOBAL_OBM_CONFIGURATION_PATH;
	public static final String IMAP_ARCHIVE_CONFIG_FILE_PATH = "/etc/obm-imap-archive/obm-imap-archive.ini";
	

	@Override
	protected void configure() {
		ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl.Factory()
			.create(GLOBAL_CONFIGURATION_FILE, APPLICATION_NAME);
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationService = imapArchiveConfigurationService(configurationService);
		
		final GlobalAppConfiguration<ConfigurationService> globalConfiguration = buildConfiguration(configurationService, imapArchiveConfigurationService);
		bind(ConfigurationService.class).toInstance(globalConfiguration.getConfiguration());
		install(new ConfigurationModule<ConfigurationService> (globalConfiguration, ConfigurationService.class));
		
		bind(ImapArchiveConfigurationService.class).toInstance(imapArchiveConfigurationService);
		
		ImapArchiveEmailConfigurationImpl emailConfiguration = new ImapArchiveEmailConfigurationImpl.Factory().create(IMAP_ARCHIVE_CONFIG_FILE_PATH);
		bind(EmailConfiguration.class).toInstance(emailConfiguration);
	}

	private ImapArchiveConfigurationServiceImpl imapArchiveConfigurationService(ConfigurationServiceImpl configurationService) {
		return new ImapArchiveConfigurationServiceImpl.Factory(new DefaultTransactionConfiguration.Factory().create(APPLICATION_NAME, configurationService)).create();
	}

	private GlobalAppConfiguration<ConfigurationService> buildConfiguration(ConfigurationService configurationService, TransactionConfiguration transactionConfiguration) {
		return GlobalAppConfiguration
				.<ConfigurationService> builder()
				.mainConfiguration(configurationService)
				.locatorConfiguration(
						new LocatorConfigurationImpl.Factory().create(GLOBAL_CONFIGURATION_FILE))
				.databaseConfiguration(
						new DatabaseConfigurationImpl.Factory().create(GLOBAL_CONFIGURATION_FILE))
				.transactionConfiguration(transactionConfiguration)
				.build();
	}

}
