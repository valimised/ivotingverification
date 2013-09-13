/**
 * Copyright (C) 2013 Eesti Vabariigi Valimiskomisjon 
 * (Estonian National Electoral Committee), www.vvk.ee
 *
 * Written in 2013 by AS Finestmedia, www.finestmedia.ee
 * 
 * Vote-verification application for Estonian Internet voting system
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
 
package ee.vvk.ivotingverification.util;

import java.security.SecureRandom;

import ext.org.bouncycastle.util.encoders.Hex;

/**
 * Random generator with memory - used for OAEP padding manipulation
 * 
 * @version 25.04.2013
 */
public class SecureRandomWrapper extends SecureRandom {

	private byte[] next;
	private byte[] last;

	public SecureRandomWrapper() {
	}

	public SecureRandomWrapper(byte[] next) {
		setNextBytes(next);
	}

	@Override
	public void nextBytes(byte[] bytes) {
		if (next != null) {
			if (bytes.length != next.length) {
				throw new IllegalArgumentException(
						"bytes.length != next.length");
			}
			System.arraycopy(next, 0, bytes, 0, next.length);
			last = next;
			next = null;
		} else {
			super.nextBytes(bytes);
			last = new byte[bytes.length];
			System.arraycopy(bytes, 0, last, 0, bytes.length);
		}
	}

	public final void setNextBytes(byte[] bytes) {
		next = bytes == null ? null : Hex.decode(bytes);
	}

	public byte[] getLastBytes() {
		return last == null ? null : Hex.encode(last);
	}
}
