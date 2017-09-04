package ee.vvk.ivotingverification.util;

import android.util.Base64;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERGeneralString;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;

import java.io.IOException;
import java.math.BigInteger;

public class ElGamalPub {
    private static final String BEGIN_PUB_KEY = "-----BEGIN PUBLIC KEY-----";
    private static final String END_PUB_KEY = "-----END PUBLIC KEY-----";

    private ASN1ObjectIdentifier oid;
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
        oid = spki.getAlgorithm().getAlgorithm();
        key = ((ASN1Integer)((ASN1Sequence) spki.parsePublicKey()).getObjectAt(0)).getPositiveValue();
        ASN1Sequence k = (ASN1Sequence) spki.getAlgorithm().getParameters();
        ASN1Encodable[] params = k.toArray();
        this.p = ((ASN1Integer) params[0]).getValue();
        this.g = ((ASN1Integer) params[1]).getValue();
        this.elId = ((DERGeneralString) params[2]).getString();
        this.q = this.p.subtract(BigInteger.ONE).shiftRight(1);
    }
}
