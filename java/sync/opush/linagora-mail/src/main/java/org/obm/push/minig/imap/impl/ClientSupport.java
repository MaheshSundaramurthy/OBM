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

package org.obm.push.minig.imap.impl;

import java.io.InputStream;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLException;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.obm.push.mail.bean.FastFetch;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.IMAPHeaders;
import org.obm.push.mail.bean.InternalDate;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.NameSpaceInfo;
import org.obm.push.mail.bean.QuotaInfo;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.bean.UIDEnvelope;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.minig.imap.command.AppendCommand;
import org.obm.push.minig.imap.command.CapabilityCommand;
import org.obm.push.minig.imap.command.CreateCommand;
import org.obm.push.minig.imap.command.DeleteCommand;
import org.obm.push.minig.imap.command.ExpungeCommand;
import org.obm.push.minig.imap.command.ICommand;
import org.obm.push.minig.imap.command.ListCommand;
import org.obm.push.minig.imap.command.LoginCommand;
import org.obm.push.minig.imap.command.LsubCommand;
import org.obm.push.minig.imap.command.NamespaceCommand;
import org.obm.push.minig.imap.command.NoopCommand;
import org.obm.push.minig.imap.command.QuotaRootCommand;
import org.obm.push.minig.imap.command.RenameCommand;
import org.obm.push.minig.imap.command.SelectCommand;
import org.obm.push.minig.imap.command.StartIdleCommand;
import org.obm.push.minig.imap.command.StopIdleCommand;
import org.obm.push.minig.imap.command.SubscribeCommand;
import org.obm.push.minig.imap.command.UIDCopyCommand;
import org.obm.push.minig.imap.command.UIDFetchBodyStructureCommand;
import org.obm.push.minig.imap.command.UIDFetchEnvelopeCommand;
import org.obm.push.minig.imap.command.UIDFetchFastCommand;
import org.obm.push.minig.imap.command.UIDFetchFlagsCommand;
import org.obm.push.minig.imap.command.UIDFetchHeadersCommand;
import org.obm.push.minig.imap.command.UIDFetchInternalDateCommand;
import org.obm.push.minig.imap.command.UIDFetchMessageCommand;
import org.obm.push.minig.imap.command.UIDFetchPartCommand;
import org.obm.push.minig.imap.command.UIDNextCommand;
import org.obm.push.minig.imap.command.UIDSearchCommand;
import org.obm.push.minig.imap.command.UIDStoreCommand;
import org.obm.push.minig.imap.command.UIDThreadCommand;
import org.obm.push.minig.imap.command.UIDValidityCommand;
import org.obm.push.minig.imap.command.UnSubscribeCommand;
import org.obm.push.minig.imap.command.parser.BodyStructureParser;
import org.obm.push.minig.imap.tls.MinigTLSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSupport {

	private final static Logger logger = LoggerFactory.getLogger(ClientSupport.class);
	
	private final IoHandler handler;
	private IoSession session;
	private Semaphore lock;
	private List<IMAPResponse> lastResponses;
	private TagProducer tagsProducer;
	private MinigTLSFilter sslFilter;

	public ClientSupport(IoHandler handler) {
		this.lock = new Semaphore(1);
		this.handler = handler;
		this.tagsProducer = new TagProducer();
		this.lastResponses = Collections
				.synchronizedList(new LinkedList<IMAPResponse>());
	}

	private void lock() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("InterruptedException !!");
		}
	}


	public void login(String login, String password,
			SocketConnector connector, SocketAddress address,
			Boolean activateTLS) throws IMAPException {
		if (session != null && session.isConnected()) {
			throw new IllegalStateException(
					"Already connected. Disconnect first.");
		}

		lock(); // waits for "* OK IMAP4rev1 server...
		ConnectFuture cf = connector.connect(address, handler);
		cf.join();
		if (!cf.isConnected()) {
			lock.release();
			// This method call will throws the original exception  
			cf.getSession();
			// This should never occur
			throw new IMAPException("Cannot log into imap server");
		}
		session = cf.getSession();
		logger.debug("Connection established");
		if (activateTLS) {
			boolean tlsActivated = run(new StartTLSCommand());
			if (tlsActivated) {
				activateSSL();
			} else {
				logger.debug("TLS not supported by IMAP server.");
			}
		}
		logger.debug("Sending " + login + " login informations.");
		if (!run(new LoginCommand(login, password))) {
			throw new IMAPException("Cannot log into imap server");
		}
	}

	private void activateSSL() {
		try {
			sslFilter = new MinigTLSFilter();
			sslFilter.setUseClientMode(true);
			session.getFilterChain().addBefore(
					"org.apache.mina.common.ExecutorThreadModel", "tls",
					sslFilter);
			logger.debug("Network traffic with IMAP server will be encrypted. ");
		} catch (Throwable t) {
			logger.error("Error starting ssl", t);
		}
	}

	public void logout() {
		if (session != null) {
			if (sslFilter != null) {
				try {
					sslFilter.stopSSL(session);
				} catch (SSLException e) {
					logger.error("error stopping ssl", e);
				} catch (IllegalStateException ei) {
					logger.error("imap connection is already stop");
				}
			}
			session.close().join();
			session = null;
		}
	}

	private <T> T run(ICommand<T> cmd) {
		logger.debug(Integer.toHexString(hashCode()) + " CMD: "
				+ cmd.getClass().getName() + " Permits: "
				+ lock.availablePermits());
		// grab lock, this one should be ok, except on first call
		// where we might wait for cyrus welcome text.
		lock();
		try {
			cmd.execute(session, tagsProducer, lock, lastResponses);
			lock(); // this one should wait until this.setResponses is called
			cmd.responseReceived(lastResponses);
		} finally {
			lock.release();
		}

		return cmd.getReceivedData();
	}

	/**
	 * Called by MINA on message received
	 * 
	 * @param rs
	 */
	public void setResponses(List<IMAPResponse> rs) {
		for (IMAPResponse ir : rs) {
			logger.debug("S: " + ir.getPayload());
		}

		synchronized (lastResponses) {
			this.lastResponses.clear();
			this.lastResponses.addAll(rs);
		}
		lock.release();
	}

	public boolean select(String mailbox) {
		return run(new SelectCommand(mailbox));
	}
	
	public ListResult listSubscribed() {
	 	return run(new LsubCommand());
	}
	
	public ListResult listAll() {
		return run(new ListCommand());
	}

	public Set<String> capabilities() {
		return run(new CapabilityCommand());
	}

	public boolean noop() {
		return run(new NoopCommand());
	}

	public boolean create(String mailbox) {
		return run(new CreateCommand(mailbox));
	}

	public boolean delete(String mailbox) {
		return run(new DeleteCommand(mailbox));
	}

	public boolean rename(String mailbox, String newMailbox) {
		return run(new RenameCommand(mailbox, newMailbox));
	}

	public boolean subscribe(String mailbox) {
		return run(new SubscribeCommand(mailbox));
	}

	public boolean unsubscribe(String mailbox) {
		return run(new UnSubscribeCommand(mailbox));
	}

	public boolean append(String mailbox, InputStream in, FlagsList fl) {
		return run(new AppendCommand(mailbox, in, fl));
	}

	public void expunge() {
		run(new ExpungeCommand());
	}

	public QuotaInfo quota(String mailbox) {
		return run(new QuotaRootCommand(mailbox));
	}

	public InputStream uidFetchMessage(long uid) {
		return run(new UIDFetchMessageCommand(uid));
	}

	public MessageSet uidSearch(SearchQuery sq) {
		return run(new UIDSearchCommand(sq));
	}

	public Collection<MimeMessage> uidFetchBodyStructure(MessageSet messages) {
		return run(new UIDFetchBodyStructureCommand(new BodyStructureParser(), messages));
	}

	public Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids,
			String[] headers) {
		return run(new UIDFetchHeadersCommand(uids, headers));
	}

	public Collection<UIDEnvelope> uidFetchEnvelope(MessageSet messages) {
		return run(new UIDFetchEnvelopeCommand(messages));
	}

	public Map<Long, FlagsList> uidFetchFlags(MessageSet messages) {
		return run(new UIDFetchFlagsCommand(messages));
	}

	public InternalDate[] uidFetchInternalDate(Collection<Long> uids) {
		return run(new UIDFetchInternalDateCommand(uids));
	}
	
	public Collection<FastFetch> uidFetchFast(MessageSet messages) {
		return run(new UIDFetchFastCommand(messages));
	}

	public MessageSet uidCopy(MessageSet messages, String destMailbox) {
		return run(new UIDCopyCommand(messages, destMailbox));
	}

	public boolean uidStore(MessageSet messages, FlagsList fl, boolean set) {
		return run(new UIDStoreCommand(messages, fl, set));
	}

	public InputStream uidFetchPart(long uid, String address) {
		return run(new UIDFetchPartCommand(uid, address));
	}
	
	public InputStream uidFetchPart(long uid, String address, long truncation) {
		return run(new UIDFetchPartCommand(uid, address, truncation));
	}

	public List<MailThread> uidThreads() {
		// UID THREAD REFERENCES UTF-8 NOT DELETED
		return run(new UIDThreadCommand());
	}

	public NameSpaceInfo namespace() {
		return run(new NamespaceCommand());
	}

	public void startIdle() {
		run(new StartIdleCommand());
	}

	public void stopIdle() {
		run(new StopIdleCommand());
	}
	
	public boolean isConnected() {
		return session.isConnected();
	}
	
	public long uidNext(String mailbox) {
		return run(new UIDNextCommand(mailbox));
	}
	
	public long uidValidity(String mailbox) {
		return run(new UIDValidityCommand(mailbox));
	}
}
