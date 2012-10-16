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

package org.obm.configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DatabaseConfigurationImpl extends AbstractConfigurationService
        implements DatabaseConfiguration {

    private static final String DB_TYPE_KEY = "dbtype";
    private static final String DB_HOST_KEY = "host";
    private static final String DB_NAME_KEY = "db";
    private static final String DB_USER_KEY = "user";
    private static final String DB_PASSWORD_KEY = "password";
    private static final String DB_MAX_POOL_SIZE_KEY = "database-max-connection-pool-size";
    private static final String DB_PG_SSL = "database-postgres-ssl-enabled";
    private static final String DB_PG_SSL_NON_VALIDATING = "database-postgres-ssl-non-validating-factory";
    private static final int DB_MAX_POOL_SIZE_DEFAULT = 10;

    @Inject
    DatabaseConfigurationImpl() {
        super(GLOBAL_CONFIGURATION_FILE);
    }

    @Override
    public Integer getDatabaseMaxConnectionPoolSize() {
        return getIntValue(DB_MAX_POOL_SIZE_KEY, DB_MAX_POOL_SIZE_DEFAULT);
    }

    @Override
    public DatabaseSystem getDatabaseSystem() {
        return DatabaseSystem.valueOf(getStringValue(DB_TYPE_KEY).trim());
    }

    @Override
    public Boolean isPostgresSSLEnabled() {
        return getBooleanValue(DB_PG_SSL);
    }

    @Override
    public String getDatabaseName() {
        return getStringValue(DB_NAME_KEY);
    }

    @Override
    public String getDatabaseHost() {
        return getStringValue(DB_HOST_KEY);
    }

    @Override
    public String getDatabaseLogin() {
        return getStringValue(DB_USER_KEY);
    }

    @VisibleForTesting String removeEnclosingDoubleQuotes(String toUnquote)
    {
        return toUnquote.replaceAll("^\"(.+)\"$", "$1");
    }

    @Override
    public String getDatabasePassword() {
        return removeEnclosingDoubleQuotes(getStringValue(DB_PASSWORD_KEY));
    }

    @Override
    public Boolean isPostgresSSLNonValidating() {
        return getBooleanValue(DB_PG_SSL_NON_VALIDATING);
    }
}
