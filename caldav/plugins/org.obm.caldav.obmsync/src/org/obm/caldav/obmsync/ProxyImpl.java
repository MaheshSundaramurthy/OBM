/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.obmsync;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.provider.impl.AbstractObmSyncProvider;
import org.obm.caldav.obmsync.service.impl.CalendarService;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.share.Token;
import org.obm.sync.auth.AccessToken;

public class ProxyImpl implements IProxy {

	private AccessToken token;
	private String userId;
	private String calendar;
	private ICalendarService calendarService;
	private Log logger = LogFactory.getLog(getClass());

	public ProxyImpl() {
	}

	private void initService() {
		calendarService = new CalendarService(token, calendar, userId);
	}

	@Override
	public void login(Token token) {
		this.userId = token.getLoginAtDomain();
		this.calendar = token.getCalendarName();
		this.token = AbstractObmSyncProvider.login(userId, token.getPassword());
		if (this.token == null) {
			logger.warn("null token: " + userId + " " + token.getPassword());
		}
		String[] split = userId.split("@");
		this.token.setUser(split[0]);
		this.token.setDomain(split[1]);
		this.initService();
	}

	@Override
	public void logout() {
		AbstractObmSyncProvider.logout(token);
	}

	@Override
	public ICalendarService getCalendarService() {
		if (this.calendarService == null) {
			throw new RuntimeException("You must be logged");
		}
		return calendarService;
	}

	@Override
	public boolean validateToken(Token t) throws Exception {
		if (t == null) {
			return false;
		}
		this.login(t);

		if (this.token == null || this.token.getSessionId() == null
				|| "".equals(this.token.getSessionId())) {
			return false;
		}

		boolean hasRightsOnCalendar = calendarService
				.hasRightsOnCalendar(this.calendar);
		this.logout();
		return hasRightsOnCalendar;
	}

	@Override
	public String getETag() throws Exception {
		return calendarService.getLastUpdate();
	}
}
