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
package org.obm.sync.push.client.commands;

import java.io.IOException;

import org.obm.push.bean.FilterType;
import org.obm.push.bean.GetItemEstimateStatus;
import org.obm.push.bean.SyncKey;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.GetItemEstimateSingleFolderResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

public class GetItemEstimateEmailFolderCommand extends AbstractCommand<GetItemEstimateSingleFolderResponse> {


	public GetItemEstimateEmailFolderCommand(SyncKey syncKey, int collectionId)
			throws SAXException, IOException {
		this(syncKey, null, collectionId);
	}
	
	public GetItemEstimateEmailFolderCommand(final SyncKey syncKey, final FilterType filterType,
			final int collectionId) throws SAXException, IOException {
		
		super(NS.GetItemEstimate, "GetItemEstimate", new TemplateDocument("GetItemEstimateRequestEmail.xml") {
			@Override
			protected void customize(Document document, AccountInfos accountInfos) {
				Element sk = DOMUtils.getUniqueElement(document.getDocumentElement(), "AirSync:SyncKey");
				sk.setTextContent(syncKey.getSyncKey());
				if (filterType != null) {
					Element ft = DOMUtils.getUniqueElement(document.getDocumentElement(), "AirSync:FilterType");
					ft.setTextContent(filterType.asSpecificationValue());
				}
				Element collection = DOMUtils.getUniqueElement(document.getDocumentElement(), "CollectionId");
				collection.setTextContent(String.valueOf(collectionId));				
			}
		});
	}

	@Override
	protected GetItemEstimateSingleFolderResponse parseResponse(Element root) {
		int colId = 0;
		int estimate = 0;
		String unparsedCollectionId = DOMUtils.getElementText(root, "CollectionId");
		if (!Strings.isNullOrEmpty(unparsedCollectionId)) {
			colId = Integer.parseInt(unparsedCollectionId);
		}
		String unparsedEstimate = DOMUtils.getElementText(root, "Estimate");
		if (!Strings.isNullOrEmpty(unparsedEstimate)) {
			estimate = Integer.parseInt(unparsedEstimate);
		}
		GetItemEstimateStatus status = findStatus(root);
		return new GetItemEstimateSingleFolderResponse(colId, estimate, status);
	}
	
	private GetItemEstimateStatus findStatus(Element root) {
		int status = Integer.parseInt(DOMUtils.getElementText(root, "Status"));
		return GetItemEstimateStatus.fromSpecificationValue(status);
	}
}
