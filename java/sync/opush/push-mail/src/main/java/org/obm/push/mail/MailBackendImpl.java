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
package org.obm.push.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.Address;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Email;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncState;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.service.impl.MappingService;
import org.obm.push.store.EmailDao;
import org.obm.push.tnefconverter.TNEFConverterException;
import org.obm.push.tnefconverter.TNEFUtils;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.utils.Mime4jUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.CalendarType;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.ICalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.mail.util.QPDecoderStream;

@Singleton
public class MailBackendImpl implements MailBackend {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private final MailboxService mailboxService;
	private final Mime4jUtils mime4jUtils;
	private final ConfigurationService configurationService;
	private final ICalendar calendarClient;
	private final LoginService login;
	private final MappingService mappingService;
	private final EmailDao emailDao;
	private final EmailSync emailSync;
	private final CollectionPathHelper collectionPathHelper;

	@Inject
	/* package */ MailBackendImpl(MailboxService mailboxService, 
			@Named(CalendarType.CALENDAR) ICalendar calendarClient, 
			EmailDao emailDao, EmailSync emailSync,
			LoginService login, Mime4jUtils mime4jUtils, ConfigurationService configurationService,
			MappingService mappingService, CollectionPathHelper collectionPathHelper)  {

		this.mailboxService = mailboxService;
		this.emailDao = emailDao;
		this.emailSync = emailSync;
		this.mime4jUtils = mime4jUtils;
		this.configurationService = configurationService;
		this.calendarClient = calendarClient;
		this.login = login;
		this.mappingService = mappingService;
		this.collectionPathHelper = collectionPathHelper;
	}

	@Override
	public PIMDataType getPIMDataType() {
		return PIMDataType.EMAIL;
	}
	
	@Override
	public List<ItemChange> getHierarchyChanges(UserDataRequest udr) throws DaoException {
		return ImmutableList.of(
				genItemChange(udr, EmailConfiguration.IMAP_INBOX_NAME, FolderType.DEFAULT_INBOX_FOLDER),
				genItemChange(udr, EmailConfiguration.IMAP_DRAFTS_NAME, FolderType.DEFAULT_DRAFTS_FOLDERS),
				genItemChange(udr, EmailConfiguration.IMAP_SENT_NAME, FolderType.DEFAULT_SENT_EMAIL_FOLDER),
				genItemChange(udr, EmailConfiguration.IMAP_TRASH_NAME,FolderType.DEFAULT_DELETED_ITEMS_FOLDERS));
	}

	private ItemChange genItemChange(UserDataRequest udr, String imapFolder,
			FolderType type) throws DaoException {
		
		ItemChange ic = new ItemChangeBuilder().parentId("0").displayName(imapFolder).itemType(type).build();

		String imapPath = collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, imapFolder);
		String serverId;
		try {
			Integer collectionId = mappingService.getCollectionIdFor(udr.getDevice(), imapPath);
			serverId = mappingService.collectionIdToString(collectionId);
		} catch (CollectionNotFoundException e) {
			serverId = mappingService.createCollectionMapping(udr.getDevice(), imapPath);
			ic.setIsNew(true);
		}

		ic.setServerId(serverId);
		return ic;
	}

	private String getWasteBasketPath(UserDataRequest udr) {
		return collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_TRASH_NAME);
	}

	private MailChanges getSync(UserDataRequest udr, SyncState state, Integer collectionId, FilterType filterType) 
			throws ProcessingEmailException, CollectionNotFoundException {
		
		try {
			String collectionPath = mappingService.getCollectionPathFor(collectionId);
			state.updatingLastSync(filterType);
			return emailSync.getSync(udr, mailboxService, state, collectionPath, collectionId);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		}
	}
	
	@Override
	public int getItemEstimateSize(UserDataRequest udr, FilterType filterType,
			Integer collectionId, SyncState state)
			throws CollectionNotFoundException, ProcessingEmailException,
			DaoException, UnexpectedObmSyncServerException {
		MailChanges mailChanges = getSync(udr, state, collectionId, filterType);
		DataDelta dataDelta = getDataDelta(udr, collectionId, mailChanges);
		return dataDelta.getItemEstimateSize();
	}
	
	@Override
	public DataDelta getChanged(UserDataRequest udr, SyncState state,
			FilterType filterType, Integer collectionId) throws DaoException,
			CollectionNotFoundException, UnexpectedObmSyncServerException,
			ProcessingEmailException {
		
		MailChanges mailChanges = getSync(udr, state, collectionId, filterType);
		try {
			updateData(udr.getDevice().getDatabaseId(), collectionId, state.getLastSync(), 
					mailChanges.getRemovedEmailsUids(), mailChanges.getNewAndUpdatedEmails());
			return getDataDelta(udr, collectionId, mailChanges);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		}
	}

	private void updateData(Integer devId, Integer collectionId, Date lastSync, Collection<Long> removedEmailUids,
			Collection<Email> newAndUpdatedEmails) throws DaoException {
		
		if (removedEmailUids != null && !removedEmailUids.isEmpty()) {
			emailDao.deleteSyncEmails(devId, collectionId, lastSync, removedEmailUids);
		}
		
		if (newAndUpdatedEmails != null && !newAndUpdatedEmails.isEmpty()) {
			markEmailsAsSynced(devId, collectionId, lastSync, newAndUpdatedEmails);
		}
	}
	
	private DataDelta getDataDelta(UserDataRequest udr, Integer collectionId, MailChanges mailChanges) 
			throws ProcessingEmailException, CollectionNotFoundException, DaoException {
		
		List<ItemChange> itemChanges = fetchMails(udr, collectionId, 
				mappingService.getCollectionPathFor(collectionId), mailChanges.getNewEmailsUids());
		List<ItemChange> itemsToDelete = mappingService.buildItemsToDeleteFromUids(collectionId, mailChanges.getRemovedEmailsUids());
		return new DataDelta(itemChanges, itemsToDelete, mailChanges.getLastSync());
	}
	
	private List<ItemChange> fetchMails(
			UserDataRequest udr, Integer collectionId, String collection, 
			Collection<Long> emailsUids) throws ProcessingEmailException {
		
		ImmutableList.Builder<ItemChange> itch = ImmutableList.builder();
		try {
			List<MSEmail> msMails = 
					mailboxService.fetchMails(udr, collectionId, collection, emailsUids);
			for (MSEmail mail: msMails) {
				itch.add(getItemChange(collectionId, mail.getUid(), mail));
			}
			return itch.build();
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		}
	}
	
	private ItemChange getItemChange(Integer collectionId, Long uid, IApplicationData data) {
		ItemChange ic = new ItemChange();
		ic.setServerId(mappingService.getServerIdFor(collectionId, "" + uid));
		ic.setData(data);
		return ic;
	}
	
	@Override
	public List<ItemChange> fetch(UserDataRequest udr, List<String> itemIds)
			throws CollectionNotFoundException, DaoException,
			ProcessingEmailException, UnexpectedObmSyncServerException {
	
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		Map<Integer, Collection<Long>> emailUids = getEmailUidByCollectionId(itemIds);
		for (Entry<Integer, Collection<Long>> entry : emailUids.entrySet()) {
			Integer collectionId = entry.getKey();
			Collection<Long> uids = entry.getValue();
			try {
				ret.addAll(fetchItems(udr, collectionId, uids));
			} catch (CollectionNotFoundException e) {
				logger.error("fetchItems : collection {} not found !", collectionId);
			}
		}
		return ret;
	}
	
	private Map<Integer, Collection<Long>> getEmailUidByCollectionId(List<String> fetchIds) {
		Map<Integer, Collection<Long>> ret = Maps.newHashMap();
		for (String serverId : fetchIds) {
			Integer collectionId = mappingService.getCollectionIdFromServerId(serverId);
			Collection<Long> set = ret.get(collectionId);
			if (set == null) {
				set = Sets.newHashSet();
				ret.put(collectionId, set);
			}
			set.add(getEmailUidFromServerId(serverId));
		}
		return ret;
	}

	private List<ItemChange> fetchItems(UserDataRequest udr, Integer collectionId, Collection<Long> uids) 
			throws CollectionNotFoundException, ProcessingEmailException {
		
		try {
			final Builder<ItemChange> ret = ImmutableList.builder();
			final String collectionPath = mappingService.getCollectionPathFor(collectionId);
			final List<MSEmail> emails = mailboxService.fetchMails(udr, collectionId, collectionPath, uids);
			for (final MSEmail email: emails) {
				ItemChange ic = new ItemChange();
				ic.setServerId(mappingService.getServerIdFor(collectionId, String.valueOf(email.getUid())));
				ic.setData(email);
				ret.add(ic);
			}
			return ret.build();	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		}
	}

	@Override
	public void delete(UserDataRequest udr, Integer collectionId, String serverId, Boolean moveToTrash)
			throws CollectionNotFoundException, DaoException,
			UnexpectedObmSyncServerException, ItemNotFoundException, ProcessingEmailException, UnsupportedBackendFunctionException {
		try {
			boolean trash = Objects.firstNonNull(moveToTrash, true);
			if (trash) {
				logger.info("move to trash serverId {}", serverId);
			} else {
				logger.info("delete serverId {}", serverId);
			}
			if (serverId != null) {
				final Long uid = getEmailUidFromServerId(serverId);
				final String collectionName = mappingService.getCollectionPathFor(collectionId);
				final Integer devDbId = udr.getDevice().getDatabaseId();

				if (trash) {
					String wasteBasketPath = getWasteBasketPath(udr);
					Integer wasteBasketId = mappingService.getCollectionIdFor(udr.getDevice(), wasteBasketPath);
					mailboxService.moveItem(udr, collectionName, wasteBasketPath, uid);
					deleteEmails(devDbId, collectionId, Arrays.asList(uid));
					addMessageInCache(udr, devDbId, wasteBasketId, uid, wasteBasketPath);
				} else {
					mailboxService.delete(udr, collectionName, uid);
				}
			}	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ItemNotFoundException(e);
		}
	}


	protected String getDefaultCalendarName(UserDataRequest udr) {
		return "obm:\\\\" + udr.getUser().getLoginAtDomain() + "\\calendar\\"
				+ udr.getUser().getLoginAtDomain();
	}
	
	@Override
	public String createOrUpdate(UserDataRequest udr, Integer collectionId, String serverId, String clientId, IApplicationData data)
			throws CollectionNotFoundException, ProcessingEmailException, DaoException, ItemNotFoundException {
		
		MSEmail email = (MSEmail) data;
		try {
			String collectionPath = mappingService.getCollectionPathFor(collectionId);
			logger.info("createOrUpdate( {}, {}, {} )", new Object[]{collectionPath, serverId, clientId});
			if (serverId != null) {
				Long mailUid = getEmailUidFromServerId(serverId);
				mailboxService.updateReadFlag(udr, collectionPath, mailUid, email.isRead());
			}
			return serverId;
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ItemNotFoundException(e);
		}
	}

	@Override
	public String move(UserDataRequest udr, String srcFolder, String dstFolder, String messageId) 
			throws CollectionNotFoundException, ProcessingEmailException, UnsupportedBackendFunctionException {
		
		try {
			logger.info("move( messageId =  {}, from = {}, to = {} )", new Object[]{messageId, srcFolder, dstFolder});
			final Long currentMailUid = getEmailUidFromServerId(messageId);
			final Integer srcFolderId = mappingService.getCollectionIdFor(udr.getDevice(), srcFolder);
			final Integer dstFolderId = mappingService.getCollectionIdFor(udr.getDevice(), dstFolder);
			final Integer devDbId = udr.getDevice().getDatabaseId();
			Long newUidMail = mailboxService.moveItem(udr, srcFolder, dstFolder, currentMailUid);
			deleteEmails(devDbId, srcFolderId, Arrays.asList(currentMailUid));
			addMessageInCache(udr, devDbId, dstFolderId, newUidMail, dstFolder);
			return dstFolderId + ":" + newUidMail;	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ProcessingEmailException(e);
		}
	}


	@Override
	public void sendEmail(UserDataRequest udr, byte[] mailContent, boolean saveInSent) throws ProcessingEmailException {
		try {
			Message message = mime4jUtils.parseMessage(mailContent);
			SendEmail sendEmail = new SendEmail(getUserEmail(udr), message);
			send(udr, sendEmail, saveInSent);
		} catch (UnexpectedObmSyncServerException e) {
			throw new ProcessingEmailException(e);
		} catch (MimeException e) {
			throw new ProcessingEmailException(e);
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (AuthFault e) {
			throw new ProcessingEmailException(e);
		} 
	}

	@Override
	public void replyEmail(UserDataRequest udr, byte[] mailContent, boolean saveInSent, Integer collectionId, String serverId)
			throws ProcessingEmailException, CollectionNotFoundException, ItemNotFoundException {
		
		try {
			String collectionPath = "";
			if (collectionId != null && collectionId > 0) {
				collectionPath = mappingService.getCollectionPathFor(collectionId);
			}
			
			if (serverId == null || !serverId.isEmpty()) {
				collectionId = mappingService.getCollectionIdFromServerId(serverId);
				collectionPath = mappingService.getCollectionPathFor(collectionId);
			}
			
			Long uid = getEmailUidFromServerId(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<MSEmail> mail = mailboxService.fetchMails(udr, collectionId, collectionPath, uids);

			if (mail.size() > 0) {
				Message message = mime4jUtils.parseMessage(mailContent);
				ReplyEmail replyEmail = new ReplyEmail(configurationService, mime4jUtils, getUserEmail(udr), mail.get(0), message,
						ImmutableMap.<String, MSAttachementData>of());
				send(udr, replyEmail, saveInSent);
				mailboxService.setAnsweredFlag(udr, collectionPath, uid);
			} else {
				sendEmail(udr, mailContent, saveInSent);
			}
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (UnexpectedObmSyncServerException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (MimeException e) {
			throw new ProcessingEmailException(e);
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (AuthFault e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ItemNotFoundException(e);
		} 
	}

	@Override
	public void forwardEmail(UserDataRequest udr, byte[] mailContent, boolean saveInSent, String collectionId, String serverId) 
			throws ProcessingEmailException, CollectionNotFoundException {
		
		try {
			Integer collectionIdInt = Integer.parseInt(collectionId);
			String collectionName = mappingService.getCollectionPathFor(collectionIdInt);
			Long uid = getEmailUidFromServerId(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);

			List<MSEmail> mail = mailboxService.fetchMails(udr, collectionIdInt, collectionName, uids);
			if (mail.size() > 0) {
				Message message = mime4jUtils.parseMessage(mailContent);
				MSEmail originMail = mail.get(0);
				
				Map<String, MSAttachementData> originalMailAttachments = new HashMap<String, MSAttachementData>();
				if (!mime4jUtils.isAttachmentsExist(message)) {
					loadAttachments(originalMailAttachments, udr, originMail);
				}
				
				ForwardEmail forwardEmail = 
						new ForwardEmail(configurationService, mime4jUtils, getUserEmail(udr), originMail, message, originalMailAttachments);
				send(udr, forwardEmail, saveInSent);
				try{
					mailboxService.setAnsweredFlag(udr, collectionName, uid);
				} catch (Throwable e) {
					logger.info("Can't set Answered Flag to mail["+uid+"]");
				}
			} else {
				sendEmail(udr, mailContent, saveInSent);
			}
		} catch (NumberFormatException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (UnexpectedObmSyncServerException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (MimeException e) {
			throw new ProcessingEmailException(e);
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (AuthFault e) {
			throw new ProcessingEmailException(e);
		} 
	}

	private void loadAttachments(Map<String, MSAttachementData> attachments, 
			UserDataRequest udr, MSEmail originMail) throws ProcessingEmailException {
		
		for (MSAttachement msAttachement: originMail.getAttachements()) {
			try {
				MSAttachementData msAttachementData = getAttachment(udr, msAttachement.getFileReference());
				attachments.put(msAttachement.getDisplayName(), msAttachementData);
			} catch (AttachementNotFoundException e) {
				throw new ProcessingEmailException(e);
			} catch (CollectionNotFoundException e) {
				throw new ProcessingEmailException(e);
			} 
		}
	}

	private AccessToken login(UserDataRequest session) throws AuthFault {
		return login.login(session.getUser().getLoginAtDomain(), session.getPassword());
	}
	
	private String getUserEmail(UserDataRequest udr) throws UnexpectedObmSyncServerException, AuthFault {
		ICalendar cal = calendarClient;
		AccessToken at = login(udr);
		try {
			return cal.getUserEmail(at);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			login.logout(at);
		}
	}

	private void send(UserDataRequest udr, SendEmail sendEmail, boolean saveInSent) throws ProcessingEmailException {
		try {
			boolean isScheduleMeeting = !TNEFUtils.isScheduleMeetingRequest(sendEmail.getMessage());

			Address from = getAddress(sendEmail.getFrom());
			if (!sendEmail.isInvitation() && isScheduleMeeting) {
				mailboxService.sendEmail(udr, from, sendEmail.getTo(),
						sendEmail.getCc(), sendEmail.getCci(), sendEmail.getMessage(), saveInSent);	
			} else {
				logger.warn("OPUSH blocks email invitation sending by PDA. Now that obm-sync handle email sending on event creation/modification/deletion, we must filter mail from PDA for these actions.");
			}
		} catch (TNEFConverterException e) {
			throw new ProcessingEmailException(e);
		} catch (StoreEmailException e) {
			throw new ProcessingEmailException(e);
		} catch (SendEmailException e) {
			throw new ProcessingEmailException(e);
		} catch (SmtpInvalidRcptException e) {
			throw new ProcessingEmailException(e);
		}
	}

	private Address getAddress(String from) throws ProcessingEmailException {
		if(from == null || !from.contains("@")){
			throw new ProcessingEmailException(""+from+"is not a valid email");
		}
		return new Address(from);
	}

	@Override
	public MSEmail getEmail(UserDataRequest udr, Integer collectionId, String serverId) throws CollectionNotFoundException, ProcessingEmailException {
		try {
			String collectionName = mappingService.getCollectionPathFor(collectionId);
			Long uid = getEmailUidFromServerId(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<MSEmail> emails = mailboxService.fetchMails(udr, collectionId, collectionName, uids);
			if (emails.size() > 0) {
				return emails.get(0);
			}
			return null;	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		}
	}

	@Override
	public MSAttachementData getAttachment(UserDataRequest udr, String attachmentId) 
			throws AttachementNotFoundException, CollectionNotFoundException, ProcessingEmailException {
		
		if (attachmentId != null && !attachmentId.isEmpty()) {
			Map<String, String> parsedAttId = AttachmentHelper.parseAttachmentId(attachmentId);
			try {
				String collectionId = parsedAttId
						.get(AttachmentHelper.COLLECTION_ID);
				String messageId = parsedAttId.get(AttachmentHelper.MESSAGE_ID);
				String mimePartAddress = parsedAttId
						.get(AttachmentHelper.MIME_PART_ADDRESS);
				String contentType = parsedAttId
						.get(AttachmentHelper.CONTENT_TYPE);
				String contentTransferEncoding = parsedAttId
						.get(AttachmentHelper.CONTENT_TRANSFERE_ENCODING);
				logger.info("attachmentId= [collectionId:" + collectionId
						+ "] [emailUid" + messageId + "] [mimePartAddress:"
						+ mimePartAddress + "] [contentType" + contentType
						+ "] [contentTransferEncoding"
						+ contentTransferEncoding + "]");

				String collectionName = mappingService.getCollectionPathFor(Integer
						.parseInt(collectionId));
				InputStream is = mailboxService.findAttachment(udr,
						collectionName, Long.parseLong(messageId),
						new MimeAddress(mimePartAddress));

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				FileUtils.transfer(is, out, true);
				byte[] rawData = out.toByteArray();

				if ("QUOTED-PRINTABLE".equals(contentTransferEncoding)) {
					out = new ByteArrayOutputStream();
					InputStream in = new QPDecoderStream(new ByteArrayInputStream(rawData));
					FileUtils.transfer(in, out, true);
					rawData = out.toByteArray();
				} else if ("BASE64".equals(contentTransferEncoding)) {
					rawData = new Base64().decode(rawData);
				}

				return new MSAttachementData(contentType,
						new ByteArrayInputStream(rawData));
		
			} catch (NumberFormatException e) {
				throw new ProcessingEmailException(e);
			} catch (IOException e) {
				throw new ProcessingEmailException(e);
			} catch (MailException e) {
				throw new ProcessingEmailException(e);
			} catch (DaoException e) {
				throw new ProcessingEmailException(e);
			} catch (LocatorClientException e) {
				throw new ProcessingEmailException(e);
			}
		}
		
		throw new AttachementNotFoundException();
	}

	@Override
	public void emptyFolderContent(UserDataRequest udr, String collectionPath,
			boolean deleteSubFolder) throws NotAllowedException, CollectionNotFoundException, ProcessingEmailException {
		
		try {
			String wasteBasketPath = getWasteBasketPath(udr);
			if (!wasteBasketPath.equals(collectionPath)) {
				throw new NotAllowedException(
						"Only the Trash folder can be purged.");
			}
			final Integer devDbId = udr.getDevice().getDatabaseId();
			int collectionId = mappingService.getCollectionIdFor(udr.getDevice(), collectionPath);
			Collection<Long> uids = mailboxService.purgeFolder(udr, devDbId, collectionPath, collectionId);
			deleteEmails(devDbId, collectionId, uids);
			if (deleteSubFolder) {
				logger.warn("deleteSubFolder isn't implemented because opush doesn't yet manage folders");
			}	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		}
	}
	
	@Override
	public Long getEmailUidFromServerId(String serverId) {
		Integer itemIdFromServerId = mappingService.getItemIdFromServerId(serverId);
		if (itemIdFromServerId != null) {
			return itemIdFromServerId.longValue();
		} else {
			return null;
		}
	}

	private void addMessageInCache(UserDataRequest udr, Integer devId, Integer collectionId, Long mailUids, String collectionName) throws DaoException, MailException {
		Collection<Email> emails = mailboxService.fetchEmails(udr, collectionName, ImmutableList.of(mailUids));
		try {
			markEmailsAsSynced(devId, collectionId, emails);
		} catch (DaoException e) {
			throw new DaoException("Error while adding messages in db", e);
		}
	}

	public void markEmailsAsSynced(Integer devId, Integer collectionId, Collection<Email> messages) throws DaoException {
		markEmailsAsSynced(devId, collectionId, DateUtils.getCurrentDate(), messages);
	}

	public void markEmailsAsSynced(Integer devId, Integer collectionId, Date lastSync, Collection<Email> emails) throws DaoException {
		Set<Email> allEmailsToMark = Sets.newHashSet(emails);
		Set<Email> alreadySyncedEmails = emailDao.alreadySyncedEmails(collectionId, devId, emails);
		Set<Email> modifiedEmails = findModifiedEmails(allEmailsToMark, alreadySyncedEmails);
		Set<Email> emailsNeverTrackedBefore = filterOutAlreadySyncedEmails(allEmailsToMark, alreadySyncedEmails);
		logger.info("mark {} updated mail(s) as synced", modifiedEmails.size());
		emailDao.updateSyncEntriesStatus(devId, collectionId, modifiedEmails);
		logger.info("mark {} new mail(s) as synced", emailsNeverTrackedBefore.size());
		emailDao.createSyncEntries(devId, collectionId, emailsNeverTrackedBefore, lastSync);
	}

	private Set<Email> findModifiedEmails(Set<Email> allEmailsToMark, Set<Email> alreadySyncedEmails) {
		Map<Long, Email> indexedEmailsToMark = getEmailsAsUidTreeMap(allEmailsToMark);
		HashSet<Email> modifiedEmails = Sets.newHashSet();
		for (Email email: alreadySyncedEmails) {
			Email modifiedEmail = indexedEmailsToMark.get(email.getUid());
			if (modifiedEmail != null && !modifiedEmail.equals(email)) {
				modifiedEmails.add(modifiedEmail);
			}
		}
		return modifiedEmails;
	}

	private Set<Email> filterOutAlreadySyncedEmails(Set<Email> allEmailsToMark, Set<Email> alreadySyncedEmails) {
		return org.obm.push.utils.collection.Sets.difference(allEmailsToMark, alreadySyncedEmails, new Comparator<Email>() {
			@Override
			public int compare(Email o1, Email o2) {
				return Long.valueOf(o1.getUid() - o2.getUid()).intValue();
			}
		});
	}

	private Map<Long, Email> getEmailsAsUidTreeMap(Set<Email> emails) {
		return Maps.uniqueIndex(emails, new Function<Email, Long>() {
			@Override
			public Long apply(Email email) {
				return email.getIndex();
			}
		});
	}
	

	private void deleteEmails(Integer devId, Integer collectionId, Collection<Long> mailUids) throws DaoException {
		try {
			emailDao.deleteSyncEmails(devId, collectionId, mailUids);
		} catch (DaoException e) {
			throw new DaoException("Error while deleting messages in db", e);
		}
	}

}
