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
package org.obm.annotations.transactional;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.config.Configuration;
import org.hornetq.jms.server.config.JMSConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.linagora.obm.sync.HornetQConfiguration;
import com.linagora.obm.sync.JMSClient;
import com.linagora.obm.sync.JMSServer;
import com.linagora.obm.sync.Producer;

public class MessageQueueModule extends AbstractModule {

	private final String TOPIC = "test";
	private final JMSServer jmsServer;
	
	public MessageQueueModule() {
		super();
		this.jmsServer = new JMSServer(hornetQConfiguration(), jmsConfiguration());
	}

	@Override
	protected void configure() {
		bind(JMSServer.class).toInstance(jmsServer);
		bind(Boolean.class).annotatedWith(Names.named("queueIsRemote")).toInstance(false);
	}
	
	public static Configuration hornetQConfiguration() {
		return HornetQConfiguration.configuration()
				.enablePersistence(true)
				.enableSecurity(false)
				.connector(HornetQConfiguration.Connector.HornetQInVMCore)
				.acceptor(HornetQConfiguration.Acceptor.HornetQInVMCore)
				.build();
	}
	
	private JMSConfiguration jmsConfiguration() {
		return 
				HornetQConfiguration.jmsConfiguration()
				.connectionFactory(
						HornetQConfiguration.connectionFactoryConfigurationBuilder()
						.name("ConnectionFactory")
						.connector(HornetQConfiguration.Connector.HornetQInVMCore)
						.binding("ConnectionFactory")
						.binding("XAConnectionFactory")
						.factoryType(JMSFactoryType.XA_CF)
						.build())
				.topic(TOPIC, "/topic/test")
				.build();
	}
	
	@Provides @Singleton
	MessageConsumer provideMessageConsumer(JMSClient client) throws JMSException {
		Connection connection = client.createConnection();
		connection.start();
		Session consumerSession = client.createSession(connection);
		return client.createConsumerOnTopic(consumerSession, TOPIC);
	}
	
	@Provides @Singleton
	Producer provideMessageProducer(JMSClient queueManager) throws JMSException {
		Connection connection = queueManager.createConnection();
		Session session = queueManager.createSession(connection);
		return queueManager.createProducerOnTopic(session, TOPIC);
	}

	
}
