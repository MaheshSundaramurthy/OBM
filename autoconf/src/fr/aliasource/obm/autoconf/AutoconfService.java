package fr.aliasource.obm.autoconf;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPException;

import fr.aliasource.obm.utils.ConstantService;

/**
 * @author nicolasl
 * 
 */
public class AutoconfService extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3346961272290678959L;
	private Log logger;

	public AutoconfService() {
		logger = LogFactory.getLog(getClass());
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String reqString = req.getRequestURI();
		logger.info("AutoconfService : reqString: '" + reqString);

		// get user from reqString : format ".../autoconfiguration/login"
		String login = reqString.substring(reqString.lastIndexOf("/") + 1);
		String domain = null;
		logger.info("login : '" + login + "'");
		if (login.indexOf("@") > 0) {
			String[] splitted = login.split("@");
			login = splitted[0];
			domain = splitted[1];
		}

		DBQueryTool dbqt = new DBQueryTool();

		HashMap<String, String> hostIps = null;
		try {
			hostIps = dbqt.getDBInformation(login, loadDomain(dbqt, login,
					domain));
		} catch (Exception e) {
			logger.error("Cannot contact DB:" + e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		if (hostIps == null) {
			logger.warn("NULL information obtained from DBQueryTool for "
					+ login);
			return;
		}

		if (hostIps.get("imap") == null || hostIps.get("smtp") == null) {
			logger.warn("No hosts found for  " + login);
			return;
		}

		// map host ip with imap host and smtp host

		String imapMailHost = hostIps.get("imap");
		String smtpMailHost = hostIps.get("smtp");
		String ldapHost = ConstantService.getInstance().getStringValue(
				"ldapHostname");
		String allowedAtt = ConstantService.getInstance().getStringValue(
				"allowedAtt");
		String allowedValue = ConstantService.getInstance().getStringValue(
				"allowedValue");

		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z");
		resp.setHeader("Expires", formatter.format(new Date()));
		resp.setContentType("application/xml");

		DirectoryConfig dc = new DirectoryConfig(login, ConstantService
				.getInstance());
		LDAPQueryTool lqt = new LDAPQueryTool(dc);
		LDAPAttributeSet attributeSet;
		try {
			attributeSet = lqt.getLDAPInformations();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.LOCAL_ERROR) {
				logger
						.warn("NULL informations obtained from LDAPQueryTool for "
								+ login);
			} else {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			return;
		}

		TemplateLoader tl = new TemplateLoader(dc.getConfigXml(),
				ConstantService.getInstance());
		tl.applyTemplate(attributeSet, imapMailHost, smtpMailHost, ldapHost,
				resp.getOutputStream(), allowedAtt, allowedValue);
	}

	private String loadDomain(DBQueryTool dbqt, String login, String domain) {
		if (domain != null) {
			return domain;
		} else {
			return dbqt.getDomain(login);
		}
	}

}
