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
package org.obm.push.backend;

import java.util.TreeSet;

import org.fest.assertions.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.obm.push.contacts.ComparatorUsingFolderName;
import org.obm.sync.book.Folder;

import com.google.common.collect.ImmutableList;

import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class ContactsBackendTest {

	@Test
	public void sortedByDefaultFolderName() {
		final String defaultFolderName = "contacts";
		
		Folder f1 = createFolder("users", -1);
		Folder f2 = createFolder("collected_contacts", 2);
		Folder f3 = createFolder(defaultFolderName, 3);
		Folder f4 = createFolder("my address book", 4);
		
		ImmutableList<Folder> immutableList = ImmutableList.of(f1, f2, f3, f4);
		TreeSet<Folder> treeset = new TreeSet<Folder>(
				new ComparatorUsingFolderName(defaultFolderName));
		treeset.addAll(immutableList);
		
		Assert.assertNotNull(treeset);
		Assertions.assertThat(treeset).hasSize(4);
		Assertions.assertThat(treeset).contains(immutableList.toArray());
		Assertions.assertThat(treeset.first().getName()).isEqualTo(defaultFolderName);
		Assertions.assertThat(treeset.last().getName()).isEqualTo("users");
	}

	private Folder createFolder(String name, int uid) {
		Folder folder = new Folder();
		folder.setName(name);
		folder.setUid(uid);
		return folder;
	}
	
}
