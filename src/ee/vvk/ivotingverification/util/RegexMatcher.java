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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * Regular expressions matcher.
 * 
 * @version 16.05.2013
 */
public class RegexMatcher {

	public final static boolean isElevenDigets(String s) {
		return s.matches("\\d{11}");
	}

	public final static boolean IsOneDigit(String s) {
		return s.matches("\\d{1}");
	}

	public final static boolean IsOneOrTwoDigits(String s) {
		return s.matches("\\d{1,2}");
	}

	public final static boolean IsCandidateNumber(String s) {
		return s.matches("\\d{1,10}.\\d{1,11}");
	}

	public final static boolean IsFortyCharacters(String s) {
		if (s.length() == 40) {
			return true;
		}
		return false;
	}
	
	public static boolean isCorrectQR(String value){
		if(value.matches("^\\w{40}\n(\\w{1,28}\t([A-Fa-f0-9]){40}\n){1,5}")){
			return true;
		}	
		return false;
	}

	public final static boolean Is256Bytes(String s) {
		if (s.length() == 256) {
			return true;
		}
		return false;
	}

	public final static boolean Is512Bytes(String s) {
		if (s.length() == 512) {
			return true;
		}
		return false;
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

	public final static boolean IsLessThan101UtfChars(String s) {
		if (s.length() < 101 && isValidUTF8(s.getBytes())) {
			return true;
		}
		return false;
	}
}
