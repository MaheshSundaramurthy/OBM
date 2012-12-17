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
package org.obm.push.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.exception.ElementNotFoundException;
import org.obm.push.store.ehcache.ObjectStoreManager;

public class ContinuationTransactionMapImplTest {
	
	public final static String PENDING_CONTINUATIONS = "pendingContinuation";
	public final static String KEY_ID_REQUEST = "key_id_request";
	private Device device;
	
	@Before
	public void setUp() {
		device = new Device(1, "devType", new DeviceId("devId"), new Properties());
	}
	
	@Test
	public void testGetContinuationForDevice() throws ElementNotFoundException {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		PushContinuation expectedContinuation = mockContinuation();
		cache.putIfAbsent(new Element(device, expectedContinuation));
		
		replay(objectStoreManager, expectedContinuation);

		ContinuationTransactionMapImpl continuationTransactionMap = new ContinuationTransactionMapImpl(objectStoreManager);
		IContinuation continuationForDevice = continuationTransactionMap.getContinuationForDevice(device);
		
		verify(objectStoreManager, expectedContinuation);
		assertThat(continuationForDevice).isEqualTo(expectedContinuation);
	}
	
	@Test (expected=ElementNotFoundException.class)
	public void testGetContinuationForDeviceElementNotFound() throws ElementNotFoundException {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		replay(objectStoreManager);

		ContinuationTransactionMapImpl continuationTransactionMap = new ContinuationTransactionMapImpl(objectStoreManager);
		continuationTransactionMap.getContinuationForDevice(device);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPutContinuationForDevice() {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		PushContinuation expectedContinuation = mockContinuation();
		cache.putIfAbsent(new Element(device, expectedContinuation));
		Element expectedElement = new Element(device, expectedContinuation);
		
		replay(objectStoreManager, expectedContinuation);

		ContinuationTransactionMapImpl continuationTransactionMap = new ContinuationTransactionMapImpl(objectStoreManager);
		Element element = continuationTransactionMap.putContinuationForDevice(device, expectedContinuation);
		
		verify(objectStoreManager, expectedContinuation);
		assertThat(element).isEqualTo(expectedElement);
		assertThat(cache.getKeys()).containsOnly(device);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPutContinuationForDeviceNoCachedElement() {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		PushContinuation expectedContinuation = mockContinuation();
		
		replay(objectStoreManager, expectedContinuation);

		ContinuationTransactionMapImpl continuationTransactionMap = new ContinuationTransactionMapImpl(objectStoreManager);
		Element element = continuationTransactionMap.putContinuationForDevice(device, expectedContinuation);
		
		verify(objectStoreManager, expectedContinuation);
		assertThat(element).isNull();
		assertThat(cache.getKeys()).containsOnly(device);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDelete() {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		PushContinuation expectedContinuation = mockContinuation();
		cache.putIfAbsent(new Element(device, expectedContinuation));
		
		replay(objectStoreManager, expectedContinuation);

		ContinuationTransactionMapImpl continuationTransactionMap = new ContinuationTransactionMapImpl(objectStoreManager);
		continuationTransactionMap.delete(device);
		
		verify(objectStoreManager, expectedContinuation);
		assertThat(cache.getKeys()).isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteNotCachedElement() {
		Cache cache = buildCache();
		ObjectStoreManager objectStoreManager = mockObjectStoreManager(cache);
		
		PushContinuation expectedContinuation = mockContinuation();
		
		replay(objectStoreManager, expectedContinuation);

		ContinuationTransactionMapImpl continuationTransactionMap = new ContinuationTransactionMapImpl(objectStoreManager);
		continuationTransactionMap.delete(device);
		
		verify(objectStoreManager, expectedContinuation);
		assertThat(cache.getKeys()).isEmpty();
	}

	private PushContinuation mockContinuation() {
		PushContinuation pushContinuation = createMock(PushContinuation.class);
		return pushContinuation;
	}
	
	private CacheConfiguration cacheConfigurationForContinuation() {
		return new CacheConfiguration()
			.maxElementsInMemory(0)
			.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
			.transactionalMode(TransactionalMode.OFF)
			.eternal(false)
			.name(PENDING_CONTINUATIONS);
	}
	
	private ObjectStoreManager mockObjectStoreManager(Cache cache) {
		ObjectStoreManager objectStoreManager = createMock(ObjectStoreManager.class);
		expect(objectStoreManager.getStore(PENDING_CONTINUATIONS))
			.andReturn(cache).anyTimes();
		
		return objectStoreManager;
	}

	private Cache buildCache() {
		Cache cache = new Cache(cacheConfigurationForContinuation());
		cache.initialise();
		return cache;
	}
}
