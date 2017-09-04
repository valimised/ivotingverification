package ee.vvk.ivotingverification.util;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DERSequence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    private String ballotNameRegex;

    public enum SigningAlg {
        SHA256_RSA,
        SHA256_ECDSA
    }

    private final MessageDigest sha256;
    private final ZipInputStream zip;
    private final X509Certificate[] issuers;
    public byte[] manifest;
    public byte[] signature;
    private Map<String, byte[]> votes = new HashMap<>();
    public SigningAlg signingAlg;
    public X509Certificate cert;
    private String canonSigFileStr;

    public BDocContainer(InputStream in, String electionName, X509Certificate... issuers) {
        try {
            sha256 = MessageDigest.getInstance("SHA256");
        } catch (NoSuchAlgorithmException e) {
            // sha256 is always available, throw declaration to silence compiler
            throw new RuntimeException(e);
        }
        zip = new ZipInputStream(in);
        this.ballotNameRegex = String.format("^%s\\.[^.]+\\.ballot$", electionName);
        this.issuers = issuers;
    }

    public void readAndValidate() throws Exception {
        parseContainer();
        validateContainer();
    }

    public Map<String, byte[]> getVotes() {
        Map<String, byte[]> res = new HashMap<>();
        for (Map.Entry<String, byte[]> entry: votes.entrySet()) {
            String name = entry.getKey();
            res.put(name.substring(name.indexOf(".") + 1, name.lastIndexOf(".")), entry.getValue());
        }
        return res;
    }


    private void parseContainer() throws Exception {
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null)
        {
            if (Util.DEBUGGABLE) {
                Log.d(TAG, "entry: " + entry.getName() + ", " + entry.getSize());
            }

            if (entry.getName().equals("mimetype") && entry.getSize()==-1) {
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

    private void validateContainer() throws Exception {
        Document doc = buildDoc();
        Element root = doc.getRootElement();
        canonSigFileStr = new String(canonicalizeDocument(doc));
        String signedPropertiesDigest = computeSignedPropertiesDigest(canonSigFileStr);
        verifyDigestValues(root, signedPropertiesDigest);
        if (Util.DEBUGGABLE) {
            Log.i(TAG, "Container signature file digest values correct");
        }
        byte[] signedInfo = getSignedInfo(canonSigFileStr);
        String sig = getSignature(root);
        PublicKey pk = getKey(root);
        verifySignature(signedInfo, sig, pk);
        if (Util.DEBUGGABLE) {
            Log.i(TAG, "Container signature correct");
        }
    }

    private byte[] getSignedInfo(String canonStr) {
        String prefix = "<ds:SignedInfo";
        String suffix = "</ds:SignedInfo>";
        int prefixLen = prefix.length();

        String str = canonStr.substring(canonStr.indexOf(prefix), canonStr.indexOf(suffix)) + suffix;
        str = prefix + " " + NS_ALL + str.substring(prefixLen);
        return str.getBytes();
    }

    private PublicKey getKey(Element root) throws Exception {
        Element node = root.getFirstChildElement("Signature", NS_XMLDSIG)
                .getFirstChildElement("KeyInfo", NS_XMLDSIG)
                .getFirstChildElement("X509Data", NS_XMLDSIG)
                .getFirstChildElement("X509Certificate", NS_XMLDSIG);
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "SC");
        cert = (X509Certificate)cf.generateCertificate(
                new ByteArrayInputStream(Base64.decode(node.getValue(), Base64.DEFAULT)));

        try {
            Util.verifyCertIssuerSig(cert, issuers);
        } catch (Exception e) {
            throw new Exception("Vote signer cert not properly signed by any of ESTEID certs");
        }
        return cert.getPublicKey();
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
            if (Util.DEBUGGABLE) {
                Log.e(TAG, "container signature did not verify successfully");
            }
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
                if (Util.DEBUGGABLE) {
                    Log.e(TAG, "Invalid signing algorithm: " + signingAlgStr);
                }
                throw new Exception();
        }
        Elements references = node.getChildElements("Reference", NS_XMLDSIG);
        for (int i = 0; i < references.size(); i++) {
            String type = references.get(i).getAttributeValue("Type");
            String uri = references.get(i).getAttributeValue("URI");
            String expectedDigestValue = references.get(i)
                    .getFirstChildElement("DigestValue", NS_XMLDSIG).getValue().replace("\n", "");
            if (type.equals("http://uri.etsi.org/01903#SignedProperties")) {
                if (!expectedDigestValue.equals(signedPropertiesDigest)) {
                    if (Util.DEBUGGABLE) {
                        Log.e(TAG, "Signature and computed digest values for 'SignedProperties' do not match\n" +
                                "expected: " + expectedDigestValue + "\n" +
                                "computed: " + signedPropertiesDigest);
                    }
                    throw new Exception();
                }
            } else {
                byte[] digestData = votes.get(Uri.decode(uri));
                if (digestData == null) {
                    if (Util.DEBUGGABLE) {
                        Log.e(TAG, "Reference uri in signature does not match any file in container: " + uri);
                    }
                    throw new Exception();
                }
                String computedDigest = Base64.encodeToString(sha256.digest(digestData), Base64.NO_WRAP);
                if (!computedDigest.equals(expectedDigestValue)) {
                    if (Util.DEBUGGABLE) {
                        Log.e(TAG, "Signature and computed digest values do not match: " + uri + "\n" +
                        "expected: " + expectedDigestValue + "\n" +
                        "computed: " + computedDigest);
                    }
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
        if (Util.DEBUGGABLE) {
            Log.d(TAG, "mimetype value: " + str);
        }
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