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
package org.obm.servlet.filter.qos.it;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.CacheManager;

import org.apache.http.StatusLine;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.obm.PortNumber;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.servlet.filter.qos.handlers.BusinessKeyProvider;
import org.obm.servlet.filter.qos.util.AsyncServletRequestUtils;
import org.obm.servlet.filter.qos.util.SuspendingServletUtils;
import org.obm.servlet.filter.qos.util.server.QoSFilterTestModule;
import org.obm.servlet.filter.qos.util.server.SuspendingServlet;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@RunWith(SlowFilterRunner.class) @Slow
public class OnePerClientQoSSuspendRequestAndContinuationHandlerTest {
	
	private static class Configuration extends NPerClientQosSuspendConfiguration {
		@Override
		protected int getN() {
			return 1;
		}
	}
	
	@Rule
	public JUnitGuiceRule guiceModule = new JUnitGuiceRule(Configuration.class);
	
	@Inject @Named(org.obm.servlet.filter.qos.QoSFilterModule.CONCURRENT_REQUEST_INFO_STORE) CacheManager cacheManager; 
	@Inject @PortNumber int port;
	@Inject IMocksControl control;
	@Inject BusinessKeyProvider<String> businessKeyProvider;
	@Inject Server server;
	@Inject SuspendingServlet suspendingServlet;

	private AsyncServletRequestUtils async;

	private ExecutorService threadpool;

	private SuspendingServletUtils suspendingServletUtils;


	@Before
	public void setup() throws Exception {
		threadpool = Executors.newFixedThreadPool(12);
		async = new AsyncServletRequestUtils(threadpool, port, QoSFilterTestModule.SUSPENDING_SERVLET_NAME);
		suspendingServletUtils = new SuspendingServletUtils(suspendingServlet);
		server.start();
		System.out.println("test started");
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
		threadpool.shutdown();
		cacheManager.shutdown();
	}

	@Test
	public void suspendedContinuationIsNotTakenIntoAccount() throws InterruptedException, ExecutionException, TimeoutException {
		expect(businessKeyProvider.provideKey(anyObject(HttpServletRequest.class))).andReturn("sameKey").anyTimes();
		control.replay();

		Future<StatusLine> request1 = async.asyncHttpGet();
		System.out.println("waiting");
		suspendingServletUtils.waitingServletRequestHandling();
		Future<StatusLine> request2 = async.asyncHttpGet();
		boolean requestHandlingNotified = suspendingServletUtils.tryWaitingServletRequestHandling();
		suspendingServletUtils.unlockServerRequestHandling();
		suspendingServletUtils.unlockServerRequestHandling();
		StatusLine response1 = async.retrieveRequestStatus(request1);
		StatusLine response2 = async.retrieveRequestStatus(request2);

		assertThat(requestHandlingNotified).isTrue();
		assertThat(response1).isNotNull();
		assertThat(response2).isNotNull();
		assertThat(response1.getStatusCode()).isEqualTo(org.apache.http.HttpStatus.SC_OK);
		assertThat(response2.getStatusCode()).isEqualTo(org.apache.http.HttpStatus.SC_OK);
		control.verify();
	}

}