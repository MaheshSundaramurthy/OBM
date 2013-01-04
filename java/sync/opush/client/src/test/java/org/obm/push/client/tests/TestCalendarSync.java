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
package org.obm.push.client.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.beans.Collection;
import org.obm.sync.push.client.beans.Folder;
import org.obm.sync.push.client.beans.FolderSyncResponse;
import org.obm.sync.push.client.beans.FolderType;
import org.obm.sync.push.client.beans.SyncResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Ignore("It's necessary to do again all tests")
public class TestCalendarSync extends OPClientTests {

	public void testSync() throws Exception {
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder calendarFolder = fsr.getFolders().get(
				FolderType.DEFAULT_CALENDAR_FOLDER);
		SyncResponse syncResp = testInitialSync(calendarFolder);

		InputStream in = null;
		Document doc = null;
		Document ret = null;

		in = loadDataFile("GetItemEstimateRequestEvent.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp);
		syncResp = testSync(doc);
		assertNotNull(syncResp);
		Collection colInbox = syncResp.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colInbox);
		assertTrue(colInbox.getAdds().size() > 0);
	}

	public void testSyncOldSyncKey() throws Exception {
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder calendarFolder = fsr.getFolders().get(
				FolderType.DEFAULT_CALENDAR_FOLDER);
		SyncResponse syncResp1 = testInitialSync(calendarFolder);

		InputStream in = null;
		Document doc = null;
		Document ret = null;

		in = loadDataFile("GetItemEstimateRequestEvent.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		SyncResponse syncResp2 = testSync(doc);
		assertNotNull(syncResp2);
		Collection colCal2 = syncResp2.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colCal2);
		assertTrue(colCal2.getAdds().size() > 0);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp2);
		SyncResponse syncResp3 = testSync(doc);
		assertNotNull(syncResp3);
		Collection colCal3 = syncResp3.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colCal3);
		assertEquals(0, colCal3.getAdds().size());

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		SyncResponse syncResp4 = testSync(doc);
		assertNotNull(syncResp4);
		Collection colCal4 = syncResp4.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colCal4);
		assertTrue(colCal4.getAdds().size() > 0);

	}

	public void testCalAdd() throws Exception {
		testOptions();
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder calendarFolder = fsr.getFolders().get(
				FolderType.DEFAULT_CALENDAR_FOLDER);
		SyncResponse syncResp1 = testInitialSync(calendarFolder);

		InputStream in = null;
		Document doc = null;
		Document ret = null;

		in = loadDataFile("GetItemEstimateRequestEvent.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		SyncResponse syncResp2 = testSync(doc);
		assertNotNull(syncResp2);
		Collection colCal2 = syncResp2.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colCal2);
		assertTrue(colCal2.getAdds().size() > 0);

		in = loadDataFile("GetItemEstimateRequestEvent.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp2);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
	
		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp2);
		ret = postXml("AirSync", doc, "Sync");

		in = loadDataFile("CalSyncAdd.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp2);
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "ClientId")
				.setTextContent(UUID.randomUUID().toString());
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "Calendar:UID")
				.setTextContent(UUID.randomUUID().toString());
		SyncResponse syncRespAdd = testSync(doc);
		Collection colCalAdd = syncRespAdd.getCollection(calendarFolder
				.getServerId());
		assertNotNull(syncRespAdd);
		assertTrue(colCalAdd.getAdds().size() > 0);

	}

	public void testCalTwoAdd() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(
				doc.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("CalSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
				.getTextContent();

		in = loadDataFile("CalSyncAdd.xml");
		doc = DOMUtils.parse(in);
		Element cliidElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"ClientId");
		cliidElem.setTextContent("999999999");
		for (int i = 0; i < 2; i++) {
			synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
					"SyncKey");
			synckeyElem.setTextContent(sk);
			DOMUtils.logDom(doc);
			ret = postXml("AirSync", doc, "Sync");
			assertNotNull(ret);
			sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
					.getTextContent();
		}

		NodeList nl = ret.getDocumentElement().getElementsByTagName(
				"ApplicationData");
		assertTrue(nl.getLength() > 0);

	}

	public void testCalDelete() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml25("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(
				doc.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml25("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("CalSyncDelete1.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		Element cliidElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"ClientId");
		String clientId = "" + new Random().nextInt(999999999);
		cliidElem.setTextContent(clientId);
		DOMUtils.logDom(doc);
		ret = postXml25("AirSync", doc, "Sync");
		assertNotNull(ret);

		NodeList nl = ret.getDocumentElement().getElementsByTagName("Add");
		String servId = null;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element elem = (Element) nl.item(i);
				String cliId = DOMUtils.getElementText(elem, "ClientId");
				if (clientId.equals(cliId)) {
					servId = DOMUtils.getElementText(elem, "ServerId");
					break;
				}
			}
		}
		if (servId == null) {
			fail();
		}
		sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
				.getTextContent();

		in = loadDataFile("CalSyncDelete2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		Element servIdElem = DOMUtils.getUniqueElement(
				doc.getDocumentElement(), "ServerId");
		servIdElem.setTextContent(servId);
		DOMUtils.logDom(doc);
		ret = postXml25("AirSync", doc, "Sync");
		assertNotNull(ret);

	}
}
