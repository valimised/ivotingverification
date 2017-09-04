package ee.vvk.ivotingverification.util;

import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;

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
