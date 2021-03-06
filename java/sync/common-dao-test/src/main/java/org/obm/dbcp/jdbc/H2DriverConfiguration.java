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

package org.obm.dbcp.jdbc;

import java.sql.SQLException;
import java.util.Map;

import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.DatabaseFlavour;

import com.google.common.collect.ImmutableMap;

public class H2DriverConfiguration implements DatabaseDriverConfiguration {

	@Override
	public String getLastInsertIdQuery() {
		return "SELECT lastval()";
	}

	@Override
	public String getDataSourceClassName() {
		return "org.h2.jdbcx.JdbcDataSource";
	}

	@Override
	public String getNonXADataSourceClassName() {
		return "org.h2.jdbcx.JdbcDataSource";
	}

	@Override
	public DatabaseFlavour getFlavour() {
		return DatabaseFlavour.H2;
	}
	
	@Override
	public Map<String, String> getDriverProperties(DatabaseConfiguration conf) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		builder.put("user", conf.getDatabaseLogin());
		builder.put("password", conf.getDatabasePassword());
		builder.put("URL", getJDBCUrl(conf.getDatabaseName(), conf.getJdbcOptions()));
		return builder.build();
	}

	private String getJDBCUrl(String dbName, String jdbcOptions) {
		return "jdbc:h2:mem:" + dbName + ";TRACE_LEVEL_SYSTEM_OUT=2" + jdbcOptions;
	}
	
	@Override
	public boolean readOnlySupported() {
		return false;
	}

	@Override
	public String getGMTTimezoneQuery() {
		return null;
	}

	@Override
	public String getIntegerCastType() {
		return "INTEGER";
	}
	
	@Override
	public Object getJDBCObject(String type, String value) throws SQLException {
		return value;
	}
}
