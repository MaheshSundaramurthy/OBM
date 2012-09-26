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
package org.obm.sync.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.obm.locator.LocatorClientException;
import org.obm.sync.XTrustProvider;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.exception.ObmSyncClientException;
import org.obm.sync.client.exception.SIDNotFoundException;
import org.obm.sync.locators.Locator;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public abstract class AbstractClientImpl {

	static {
		XTrustProvider.install();
	}
	
	private static final int MAX_CONNECTIONS = 8;
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private final Logger obmSyncLogger;
	protected final SyncClientException exceptionFactory;
	protected HttpClient hc;
	
	private static final HttpMethodRetryHandler retryH = new HttpMethodRetryHandler() {
			public boolean retryMethod(HttpMethod arg0, IOException arg1, int arg2) {
				return false;
			}
		};

	protected abstract Locator getLocator();

	protected static HttpClient createHttpClient() {
		MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = 
				new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxTotalConnections(MAX_CONNECTIONS);
		params.setDefaultMaxConnectionsPerHost(MAX_CONNECTIONS);
		multiThreadedHttpConnectionManager.setParams(params);
		HttpClient ret = new HttpClient(multiThreadedHttpConnectionManager);
		ret.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryH);
		return ret;
	}

	public AbstractClientImpl(SyncClientException exceptionFactory, Logger obmSyncLogger) {
		super();
		this.exceptionFactory = exceptionFactory;
		this.obmSyncLogger = obmSyncLogger;
		this.hc = createHttpClient();
	}

	protected Document execute(AccessToken token, String action, Multimap<String, String> parameters) {
		PostMethod pm = null;
		try {
			pm = getPostMethod(token, action);
			logRequest(action, parameters);
			InputStream is = executePostAndGetResultStream(pm, parameters);
			if (is != null) {
				Document document = DOMUtils.parse(is);
				logResponse(document);
				return document;
			} else {
				throw new ObmSyncClientException("An error occurs: cannot get the request result stream");
			}
		} catch (LocatorClientException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (SAXException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (FactoryConfigurationError e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} finally {
			releaseConnection(pm);
		}
	}

	private void logRequest(String action, Multimap<String, String> parameters) {
		obmSyncLogger.debug("action {}, request {}", action, parameters);
	}

	private void logResponse(Document document) {
		if (obmSyncLogger.isDebugEnabled()) {
			try {
				obmSyncLogger.debug("response {}", DOMUtils.prettySerialize(document));
			} catch (TransformerException e) {
				obmSyncLogger.debug("unparsable response");
			}
		}
	}

	protected void setToken(Multimap<String, String> parameters, AccessToken token) throws SIDNotFoundException {
		if (token != null) {
			if (token.getSessionId() != null) {
				parameters.put("sid", token.getSessionId());
			} else {
				throw new SIDNotFoundException(token);
			}
		}
	}

	protected Multimap<String, String> initParams(AccessToken at) {
		Multimap<String, String> m = ArrayListMultimap.create();
		try {
			setToken(m, at);
		} catch (SIDNotFoundException e) {
			logger.warn(e.getMessage(), e);
		}
		return m;
	}

	private InputStream executePostAndGetResultStream(PostMethod pm, Multimap<String, String> parameters) throws HttpException, IOException {
		InputStream is = null;
		setPostMethodParameters(pm, parameters);
		int httpResultStatus = hc.executeMethod(pm);
		if (isHttpStatusOK(httpResultStatus)) {
			is = pm.getResponseBodyAsStream();
		} else {
			logger.error("method failed:\n" + pm.getStatusLine() + "\n"
					+ pm.getResponseBodyAsString());
		}
		return is;
	}

	private boolean isHttpStatusOK(int httpResultStatus) {
		return httpResultStatus == HttpStatus.SC_OK;
	}

	private void setPostMethodParameters(PostMethod pm, Multimap<String, String> parameters) {
		for (Entry<String, String> entry: parameters.entries()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				pm.setParameter(entry.getKey(), entry.getValue());
			}
		}
	}

	protected void executeVoid(AccessToken at, String action, Multimap<String, String> parameters) {
		PostMethod pm = null; 
		try {
			pm = getPostMethod(at, action);
			executePostAndGetResultStream(pm, parameters);
		} catch (LocatorClientException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (HttpException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} finally {
			releaseConnection(pm);
		}
	}

	private String getBackendUrl(String loginAtDomain) throws LocatorClientException {
		Locator locator = getLocator();
		return locator.backendUrl(loginAtDomain);
	}

	private PostMethod getPostMethod(AccessToken at, String action) throws LocatorClientException {
		String backendUrl = getBackendUrl(at.getUserWithDomain());
		PostMethod pm = new PostMethod(backendUrl + action);
		pm.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		return pm;
	}

	private void releaseConnection(PostMethod pm) {
		if (pm != null) {
			pm.releaseConnection();
		}
	}

}
