package ee.vvk.ivotingverification.util;

import android.net.Uri;
import android.util.Base64;
import android.util.Pair;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.x509.Certificate;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ee.vvk.ivotingverification.model.VerificationProfile;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.canonical.Canonicalizer;

public class BDocContainer {
    private static final String TAG = "BDOC";
    private static final String MIMETYPE_VALUE = "application/vnd.etsi.asic-e+zip";
    private static final String SIGNATURES_FILE_REGEX = "META-INF/(.*)signatures(.*).xml";
    private static final String MANIFEST = "META-INF/manifest.xml";
    private static final String NS_XMLDSIG = "http://www.w3.org/2000/09/xmldsig#";
    private static final String NS_ALL = "xmlns:asic=\"http://uri.etsi.org/02918/v1.2.1#\" " +
            "xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" " +
            "xmlns:xades=\"http://uri.etsi.org/01903/v1.3.2#\"";
    private static final int MAX_FILE_SIZE = 1024 * 1024; // 1MB
    private static final String RSA_OID = "1.2.840.113549.1.1.1";


    private final String ballotNameRegex;

    public enum SigningAlg {
        SHA256_RSA,
        SHA256_ECDSA
    }

    private final MessageDigest sha256;
    private final ZipInputStream zip;
    public byte[] manifest;
    public byte[] signature;
    private Map<String, byte[]> votes = new HashMap<>();
    public SigningAlg signingAlg;
    public X509Certificate cert;
    public X509Certificate issuer;
    private String canonSigFileStr;
    private String signerCN;

	// verifies data received from server. Throws exception on any validation error.
    public static BDocContainer getVerifiedContainer(
            VerificationProfile verificationProfile,
            byte[] containerData,
            byte[] ocspData,
            byte[] regData) throws Exception {

		BDocContainer container = new BDocContainer(
                new ByteArrayInputStream(containerData), verificationProfile.getPublicKey().elId);
		container.parseContainer();
		container.validateContainer(verificationProfile.getEsteidCerts());

		long producedAt = Ocsp.verifyResponse(new ByteArrayInputStream(ocspData),
                verificationProfile.getOcspCerts(), container.cert, container.issuer);

		long genTime = Pkix.verifyResponse(regData,
                verificationProfile.getTspregClientCert().getPublicKey(),
                verificationProfile.getTspregServiceCert(),
                container.getSignatureValueCanon());

		long d = genTime - producedAt;
		if (d < 0) {
			throw new Exception("PKIX predates OCSP");
		}
		if (d > Util.MAX_TIME_BETWEEN_OCSP_PKIX) {
			throw new Exception("PKIX and OCSP timestamps too far apart");
		}
		return container;
	}

    private BDocContainer(InputStream in, String electionName) {
        try {
            sha256 = MessageDigest.getInstance("SHA256");
        } catch (NoSuchAlgorithmException e) {
            // sha256 is always available, throw declaration to silence compiler
            throw new RuntimeException(e);
        }
        zip = new ZipInputStream(in);
        this.ballotNameRegex = String.format("^%s\\.[^.]+\\.ballot$", electionName);
    }

    public Map<String, byte[]> getVotes() {
        Map<String, byte[]> res = new HashMap<>();
        for (Map.Entry<String, byte[]> entry: votes.entrySet()) {
            String name = entry.getKey();
            res.put(name.substring(name.indexOf(".") + 1, name.lastIndexOf(".")), entry.getValue());
        }
        return res;
    }


    public void parseContainer() throws Exception {
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            Util.logDebug(TAG, "entry: " + entry.getName() + ", " + entry.getSize());

            if (entry.getName().equals("mimetype") && entry.getSize() == -1) {
                entry.setSize(31);
            }
            byte[] data = Util.toBytes(zip, MAX_FILE_SIZE);
            parseEntry(entry, data);
        }
    }

    private void parseEntry(ZipEntry entry, byte[] data) throws Exception {
        String entryName = entry.getName();
        if (isMimeType(entryName)) {
            validateMimeType(data);
        } else if (isManifest(entryName)) {
            manifest = data;
        } else if (isSignaturesFile(entryName)) {
            signature = data;
        } else if (isDataFile(entryName)) {
            votes.put(entryName, data);
        }
    }

    public void validateContainer(X509Certificate... issuers) throws Exception {
        Document doc = buildDoc();
        Element root = doc.getRootElement();
        canonSigFileStr = new String(canonicalizeDocument(doc));
        String signedPropertiesDigest = computeSignedPropertiesDigest(canonSigFileStr);
        verifyDigestValues(root, signedPropertiesDigest);
        Util.logInfo(TAG, "Container signature file digest values correct");

        byte[] signedInfo = getSignedInfo(canonSigFileStr);
        String sig = getSignature(root);
        PublicKey pk = getKey(root, issuers);
        verifySignature(signedInfo, sig, pk);
        Util.logInfo(TAG, "Container signature correct");
    }

    public String getSignerCN() {
        return signerCN;
    }

    private byte[] getSignedInfo(String canonStr) {
        String prefix = "<ds:SignedInfo";
        String suffix = "</ds:SignedInfo>";
        int prefixLen = prefix.length();

        String str = canonStr.substring(canonStr.indexOf(prefix), canonStr.indexOf(suffix)) + suffix;
        str = prefix + " " + NS_ALL + str.substring(prefixLen);
        return str.getBytes();
    }

    private PublicKey getKey(Element root, X509Certificate... issuers) throws Exception {
        Element node = root.getFirstChildElement("Signature", NS_XMLDSIG)
                .getFirstChildElement("KeyInfo", NS_XMLDSIG)
                .getFirstChildElement("X509Data", NS_XMLDSIG)
                .getFirstChildElement("X509Certificate", NS_XMLDSIG);
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "SC");
        byte[] certData = Base64.decode(node.getValue(), Base64.DEFAULT);
        cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(certData));
		signerCN = Util.getSubjectCN(cert);
		String issuerCN = Util.getIssuerCN(cert);

        try {
            issuer = Util.verifyCertIssuerSig(cert, issuers);
        } catch (Exception e) {
            throw new Exception("Vote signer cert for " + signerCN + " not properly signed by any of ESTEID certs: " + issuerCN);
        }

        try {
            return cert.getPublicKey();
        } catch (Exception e) {
            // This was probably caused by one of the misencoded Estonian RSA public keys: either
            // the modulus or exponent has an extra 0x00-byte prefix (negative encodings are
            // handled by SC itself). Parse the public key manually, resolving these errors. If it
            // turns out, that our assumption was wrong, then rethrow the original error.
            try {
                Certificate c = Certificate.getInstance(certData);
                SubjectPublicKeyInfo spki = c.getSubjectPublicKeyInfo();
                if (!RSA_OID.equals(spki.getAlgorithm().getAlgorithm().getId())) {
                    throw new Exception("Public key algorithm not RSA");
                }
                byte[] pub = spki.getPublicKeyData().getOctets();

                Pair<Integer, Integer> modexpRange = asn1Range(0x30, pub);
                if (modexpRange.second != pub.length) {
                    throw new Exception("Public key has trailing garbage");
                }
                byte[] modexp = Arrays.copyOfRange(pub, modexpRange.first, modexpRange.second);

                Pair<Integer, Integer> modRange = asn1Range(0x02, modexp);
                byte[] mod = Arrays.copyOfRange(modexp, modRange.first, modRange.second);

                byte[] exp = Arrays.copyOfRange(modexp, modRange.second, modexp.length);
                Pair<Integer, Integer> expRange = asn1Range(0x02, exp);
                if (expRange.second != exp.length) {
                    throw new Exception("RSA sequence has trailing garbage");
                }
                exp = Arrays.copyOfRange(exp, expRange.first, expRange.second);

                KeyFactory kf = KeyFactory.getInstance("RSA", "SC");
                return kf.generatePublic(new RSAPublicKeySpec(
                        new BigInteger(1, mod), new BigInteger(1, exp)));
            } catch (Exception exception) {
                Util.logDebug(TAG, "Ignored Exception, rethrowing original", exception);
                throw e;
            }
        }
    }

    private static Pair<Integer, Integer> asn1Range(int tag, byte[] der) throws Exception {
        if (der[0] != tag) {
            throw new Exception(String.format("ASN.1 tag 0x%x is not 0x%x", der[0], tag));
        }
        int length = der[1] & 0xff;
        int from = 2;
        if (length > 127) {
            int size = length & 0x7f;
            length = 0;
            for (; size > 0; size--) {
                length = (length << 8) + (der[from++] & 0xff);
            }
        }
        return new Pair<>(from, from + length);
    }

    private String getSignature(Element root) {
        Element node = root.getFirstChildElement("Signature", NS_XMLDSIG)
                    .getFirstChildElement("SignatureValue", NS_XMLDSIG);
        return node.getValue().replace("\n", "");
    }

    private void verifySignature(byte[] signedInfo, String sig, PublicKey pk) throws Exception {
        String signingAlgStr = null;
        switch (signingAlg) {
            case SHA256_RSA:
                signingAlgStr = "SHA256withRSA";
                break;
            case SHA256_ECDSA:
                signingAlgStr = "SHA256withECDSA";
                break;
        }
        Signature signature = Signature.getInstance(signingAlgStr, "SC");
        signature.initVerify(pk);
        signature.update(signedInfo);
        byte[] sigBytes = Base64.decode(sig, Base64.NO_WRAP);
        if (signingAlg == SigningAlg.SHA256_ECDSA) {
            byte[] tmp = new byte[sigBytes.length / 2];
            System.arraycopy(sigBytes, 0, tmp, 0, tmp.length);
            BigInteger r = new BigInteger(1, tmp);
            System.arraycopy(sigBytes, tmp.length, tmp, 0, tmp.length);
            BigInteger s = new BigInteger(1, tmp);
            ASN1Integer rI = new ASN1Integer(r);
            ASN1Integer sI = new ASN1Integer(s);
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(rI);
            v.add(sI);
            sigBytes = new DERSequence(v).getEncoded();
        }
        if (!signature.verify(sigBytes)) {
            Util.logDebug(TAG, "container signature did not verify successfully");
            throw new Exception();
        }
    }

    private String computeSignedPropertiesDigest(String canonStr) {
        String prefix = "<xades:SignedProperties";
        String suffix = "</xades:SignedProperties>";
        int prefixLen = prefix.length();

        String str = canonStr.substring(canonStr.indexOf(prefix), canonStr.indexOf(suffix)) + suffix;
        str = prefix + " " + NS_ALL + str.substring(prefixLen);
        return Base64.encodeToString(sha256.digest(str.getBytes()), Base64.NO_WRAP);
    }

    private void verifyDigestValues(Element root, String signedPropertiesDigest) throws Exception {
        Element node = root.getFirstChildElement("Signature", NS_XMLDSIG)
                .getFirstChildElement("SignedInfo", NS_XMLDSIG);
        String signingAlgStr = node.getFirstChildElement("SignatureMethod", NS_XMLDSIG).getAttributeValue("Algorithm");
        switch (signingAlgStr) {
            case "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256":
                signingAlg = SigningAlg.SHA256_RSA;
                break;
            case "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256":
                signingAlg = SigningAlg.SHA256_ECDSA;
                break;
            default:
                Util.logError(TAG, "Invalid signing algorithm: " + signingAlgStr);
                throw new Exception();
        }
        Elements references = node.getChildElements("Reference", NS_XMLDSIG);
        for (int i = 0; i < references.size(); i++) {
            String type = references.get(i).getAttributeValue("Type");
            String uri = references.get(i).getAttributeValue("URI");
            String expectedDigestValue = references.get(i)
                    .getFirstChildElement("DigestValue", NS_XMLDSIG).getValue().replace("\n", "");
            if (type != null && type.equals("http://uri.etsi.org/01903#SignedProperties")) {
                if (!expectedDigestValue.equals(signedPropertiesDigest)) {
                    Util.logError(TAG, "Signature and computed digest values for 'SignedProperties' do not match\n" +
                                "expected: " + expectedDigestValue + "\n" +
                                "computed: " + signedPropertiesDigest);
                    throw new Exception();
                }
            } else {
                byte[] digestData = votes.get(Uri.decode(uri));
                if (digestData == null) {
                    Util.logError(TAG, "Reference uri in signature does not match any file in container: " + uri);
                    throw new Exception();
                }
                String computedDigest = Base64.encodeToString(sha256.digest(digestData), Base64.NO_WRAP);
                if (!computedDigest.equals(expectedDigestValue)) {
                    Util.logError(TAG, "Signature and computed digest values do not match: " + uri + "\n" +
                        "expected: " + expectedDigestValue + "\n" +
                        "computed: " + computedDigest);
                    throw new Exception();
                }
            }
        }
    }

    private Document buildDoc() throws ParsingException, IOException {
        InputStream is = new ByteArrayInputStream(signature);
        Builder parser = new Builder();
        return parser.build(is);
    }

    private byte[] canonicalizeDocument(Document doc) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Canonicalizer canonicalizer = new Canonicalizer(os, Canonicalizer.CANONICAL_XML_11);
        canonicalizer.write(doc);
        return os.toByteArray();
    }

    private void validateMimeType(byte[] data) throws Exception {
        String str = new String(data);
        Util.logDebug(TAG, "mimetype value: " + str);
        if (!str.equals(MIMETYPE_VALUE)) {
            throw new Exception("Incorrect bdoc mimetype value");
        }
    }

    private boolean isMimeType(String entryName) {
        return entryName.equalsIgnoreCase("mimetype");
    }

    private boolean isDataFile(String entryName) {
        return !entryName.startsWith("META-INF/") && !isMimeType(entryName)
                && entryName.matches(ballotNameRegex);
    }

    private boolean isManifest(String entryName) {
        return entryName.equalsIgnoreCase(MANIFEST);
    }

    private boolean isSignaturesFile(String entryName) {
        return entryName.matches(SIGNATURES_FILE_REGEX);
    }

    public byte[] getSignatureValueCanon() {
        String prefix = "<ds:SignatureValue";
        String suffix = "</ds:SignatureValue>";
        int prefixLen = prefix.length();

        String str = canonSigFileStr.substring(canonSigFileStr.indexOf(prefix),
                canonSigFileStr.indexOf(suffix)) + suffix;
        str = prefix + " " + NS_ALL + str.substring(prefixLen);
        return str.getBytes();

    }
}
