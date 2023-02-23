package ee.vvk.ivotingverification.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ee.vvk.ivotingverification.ErrorActivity;
import ee.vvk.ivotingverification.NetworkErrorActivity;
import ee.vvk.ivotingverification.VersionErrorActivity;
import ee.vvk.ivotingverification.R;
import ee.vvk.ivotingverification.dialog.LoadingSpinner;

/**
 * Utilities.
 * 
 * @version 16.05.2013
 */
public class Util {

	public final static String VOTE_CONTAINER_INFO = "ee.vvk.ivotingverification.VOTE_CONTAINER_INFO";
	public final static String ERROR_MESSAGE = "ee.vvk.ivotingverification.ERROR_MESSAGE";
	public final static String QR_CODE_CONTENTS = "ee.vvk.ivotingverification.QR_CODE_CONTENTS";
	public final static String EXIT = "ee.vvk.ivotingverification.EXIT";
	public final static String RESULT = "ee.vvk.ivotingverification.RESULT";
	public final static String ENCODING = "UTF-8";
	public final static int PERMISSION_REQUEST_CAMERA = 1;

	public final static long MAX_TIME_BETWEEN_OCSP_PKIX = 1000 * 60 * 15; // 15 minutes in ms
	public final static long VIBRATE_DURATION = 350L;

	public static boolean DEBUGGABLE = false;

	// Models where camera can't be rotated to portrait
	public static Set<String> SpecialModels = new HashSet<>(Arrays.asList("Samsung GT-S6102", "Samsung GT-S5360",
			"Samsung GT-S5660", "Samsung YP-G1", "Samsung YP-G70"));


	public static void logException(String tag, Exception exc) {
		if (DEBUGGABLE) {
			Log.e(tag, "", exc);
		}
	}

	public static void logDebug(String tag, String msg) {
		if (DEBUGGABLE) {
			Log.d(tag, msg);
		}
	}
	public static void logDebug(String tag, String msg, Exception exc) {
		if (DEBUGGABLE) {
			Log.d(tag, msg, exc);
		}
	}

	public static void logInfo(String tag, String msg) {
		if (DEBUGGABLE) {
			Log.i(tag, msg);
		}
	}
	public static void logWarning(String tag, String msg) {
		if (DEBUGGABLE) {
			Log.w(tag, msg);
		}
	}
	public static void logWarning(String tag, String msg, Exception exc) {
		if (DEBUGGABLE) {
			Log.w(tag, msg, exc);
		}
	}

	public static void logError(String tag, String msg) {
		if (DEBUGGABLE) {
			Log.e(tag, msg);
		}
	}


	public static KeyStore loadTrustStore(final InputStream in) throws Exception {

		KeyStore localTrustStore = KeyStore.getInstance("BKS");

		try {
			localTrustStore.load(in, C.trustStorePass.toCharArray());
		} finally {
			in.close();
		}
		return localTrustStore;
	}

	public static KeyStore createTrustStore(String[] certStrArray) throws Exception {
		KeyStore keyStore = KeyStore.getInstance("BKS");
		keyStore.load(null, null);
		int i = 0;
		for (String cert: certStrArray) {
			keyStore.setCertificateEntry("tls" + ++i, loadCertificate(cert));
		}
		return keyStore;
	}

    public static X509Certificate loadCertificate(byte[] certificateData) throws Exception {
        return loadCertificate(new String(certificateData));
    }

	public static X509Certificate loadCertificate(String certificatePem) throws Exception {
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X509", "SC");
		final byte[] content = readPemContent(certificatePem);
		return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(content));
	}

	private static byte[] readPemContent(String pem) throws IOException {
		PemReader pemReader = new PemReader(new StringReader(pem));
		PemObject pemObject = pemReader.readPemObject();
		return pemObject.getContent();
	}

	public static String readLines(InputStream in, String encoding)
			throws IOException {
		try {
			StringBuilder buff = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, encoding != null ? encoding : "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				buff.append(line).append("\n");
			}
			return buff.toString();
		} finally {
			in.close();
		}
	}

	public static LoadingSpinner startSpinner(Activity currentActivity,
			boolean isWhite) {
		LoadingSpinner mLoadingSpinner = new LoadingSpinner(currentActivity,
				isWhite);
		if (!mLoadingSpinner.isShowing()) {
			mLoadingSpinner.show();
		}

		return mLoadingSpinner;
	}

	public static void stopSpinner(LoadingSpinner mLoadingSpinner) {
		if (mLoadingSpinner != null && mLoadingSpinner.isShowing()) {
			mLoadingSpinner.dismiss();
		}
	}

	public static void startErrorIntent(Activity currentActivity, String error_msg) {
		Intent error_intent = new Intent(currentActivity, ErrorActivity.class);
		error_intent.putExtra(Util.ERROR_MESSAGE, error_msg);
		currentActivity.startActivity(error_intent);
		currentActivity.finish();
		Util.logError("Error intent", currentActivity.getClass().getSimpleName());
	}

	public static void startNetworkErrorIntent(Activity currentActivity, String error_msg) {
		Intent error_intent = new Intent(currentActivity, NetworkErrorActivity.class);
		error_intent.putExtra(Util.ERROR_MESSAGE, error_msg);
		currentActivity.startActivity(error_intent);
		currentActivity.finish();
		Util.logError("Network error intent", currentActivity.getClass().getSimpleName());
	}

	public static void startVersionErrorIntent(Activity currentActivity, String error_msg) {
		Intent error_intent = new Intent(currentActivity, VersionErrorActivity.class);
		error_intent.putExtra(Util.ERROR_MESSAGE, error_msg);
		currentActivity.startActivity(error_intent);
		currentActivity.finish();
		Util.logError("Version error intent", currentActivity.getClass().getSimpleName());
	}

	public static int generateHexColorValue(String color) {
		int hexColor;
		try {
			hexColor = Color.parseColor(color);
		} catch (Exception e) {
			Util.logDebug("Util", "Color wrong format");
			hexColor = Color.parseColor("#FFFFFF");
		}
		return hexColor;
	}

	public static float convertDpToPixels(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return dp * (metrics.densityDpi / 160f);
	}


	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	public static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public static byte[] toBytes(InputStream stream) throws IOException {
		return toBytes(stream, Integer.MAX_VALUE);
	}

	public static byte[] toBytes(InputStream stream, int max) throws IOException{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		int total = 0;
		int nRead;
		byte[] data = new byte[bufferSize];

		while (total + bufferSize < max && (nRead = stream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
			total += nRead;
		}

		return buffer.toByteArray();
	}

	public static String getSubjectCN(X509Certificate cert) throws CertificateException {
		X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
		RDN cn = x500name.getRDNs(BCStyle.CN)[0];
		return IETFUtils.valueToString(cn.getFirst().getValue()).replace("\\,", ", ");
	}

	public static String getIssuerCN(X509Certificate cert) throws CertificateException {
		X500Name x500name = new JcaX509CertificateHolder(cert).getIssuer();
		RDN cn = x500name.getRDNs(BCStyle.CN)[0];
		return IETFUtils.valueToString(cn.getFirst().getValue()).replace("\\,", ", ");
	}

	public static X509Certificate verifyCertIssuerSig(X509Certificate cert, X509Certificate... issuers) throws Exception {
		for (X509Certificate issuer: issuers) {
			try {
				verifyCertIssuerSig(cert, issuer);
				return issuer;
			} catch (Exception ignored) {
					// just try the next one
			}
		}
		throw new Exception();
	}

	public static void verifyCertIssuerSig(X509Certificate cert, X509Certificate issuer) throws Exception {
		cert.verify(issuer.getPublicKey(), "SC");
	}

}
