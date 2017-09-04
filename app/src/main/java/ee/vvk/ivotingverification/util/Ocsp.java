package ee.vvk.ivotingverification.util;

import android.util.Log;

import org.spongycastle.cert.ocsp.BasicOCSPResp;
import org.spongycastle.cert.ocsp.CertificateID;
import org.spongycastle.cert.ocsp.CertificateStatus;
import org.spongycastle.cert.ocsp.OCSPResp;
import org.spongycastle.cert.ocsp.SingleResp;
import org.spongycastle.jce.X509Principal;
import org.spongycastle.operator.ContentVerifierProvider;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class Ocsp {
    private static final String TAG = "OCSP";

    public static long verifyResponse(InputStream response, List<ContentVerifierProvider> ocspVerifiers,
                                      X509Certificate requestedCert) throws Exception {
        OCSPResp ocspResp = new OCSPResp(response);
        if (ocspResp.getStatus() != OCSPResp.SUCCESSFUL) {
            throw new Exception("Ocsp response status value not 0: " + ocspResp.getStatus());
        }
        Object respObject = ocspResp.getResponseObject();
        if (!(respObject instanceof BasicOCSPResp)) {
            throw new Exception("Ocsp response is not basic");
        }
        BasicOCSPResp basicResp = (BasicOCSPResp) respObject;

        boolean verified = false;
        for (ContentVerifierProvider verifier: ocspVerifiers) {
            if (basicResp.isSignatureValid(verifier)) {
                verified = true;
                break;
            }
        }
        if (!verified) {
            throw new Exception("Signature verification failed");
        }

        byte[] authorityKeyId = requestedCert.getExtensionValue("2.5.29.35");
        if (authorityKeyId != null) {
            authorityKeyId = Arrays.copyOfRange(authorityKeyId, 6, authorityKeyId.length);
        }

        SingleResp[] responses = basicResp.getResponses();
        for (SingleResp resp: responses) {
            if (resp.getCertStatus() != CertificateStatus.GOOD) {
                if (Util.DEBUGGABLE) {
                    Log.w(TAG, "Ocsp SingleResponse cert status not GOOD: " + resp.getCertStatus());
                }
                continue;
            }
            CertificateID id = resp.getCertID();
            if (!requestedCert.getSerialNumber().equals(id.getSerialNumber())) {
                if (Util.DEBUGGABLE) {
                    Log.w(TAG, "Cert's and Ocsp responses serial numbers do not match");
                }
                continue;
            }
            MessageDigest digest = MessageDigest.getInstance(id.getHashAlgOID().toString(), "SC");
            if (authorityKeyId != null && !Arrays.equals(authorityKeyId, id.getIssuerKeyHash())) {
                if (Util.DEBUGGABLE) {
                    Log.w(TAG, "Cert's and ocsp's authority key hash do not match");
                }
                continue;
            }
            byte[] dn = digest.digest(((X509Principal) requestedCert.getIssuerDN()).getEncoded());
            if (!Arrays.equals(dn, id.getIssuerNameHash())) {
                if (Util.DEBUGGABLE) {
                    Log.w(TAG, "Cert's and ocsp's issuer DN hash do not match");
                }
                continue;
            }
            // reach here if all the checks are successful
            return basicResp.getProducedAt().getTime();
        }
        throw new Exception("Couldn't verify ocsp response");
    }
}
