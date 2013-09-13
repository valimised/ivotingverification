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

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

import ext.org.bouncycastle.asn1.ASN1InputStream;
import ext.org.bouncycastle.asn1.ASN1Sequence;
import ext.org.bouncycastle.asn1.DERBitString;
import ext.org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import ext.org.bouncycastle.crypto.InvalidCipherTextException;
import ext.org.bouncycastle.crypto.encodings.OAEPEncoding;
import ext.org.bouncycastle.crypto.engines.RSAEngine;
import ext.org.bouncycastle.crypto.params.ParametersWithRandom;
import ext.org.bouncycastle.crypto.params.RSAKeyParameters;
import ext.org.bouncycastle.util.encoders.Hex;
import ext.org.bouncycastle.util.io.pem.PemObject;
import ext.org.bouncycastle.util.io.pem.PemReader;

/**
 * RSA-OAEP modification for the brute force verification.
 * 
 * @version 25.04.2013
 */
public class Crypto {

	private static String lastRandom;

	public static byte[] encrypt(String data, String random, String strkey)
			throws IOException, InvalidCipherTextException {
		return encrypt(data.getBytes(Util.ENCODING), new SecureRandomWrapper(
				random.getBytes(Util.ENCODING)), strkey);
	}

	public static byte[] encrypt(byte[] data, SecureRandomWrapper random,
			String strkey) throws IOException, InvalidCipherTextException {

		RSAKeyParameters key = readKey(strkey);

		OAEPEncoding engine = new OAEPEncoding(new RSAEngine());
		engine.init(true, new ParametersWithRandom(key, random));

		int bsz = engine.getInputBlockSize();
		byte[] res = Hex.encode(engine.processBlock(data, 0,
				Math.min(bsz, data.length)));

		lastRandom = new String(random.getLastBytes(), Util.ENCODING);
		return res;
	}

	private static RSAKeyParameters readKey(String pemstr) throws IOException {
		PemReader reader = null;
		PemObject pem;
		try {
			StringReader rr = new StringReader(pemstr);
			reader = new PemReader(rr);
			pem = reader.readPemObject();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		ASN1InputStream stream = null;
		ASN1Sequence seq;
		try {
			stream = new ASN1InputStream(pem.getContent());
			seq = (ASN1Sequence) stream.readObject();

			Enumeration enm = seq.getObjects();
			enm.nextElement();

			stream = new ASN1InputStream(
					((DERBitString) enm.nextElement()).getBytes());
			seq = (ASN1Sequence) stream.readObject();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		RSAPublicKeyStructure pks = new RSAPublicKeyStructure(seq);
		return new RSAKeyParameters(false, pks.getModulus(),
				pks.getPublicExponent());
	}

	public static String getLastRandom() {
		return lastRandom;
	}
}