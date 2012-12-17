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

package org.minig.imap.sieve;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SieveCodecFactory implements ProtocolCodecFactory {
	
	private static final Logger logger = LoggerFactory
			.getLogger(SieveClientSupport.class);

	private ProtocolDecoder decoder = new ProtocolDecoderAdapter() {

		@Override
		public void decode(IoSession arg0, ByteBuffer arg1,
				ProtocolDecoderOutput arg2) throws Exception {
			java.nio.ByteBuffer received = arg1.buf();
			java.nio.ByteBuffer copy = java.nio.ByteBuffer.allocate(received
					.remaining());
			copy.put(received);
			// copy.flip();
			byte[] data = copy.array();
			if (logger.isDebugEnabled()) {
				logger.debug("decoded: " + new String(data));
			}
			SieveMessage sm = new SieveMessage();
			sm.addLine(new String(data));
			arg2.write(sm);
		}
	};

	private ProtocolEncoder encoder = new ProtocolEncoderAdapter() {

		@Override
		public void encode(IoSession arg0, Object arg1,
				ProtocolEncoderOutput arg2) throws Exception {
			byte[] raw = (byte[]) arg1;
			ByteBuffer b = ByteBuffer.wrap(raw);
			arg2.write(b);
		}
	};

	@Override
	public ProtocolDecoder getDecoder() throws Exception {
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder() throws Exception {
		return encoder;
	}

}
