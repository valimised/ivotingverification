package ee.vvk.ivotingverification.util;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.SignerInformationVerifier;
import org.spongycastle.tsp.TSPValidationException;
import org.spongycastle.tsp.TimeStampToken;
import org.spongycastle.tsp.TimeStampTokenInfo;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;

public class Pkix {
    public static long verifyResponse(byte[] response, PublicKey collectorPub,
                                      SignerInformationVerifier sigVerifier, byte[] msg) throws Exception {
        TimeStampToken token = new TimeStampToken(new CMSSignedData(response));
        TimeStampTokenInfo tokenInfo = token.getTimeStampInfo();

        // Msg digest and msg imprint in Pkix response have to match
        MessageDigest digest = MessageDigest.getInstance(tokenInfo.getMessageImprintAlgOID().toString(), "SC");
        if (!Arrays.equals(digest.digest(msg), tokenInfo.getMessageImprintDigest())) {
            throw new Exception("MessageImprints of input data and TSP response do not match");
        }

        // Nonce has to have proper structure
        Nonce nonce;
        try {
            nonce = new Nonce(tokenInfo.getNonce().toByteArray());
        } catch (Exception e) {
            throw new Exception("Invalid nonce asn1 structure in PKIX response");
        }

        // Collector's signature on the msg has to be verified with trusted key
        Signature signature = Signature.getInstance(nonce.oid, "SC");
        signature.initVerify(collectorPub);
        signature.update(msg);
        if (!signature.verify(nonce.sig)) {
            throw new Exception("Verification of collector's signature in nonce failed");
        }

        // Pkix responser signature on the response has to verified with trusted certificate
        try {
            token.validate(sigVerifier);
        } catch (TSPValidationException e) {
            throw new Exception("TSP response invalid: " + e.getMessage(), e);
        }
        return tokenInfo.getGenTime().getTime();
    }

    private static class Nonce {
        private final String oid;
        private final byte[] sig;

        private Nonce(byte[] nonce) throws Exception {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(nonce));
            ASN1Sequence root = (ASN1Sequence) in.readObject();
            oid = ((ASN1ObjectIdentifier) ((ASN1Sequence) root.getObjectAt(0)).getObjectAt(0)).getId();
            sig = ((DEROctetString) root.getObjectAt(1)).getOctets();
        }
    }
}
