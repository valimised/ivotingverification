package ee.vvk.ivotingverification.util;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;


public class RegexMatcher {

	private static final String SESSION_ID_REGEX = "^[0-9a-f]{32}$";
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

		// Session log ID is hex-encoding of 16-byte string.
		if (!splitQr[0].matches(SESSION_ID_REGEX)) {
			return false;
		}

		// Random seed and vote ID are base64 encoded with maximum
		// encoded length 24 for the vote ID (decoded length 16).
		if (!isBase64(splitQr[1]) || !isBase64(splitQr[2], 24)) {
			return false;
		}

		return true;
	}

	private static boolean isBase64(String s) {
		return s.matches(BASE_64_REGEX);
	}

	private static boolean isBase64(String s, int maxlen) {
		return isBase64(s) && s.length() <= maxlen;
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
		return s.length() < 101 && isValidUTF8(s.getBytes());
	}
}
