package ee.vvk.ivotingverification.util;

import android.util.Log;

import org.spongycastle.asn1.x509.ExtendedKeyUsage;
import org.spongycastle.asn1.x509.KeyPurposeId;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.ocsp.BasicOCSPResp;
import org.spongycastle.cert.ocsp.CertificateID;
import org.spongycastle.cert.ocsp.CertificateStatus;
import org.spongycastle.cert.ocsp.OCSPResp;
import org.spongycastle.cert.ocsp.SingleResp;
import org.spongycastle.jce.X509Principal;
import org.spongycastle.operator.ContentVerifierProvider;
import org.spongycastle.operator.jcajce.JcaContentVerifierProviderBuilder;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class Ocsp {
    private static final String TAG = "OCSP";

    public static long verifyResponse(InputStream response, List<ContentVerifierProvider> ocspVerifiers,
                                      X509Certificate requestedCert, X509Certificate issuerCert) throws Exception {
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
            Log.i(TAG, "Preconfigured OCSP responders not suitable, trying AIA");
            verified = checkAIAResponder(basicResp, issuerCert);
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

            byte[] dn;
            try {
                dn = digest.digest(((X509Principal) requestedCert.getIssuerDN()).getEncoded());
            } catch (Exception e) {
                dn = digest.digest(new X509Principal(true, requestedCert.getIssuerDN().getName()).getEncoded());
            }

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

    private static boolean checkAIAResponder(BasicOCSPResp basicResp, X509Certificate issuerCert) throws Exception {
        JcaContentVerifierProviderBuilder builder = new JcaContentVerifierProviderBuilder().setProvider("SC");
        ContentVerifierProvider issuerVerifier = builder.build(issuerCert);
        for (X509CertificateHolder responder : basicResp.getCerts()) {
            // is signed by same issuer as the cert whose ocsp be are checking
            if (responder.isSignatureValid(issuerVerifier)) {
                // and has proper signature of the responder
                if (basicResp.isSignatureValid(builder.build(responder))) {
                    ExtendedKeyUsage keyUsage = ExtendedKeyUsage.fromExtensions(responder.getExtensions());
                    // and responder has proper key extension
                    if (keyUsage.hasKeyPurposeId(KeyPurposeId.id_kp_OCSPSigning)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
