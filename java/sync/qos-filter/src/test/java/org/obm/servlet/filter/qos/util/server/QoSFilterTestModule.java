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
package org.obm.servlet.filter.qos.util.server;

import org.eclipse.jetty.continuation.ContinuationFilter;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.thread.QueuedThreadPool;
import org.obm.servlet.filter.qos.QoSFilter;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;

public class QoSFilterTestModule extends ServletModule {
	
	public static final String BLOCKING_SERVLET_NAME = "blocking";
	public static final String SUSPENDING_SERVLET_NAME = "suspending";
	
	@Provides @Singleton
	protected EmbeddedServer buildServerWithModules() {
		final Server server = new Server();
		server.setThreadPool(new QueuedThreadPool(5));
		final SelectChannelConnector selectChannelConnector = new SelectChannelConnector();
		server.setConnectors(new Connector[] {selectChannelConnector});
		Context root = new Context(server, "/", Context.SESSIONS);
		root.addFilter(GuiceFilter.class, "/*", 0);
		root.addServlet(DefaultServlet.class, "/");
		return new EmbeddedServer() {
			
			@Override
			public void stop() throws Exception {
				server.stop();
			}
			
			@Override
			public void start() throws Exception {
				server.start();
			}
			
			@Override
			public int getPort() {
				return selectChannelConnector.getLocalPort();
			}
		};
	}

	@Override
	protected void configureServlets() {
		
		super.configureServlets();

		install(new org.obm.servlet.filter.qos.QoSFilterModule());
		
		filter("/*").through(new ContinuationFilter());
		filter("/*").through(QoSFilter.class);
		serve("/" + BLOCKING_SERVLET_NAME).with(BlockingServlet.class);
		serve("/" + SUSPENDING_SERVLET_NAME).with(SuspendingServlet.class);
	}
}
