/*******************************************************************************
 * Copyright (c) 2018 Bosch Software Innovations GmbH and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Bosch Software Innovations - initial creation
 ******************************************************************************/
package org.eclipse.californium.scandium.dtls;

import java.security.SecureRandom;

import org.eclipse.californium.elements.util.Bytes;
import org.eclipse.californium.elements.util.DatagramWriter;

/**
 * Conveys information specified by the <em>connection id</em> TLS
 * extension.
 * <p>
 * See <a href="https://datatracker.ietf.org/doc/draft-ietf-tls-dtls-connection-id">draft-ietf-tls-dtls-connection-id</a>
 * for additional details.
 *
 */
public final class ConnectionIdExtension extends HelloExtension {

	private static final int CID_FIELD_LENGTH_BITS = 8;

	private ConnectionId id;

	private ConnectionIdExtension(final byte[] id, boolean raw) {
		super(ExtensionType.CONNECTION_ID);
		byte[] cid = id;
		if (!raw) {
			int len = id[0];
			if (len != id.length - 1) {
				throw new IllegalArgumentException("cid length " + len + " doesn't match " + (id.length - 1) + "!");
			}
			cid = new byte[len];
			System.arraycopy(id, 1, cid, 0, len);
		}
		this.id = new ConnectionId(cid);
	}

	private ConnectionIdExtension(ConnectionId id) {
		super(ExtensionType.CONNECTION_ID);
		this.id = id;
	}

	public ConnectionId getConnectionId() {
		return id;
	}

	@Override
	protected void addExtensionData(final DatagramWriter writer) {
		int length = id.length();
		writer.write(1 + length, LENGTH_BITS);
		writer.write(length, CID_FIELD_LENGTH_BITS);
		writer.writeBytes(id.getBytes());
	}

	public static ConnectionIdExtension fromLength(int length) {
		if (length > 255) {
			throw new IllegalArgumentException("cid length too large! 255 max, but was " + length);
		} else if (length < 0) {
			throw new IllegalArgumentException("cid length must not be negative!");
		}
		byte[] connectionId;
		if (length == 0) {
			connectionId = Bytes.EMPTY;
		} else {
			connectionId = new byte[length];
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(connectionId);
		}
		return new ConnectionIdExtension(connectionId, true);
	}

	public static ConnectionIdExtension fromConnectionId(ConnectionId cid) {
		if (cid == null) {
			throw new NullPointerException("cid must not be null!");
		}
		return new ConnectionIdExtension(cid);
	}

	public static ConnectionIdExtension fromExtensionData(final byte[] extensionData) throws HandshakeException {
		if (extensionData == null) {
			throw new NullPointerException("cid must not be null!");
		} else if (extensionData.length == 0) {
			throw new IllegalArgumentException("cid length must not be 0");
		} else if (extensionData.length > 256) {
			throw new IllegalArgumentException("cid length too large! 255 max, but has " + (extensionData.length - 1));
		}
		return new ConnectionIdExtension(extensionData, false);
	}

	@Override
	public int getLength() {
		// 2 bytes indicating extension type, 2 bytes overall length,
		// 1 byte cid length + cid
		return 2 + 2 + 1 + id.length();
	}

}
