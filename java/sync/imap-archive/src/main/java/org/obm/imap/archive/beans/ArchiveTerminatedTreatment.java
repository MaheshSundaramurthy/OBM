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
package org.obm.imap.archive.beans;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveTerminatedTreatment extends ArchiveTreatment {

	public static TerminatedBuilder forDomain(ObmDomainUuid domainUuid) {
		return new TerminatedBuilder(domainUuid);
	}
	
	public static class TerminatedBuilder extends Builder<ArchiveTerminatedTreatment>{
		
		protected TerminatedBuilder(ObmDomainUuid domainUuid) {
			super(domainUuid);
		}

		@Override
		public ArchiveTerminatedTreatment build() {
			Preconditions.checkState(isValidStatus(status));
			Preconditions.checkState(runId != null);
			Preconditions.checkState(scheduledTime != null);
			Preconditions.checkState(startTime != null);
			Preconditions.checkState(endTime != null);
			Preconditions.checkState(higherBoundary != null);
			return new ArchiveTerminatedTreatment(runId, domainUuid, status, 
					scheduledTime, startTime, endTime, higherBoundary);
		}

		private boolean isValidStatus(ArchiveStatus status) {
			switch (status) {
			case SCHEDULED:
			case RUNNING:
				break;
			case SUCCESS:
			case ERROR:
				return true;
			}
			return false;
		}
	}
	
	private ArchiveTerminatedTreatment(
			ArchiveTreatmentRunId runId, ObmDomainUuid  domainUuid, ArchiveStatus archiveStatus, 
			DateTime scheduledTime, DateTime startTime, DateTime endTime, DateTime higherBoundary) {
		super(runId, domainUuid, archiveStatus, scheduledTime, startTime, endTime, higherBoundary);
	}
	
}