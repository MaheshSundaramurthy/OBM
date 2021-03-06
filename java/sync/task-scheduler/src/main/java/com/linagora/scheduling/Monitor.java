/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package com.linagora.scheduling;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;

public class Monitor<T extends Task> implements Listener<T> {

	public static <T extends Task> Builder<T> builder() {
		return new Builder<T>();
	}
	
	public static class Builder<T extends Task> {
		
		private final ImmutableList.Builder<Listener<T>> listeners;

		private Builder() {
			listeners = ImmutableList.builder();
		}
		
		public Builder<T> addListener(Listener<T> listener) {
			listeners.add(listener);
			return this;
		}
		
		public Monitor<T> build() {
			return new Monitor<T>(listeners.build());
		}
	}
	
	private final ListenersNotifier<T> listenerNofifier;
	private final ConcurrentMap<ScheduledTask.Id, ScheduledTask<T>> tasks;
	
	private Monitor(ImmutableList<Listener<T>> listeners) {
		this.listenerNofifier = new ListenersNotifier<>(Monitor.class, listeners);
		this.tasks = new MapMaker().weakKeys().makeMap();
	}
	
	public List<ScheduledTask<T>> all() {
		return ImmutableList.copyOf(tasks.values());
	}
	
	public Optional<ScheduledTask<T>> findById(ScheduledTask.Id id) {
		return Optional.fromNullable(tasks.get(id));
	}
	
	@Override
	public void scheduled(ScheduledTask<T> task) {
		tasks.put(task.id(), task);
		listenerNofifier.notifyScheduled(task);
	}
	
	@Override
	public void terminated(ScheduledTask<T> task) {
		tasks.remove(task.id());
		listenerNofifier.notifyTerminated(task);
	}
	
	@Override
	public void canceled(ScheduledTask<T> task) {
		tasks.remove(task.id());
		listenerNofifier.notifyCanceled(task);
	}
	
	@Override
	public void failed(ScheduledTask<T> task, Throwable failure) {
		tasks.remove(task.id());
		listenerNofifier.notifyFailed(task, failure);
	}

	@Override
	public void running(ScheduledTask<T> task) {
		listenerNofifier.notifyRunning(task);
	}
}
