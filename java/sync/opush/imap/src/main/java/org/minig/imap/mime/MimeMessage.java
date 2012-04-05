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

package org.minig.imap.mime;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Represents the mime tree of a message. The tree of a message can be obtained
 * by parsing the BODYSTRUCTURE response from the IMAP server.
 */
public class MimeMessage implements IMimePart {

	private long uid;
	private final IMimePart from;

	public MimeMessage(IMimePart from) {
		super();
		this.from = from;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		printTree(sb);
		return sb.toString();
	}

	private void printTree(StringBuilder sb) {
		for (IMimePart part: this.listLeaves(true, false)) {
			sb.append(part.getAddress()).append(" ");
			printMimeDetails(sb, part);
		}
	}

	private void printMimeDetails(StringBuilder sb, IMimePart mimeTree) {
		sb.append(mimeTree.getMimeType() + "/" + mimeTree.getMimeSubtype());
		String name = mimeTree.getName();
		if (name != null) {
			sb.append(" " + name);
		}
	}

	public boolean isSinglePartMessage() {
		return getChildren().size() == 1 && getChildren().get(0).getChildren().isEmpty();
	}

	public boolean hasAttachments() {
		for (IMimePart part: this.listLeaves(true, false)) {
			String full = part.getFullMimeType();
			if (!("text/plain".equals(full)) && !("text/html".equals(full))) {
				return true;
			}
		}
		return false;
	}

	public boolean hasInvitation() {
		for (IMimePart part: this.listLeaves(true, true)) {
			if (part.isInvitation()) {
				return true;
			}
		}
		return false;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	@Override
	public void addPart(IMimePart child) {
		from.addPart(child);
	}

	@Override
	public String getMimeType() {
		return from.getMimeType();
	}

	@Override
	public String getMimeSubtype() {
		return from.getMimeSubtype();
	}

	@Override
	public List<IMimePart> getChildren() {
		return from.getChildren();
	}

	@Override
	public MimeAddress getAddressInternal() {
		return from.getAddress(); 
	}
	
	@Override
	public MimeAddress getAddress() {
		return null;
	}

	@Override
	public Collection<BodyParam> getBodyParams() {
		return from.getBodyParams();
	}

	@Override
	public BodyParam getBodyParam(String param) {
		return from.getBodyParam(param);
	}

	@Override
	public IMimePart getParent() {
		return null;
	}

	@Override
	public void defineParent(IMimePart parent, int index) {
		throw new RuntimeException("");
	}

	@Override
	public String getFullMimeType() {
		return from.getFullMimeType();
	}

	@Override
	public boolean isInvitation() {
		return from.isInvitation();
	}

	@Override
	public String getContentTransfertEncoding() {
		return from.getContentTransfertEncoding();
	}

	@Override
	public String getCharset() {
		return from.getCharset();
	}
	
	@Override
	public String getContentId() {
		return from.getContentId();
	}

	@Override
	public boolean isCancelInvitation() {
		return from.isCancelInvitation();
	}

	@Override
	public void setBodyParams(Collection<BodyParam> newParams) {
		from.setBodyParams(newParams);
	}
	
	@Override
	public void setMimeType(ContentType mimetype) {
		from.setMimeType(mimetype);		
	}
	
	@Override
	public String getName() {
		return from.getName();
	}
	
	@Override
	public boolean isMultipart() {
		return from.isMultipart();
	}
	
	@Override
	public String getMultipartSubtype() {
		return from.getMimeSubtype();
	}
	
	@Override
	public void setMultipartSubtype(String subtype) {
		from.setMultipartSubtype(subtype);
	}

	@Override
	public Collection<IMimePart> listLeaves(boolean depthFirst, boolean filterNested) {
		return from.listLeaves(depthFirst, filterNested);
	}
	
	@Override
	public boolean isAttachment() {
		return false;
	}
	
	@Override
	public List<IMimePart> getSibling() {
		return ImmutableList.of();
	}

	@Override
	public boolean isNested() {
		return false;
	}
	
	public IMimePart getMimePart() {
		return from;
	}
}
