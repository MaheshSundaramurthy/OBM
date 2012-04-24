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
package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.IDBCP;
import org.obm.push.bean.Device;
import org.obm.push.bean.Device.Factory;
import org.obm.push.bean.User;
import org.obm.push.exception.DaoException;
import org.obm.push.store.DeviceDao;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DeviceDaoJdbcImpl extends AbstractJdbcImpl implements DeviceDao {

	private Factory deviceFactory;

	@Inject
	private DeviceDaoJdbcImpl(IDBCP dbcp, Device.Factory deviceFactory) {
		super(dbcp);
		this.deviceFactory = deviceFactory;
	}

	@Override
	public Device getDevice(User user, String deviceId, String userAgent) 
			throws DaoException {
	
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT id, identifier, type FROM opush_device "
					+ "INNER JOIN UserObm ON owner=userobm_id "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE identifier=? AND lower(userobm_login)=? AND lower(domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, user.getLogin());
			ps.setString(3, user.getDomain());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer databaseId = rs.getInt("id");
				String devId = rs.getString("identifier");
				String devType = rs.getString("type");
				
				return deviceFactory.create(databaseId, devType, userAgent, devId);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}

	public boolean registerNewDevice(User user, String deviceId,
			String deviceType) throws DaoException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_device (identifier, type, owner) "
					+ "SELECT ?, ?, userobm_id FROM UserObm "
					+ "INNER JOIN Domain ON userobm_domain_id=domain_id "
					+ "WHERE lower(userobm_login)=? AND lower(domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, deviceType);
			ps.setString(3, user.getLogin());
			ps.setString(4, user.getDomain());
			return ps.executeUpdate() != 0;
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}
	
	public boolean syncAuthorized(User user, String deviceId) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean hasSyncPerm = false;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT policy FROM opush_sync_perms "
					+ "INNER JOIN UserObm u ON owner=userobm_id "
					+ "INNER JOIN Domain d ON userobm_domain_id=domain_id "
					+ "INNER JOIN opush_device od ON device_id=id "
					+ "WHERE od.identifier=? AND lower(u.userobm_login)=? AND lower(d.domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, user.getLogin());
			ps.setString(3, user.getDomain());

			rs = ps.executeQuery();
			if (rs.next()) {
				hasSyncPerm = true;
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		if (!hasSyncPerm) {
			logger.info(user
					+ " isn't authorized to synchronize in OBM-UI");
		}
		return hasSyncPerm;
	}
}
