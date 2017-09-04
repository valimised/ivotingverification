package ee.vvk.ivotingverification.util;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * Regular expressions matcher.
 * 
 * @version 16.05.2013
 */
public class RegexMatcher {

	private static final String BASE_64_REGEX =
			"^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";

	public static boolean IsCandidateNumber(String s) {
		return s.matches("\\d{1,10}.\\d{1,11}");
	}

	public static boolean isCorrectQR(String value){

		String[] splitQr = value.split("\n");

		// QR code has 3 lines
		if (splitQr.length != 3) {
			return false;
		}
		// Session log ID, seed and session ID are base64 encoded
		if (!splitQr[1].matches(BASE_64_REGEX) || !splitQr[2].matches(BASE_64_REGEX)) {
			return false;
		}

		return true;
	}

	public static boolean isValidUTF8(final byte[] bytes) {
		try {
			Charset.availableCharsets().get("UTF-8").newDecoder()
					.decode(ByteBuffer.wrap(bytes));
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}

	public static boolean IsLessThan101UtfChars(String s) {
		if (s.length() < 101 && isValidUTF8(s.getBytes())) {
			return true;
		}
		return false;
	}
}
