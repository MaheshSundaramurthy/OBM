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
package org.obm.push.state;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.activesync.InvalidSyncKeyException;
import org.obm.push.service.impl.DateServiceImpl;
import org.obm.push.store.CollectionDao;
import org.obm.push.utils.DateUtils;

@RunWith(SlowFilterRunner.class)
public class StateMachineTest {

	@Test(expected=IllegalArgumentException.class)
	public void testGetFolderSyncStateWithNullKey() throws Exception {
		StateMachine stateMachine = new StateMachine(null , null, new SyncKeyFactory(), new DateServiceImpl());
		
		stateMachine.getFolderSyncState(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetFolderSyncStateWithEmptyKey() throws Exception {
		StateMachine stateMachine = new StateMachine(null , null, new SyncKeyFactory(), new DateServiceImpl());
		
		stateMachine.getFolderSyncState(new SyncKey(""));
	}

	@Test
	public void testGetFolderSyncStateWithInitialKey() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;

		StateMachine stateMachine = new StateMachine(null , null, new SyncKeyFactory(), new DateServiceImpl());
		FolderSyncState folderSyncState = stateMachine.getFolderSyncState(initialSyncKey);
		
		assertThat(folderSyncState.getSyncKey()).isEqualTo(initialSyncKey);
		assertThat(folderSyncState.getSyncDate()).isEqualTo(DateUtils.getEpochPlusOneSecondCalendar().getTime());
		assertThat(folderSyncState.isInitialFolderSync()).isTrue();
	}

	@Test
	public void testGetFolderSyncStateWithKnownKey() throws Exception {
		SyncKey knownSyncKey = new SyncKey("1234");
		Date knownSyncDate = org.obm.DateUtils.date("2013-01-01T12:00:15");
		int knownSyncStateId = 156;
		FolderSyncState knownFolderSyncState = FolderSyncState.builder()
				.syncDate(knownSyncDate)
				.syncKey(knownSyncKey)
				.id(knownSyncStateId)
				.build();
		
		CollectionDao collectionDao = createStrictMock(CollectionDao.class);
		expect(collectionDao.findFolderStateForKey(knownSyncKey)).andReturn(knownFolderSyncState).once();
		replay(collectionDao);
		
		StateMachine stateMachine = new StateMachine(collectionDao , null, new SyncKeyFactory(), new DateServiceImpl());
		FolderSyncState folderSyncState = stateMachine.getFolderSyncState(knownSyncKey);

		verify(collectionDao);

		assertThat(folderSyncState.getId()).isEqualTo(knownSyncStateId);
		assertThat(folderSyncState.getSyncKey()).isEqualTo(knownSyncKey);
		assertThat(folderSyncState.getSyncDate()).isEqualTo(knownSyncDate);
		assertThat(folderSyncState.isInitialFolderSync()).isFalse();
	}

	@Test(expected=InvalidSyncKeyException.class)
	public void testGetFolderSyncStateWithUnknownKey() throws Exception {
		SyncKey unknownSyncKey = new SyncKey("1234");
		
		CollectionDao collectionDao = createStrictMock(CollectionDao.class);
		expect(collectionDao.findFolderStateForKey(unknownSyncKey)).andReturn(null).once();
		replay(collectionDao);
		
		StateMachine stateMachine = new StateMachine(collectionDao , null, new SyncKeyFactory(), new DateServiceImpl());
		stateMachine.getFolderSyncState(unknownSyncKey);
	}
}
