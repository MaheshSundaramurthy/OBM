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
package org.obm.push.protocol;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.push.bean.FolderSyncStatus;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.CollectionDeletion;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.protocol.bean.FolderSyncRequest;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FolderSyncProtocol {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public FolderSyncRequest getRequest(Document doc) throws NoDocumentException {
		if (doc == null) {
			throw new NoDocumentException("Document of FolderSync request is null.");
		}
		String syncKey = DOMUtils.getElementText(doc.getDocumentElement(), "SyncKey");
		return new FolderSyncRequest(syncKey);
	}

	public Document encodeResponse(FolderSyncResponse folderSyncResponse) throws FactoryConfigurationError {
		Document ret = DOMUtils.createDoc(null, "FolderSync");
		Element root = ret.getDocumentElement();
		
		DOMUtils.createElementAndText(root, "Status", "1");
		
		Element sk = DOMUtils.createElement(root, "SyncKey");
		sk.setTextContent(folderSyncResponse.getNewSyncKey());

		Element changes = DOMUtils.createElement(root, "Changes");
		DOMUtils.createElementAndText(changes, "Count", String.valueOf(folderSyncResponse.getCount()));
		
		for (CollectionChange collectionChange: folderSyncResponse.getCollectionsAddedAndUpdated()) {
			Element addedOrUpdated;
			if (collectionChange.isNew()) {
				addedOrUpdated = DOMUtils.createElement(changes, "Add");
			} else {
				addedOrUpdated = DOMUtils.createElement(changes, "Update");
			}
			addCollectionChange(addedOrUpdated, collectionChange);
		}
		
		for (CollectionDeletion collectionDeleted: folderSyncResponse.getCollectionsDeleted()) {
			Element deleted = DOMUtils.createElement(changes, "Delete");
			DOMUtils.createElementAndText(deleted, "ServerId", collectionDeleted.getCollectionId());
		}
		
		return ret;
	}

	private void addCollectionChange(Element addedOrUpdated, CollectionChange collectionChange) {
		DOMUtils.createElementAndText(addedOrUpdated, "ServerId", collectionChange.getCollectionId());
		DOMUtils.createElementAndText(addedOrUpdated, "ParentId", collectionChange.getParentCollectionId());
		DOMUtils.createElementAndText(addedOrUpdated, "DisplayName", collectionChange.getDisplayName());
		DOMUtils.createElementAndText(addedOrUpdated, "Type", collectionChange.getFolderType()
				.asIntString());
	}

	public Document encodeErrorResponse(FolderSyncStatus status) {
		Document ret = DOMUtils.createDoc(null, "FolderSync");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", status.asXmlValue());
		return ret;
	}
	
}
