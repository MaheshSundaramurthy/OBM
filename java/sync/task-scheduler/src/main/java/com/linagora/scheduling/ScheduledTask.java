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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class ScheduledTask implements Delayed {

	public enum State {
		FAILED,
		NEW,
		RUNNING,
		TERMINATED,
		WAITING
	}

	public static abstract class Listener {
		@SuppressWarnings("unused") 
		public void failed(Throwable failure) {}
		public void running() {}
		public void scheduled() {}
		public void terminated() {}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private DateTime scheduledTime;
		private Task task;
		private ImmutableList.Builder<Listener> listeners;

		private Builder() {
			listeners = ImmutableList.<Listener>builder();
		}
		
		public Builder scheduledTime(DateTime scheduledTime) {
			this.scheduledTime = scheduledTime;
			return this;
		}
		
		public Builder task(Task task) {
			this.task = task;
			return this;
		}
		
		public Builder addListener(Listener listener) {
			listeners.add(listener);
			return this;
		}
		
		public ScheduledTask schedule(Scheduler scheduler) {
			return new ScheduledTask(scheduledTime, task, scheduler, listeners.build()).schedule();
		}
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final DateTime scheduledTime;
	private final Task task;
	private final Scheduler scheduler;
	private final ImmutableList<Listener> listeners;
	private State state;
	
	public ScheduledTask(DateTime scheduledTime, Task task, Scheduler scheduler, ImmutableList<Listener> listeners) {
		this.scheduledTime = scheduledTime;
		this.task = task;
		this.scheduler = scheduler;
		this.listeners = listeners;
		this.state = State.NEW;
	}
	
	private ScheduledTask schedule() {
		ScheduledTask task = scheduler.schedule(this);
		notifyScheduled();
		return task;
	}

	public void cancel() {
		//FIXME coming soon
	}

	public State state() {
		return state;
	}
	
	public DateTime scheduledTime() {
		return scheduledTime;
	}
	
	public Task task() {
		return task;
	}

	/* package */ Runnable runnable() {
		return new Runnable() {
			@Override
			public void run() {
				notifyStart();
				try {
					task.run();
					notifyTerminated();
				} catch (Exception e) {
					notifyFailed(e);
				}
			}
		};
	}
	
	private void notifyScheduled() {
		state = State.WAITING;
		for (Listener listener: listeners) {
			try {
				listener.scheduled();
			} catch (Exception listenerException) {
				logger.error("Error notifying a listener", listenerException);
			}
		}
	}
	
	private void notifyStart() {
		state = State.RUNNING;
		for (Listener listener: listeners) {
			try {
				listener.running();
			} catch (Exception listenerException) {
				logger.error("Error notifying a listener", listenerException);
			}
		}
	}
	
	private void notifyTerminated() {
		state = State.TERMINATED;
		for (Listener listener: listeners) {
			try {
				listener.terminated();
			} catch (Exception listenerException) {
				logger.error("Error notifying a listener", listenerException);
			}
		}
	}

	private void notifyFailed(Exception e) {
		state = State.FAILED;
		for (Listener listener: listeners) {
			try {
				listener.failed(e);
			} catch (Exception listenerException) {
				logger.error("Error notifying a listener", listenerException);
			}
		}
	}
	
	@Override
	public int compareTo(Delayed o) {
		return Long.compare(getDelay(TimeUnit.SECONDS), o.getDelay(TimeUnit.SECONDS));
	}
	
	@Override
	public long getDelay(TimeUnit unit) {
		return 
			unit.convert(
				Seconds.secondsBetween(scheduler.now(), scheduledTime).getSeconds(), 
				TimeUnit.SECONDS);
	}
}
