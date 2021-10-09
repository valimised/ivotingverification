package ee.vvk.ivotingverification.model;

import org.spongycastle.cms.SignerInformationVerifier;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.spongycastle.operator.ContentVerifierProvider;
import org.spongycastle.operator.jcajce.JcaContentVerifierProviderBuilder;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import ee.vvk.ivotingverification.util.ElGamalPub;
import ee.vvk.ivotingverification.util.Util;

public class VerificationProfile {

    private final ElGamalPub publicKey;
    private final X509Certificate[] esteidCerts;
    private final List<ContentVerifierProvider> ocspCerts;
    private final SignerInformationVerifier tspregServiceCert;
    private final X509Certificate tspregClientCert;


    public static VerificationProfile loadVerificationProfile(
            String publicKey,
            X509Certificate[] esteidCerts,
            String[] ocspCerts,
            String tspregServiceCert,
            String tspregClientCert
            ) throws Exception {

        ElGamalPub pub = new ElGamalPub(publicKey);

        JcaContentVerifierProviderBuilder builder = new JcaContentVerifierProviderBuilder().setProvider("SC");
        List<ContentVerifierProvider> ocspVerifierProviders = new ArrayList<>();
        for (String ocspCertStr: ocspCerts) {
            ocspVerifierProviders.add(builder.build(Util.loadCertificate(ocspCertStr)));
        }

        SignerInformationVerifier tspregCert = new JcaSimpleSignerInfoVerifierBuilder().build(Util.loadCertificate(tspregServiceCert));
        X509Certificate collectorCert = Util.loadCertificate(tspregClientCert);

        return new VerificationProfile(pub, esteidCerts, ocspVerifierProviders, tspregCert, collectorCert);
    }

    public VerificationProfile(
            ElGamalPub publicKey,
            X509Certificate[] esteidCerts,
            List<ContentVerifierProvider> ocspCerts,
            SignerInformationVerifier tspregServiceCert,
            X509Certificate tspregClientCert) {
        this.publicKey = publicKey;
        this.esteidCerts = esteidCerts;
        this.ocspCerts = ocspCerts;
        this.tspregServiceCert = tspregServiceCert;
        this.tspregClientCert = tspregClientCert;
    }

    public ElGamalPub getPublicKey() {
        return publicKey;
    }

    public X509Certificate[] getEsteidCerts() {
        return esteidCerts;
    }

    public List<ContentVerifierProvider> getOcspCerts() {
        return ocspCerts;
    }

    public SignerInformationVerifier getTspregServiceCert() {
        return tspregServiceCert;
    }

    public X509Certificate getTspregClientCert() {
        return tspregClientCert;
    }


}
