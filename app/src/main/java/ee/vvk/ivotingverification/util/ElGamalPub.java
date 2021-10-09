package ee.vvk.ivotingverification.util;

import android.util.Base64;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERGeneralString;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

public class ElGamalPub {

	private static final String TAG = ElGamalPub.class.getSimpleName();

    private static final String BEGIN_PUB_KEY = "-----BEGIN PUBLIC KEY-----";
    private static final String END_PUB_KEY = "-----END PUBLIC KEY-----";

    public String elId;
    public BigInteger key;
    public BigInteger p;
    public BigInteger g;
    public BigInteger q;


    public ElGamalPub(String in) throws Exception {
        in = in.trim();
        if (!in.startsWith(BEGIN_PUB_KEY) || !in.endsWith(END_PUB_KEY)) {
            throw new Exception("The key does not have expected format");
        }
        in = in.substring(in.indexOf(BEGIN_PUB_KEY) + BEGIN_PUB_KEY.length(),
                in.indexOf(END_PUB_KEY));

        ASN1InputStream a = new ASN1InputStream(Base64.decode(in, Base64.DEFAULT));
        ASN1Primitive p;
        try {
            p = a.readObject();
        } catch (IOException e) {
            throw new Exception("Invalid public key ASN1");
        }
        SubjectPublicKeyInfo spki;
        spki = SubjectPublicKeyInfo.getInstance(p);
        key = ((ASN1Integer)((ASN1Sequence) spki.parsePublicKey()).getObjectAt(0)).getPositiveValue();
        ASN1Sequence k = (ASN1Sequence) spki.getAlgorithm().getParameters();
        ASN1Encodable[] params = k.toArray();
        this.p = ((ASN1Integer) params[0]).getValue();
        this.g = ((ASN1Integer) params[1]).getValue();
        this.elId = ((DERGeneralString) params[2]).getString();
        this.q = this.p.subtract(BigInteger.ONE).shiftRight(1);
    }

    public String getDecryptedChoice(byte[] in, byte[] rnd) throws Exception {
        BigInteger choice = getChoice(in);

        BigInteger factor = key.modPow(new BigInteger(1, rnd), p);
        BigInteger factorInverse = factor.modInverse(p);
        BigInteger s = factorInverse.multiply(choice).mod(p);
        if (!s.modPow(q, p).equals(BigInteger.ONE)) {
            Util.logDebug(TAG, "Error: Plaintext is not quadratic residue (s.modPow(q,p) != 1)");
            return null;
        }
        BigInteger m = s.compareTo(q) > 0 ? p.subtract(s) : s;
        return stripPadding(m.toByteArray());
    }

    private BigInteger getChoice(byte[] in) throws IOException {
        ASN1InputStream bIn = new ASN1InputStream(new ByteArrayInputStream(in));
        ASN1Sequence parentObj = (ASN1Sequence) ((ASN1Sequence) bIn.readObject()).getObjectAt(1);
        String c2 = parentObj.getObjectAt(1).toString();
        return new BigInteger(c2, 10);
    }

    private String stripPadding(byte[] in) throws Exception {
        if (in.length < 2) {
            throw new Exception("Source message can not contain padding");
        }
        // As the plaintext byte array is obtained from BigInteger, leading 0 is omitted
        if (in.length + 1 != p.bitLength() / 8) {
            throw new Exception("Incorrect plaintext length");
        }
        if (in[0] != 0x01) {
            throw new Exception("Incorrect padding head");
        }
        for (int i = 1; i < in.length; i++) {
            switch (in[i]) {
                case 0:
                    // found padding end
                    return new String(Arrays.copyOfRange(in, i + 1, in.length));
                case (byte) 0xff:
                    continue;
                default:
                    // incorrect padding byte
                    throw new Exception("Incorrect padding byte");
            }
        }
        throw new Exception("Padding unexpected");
    }
}
