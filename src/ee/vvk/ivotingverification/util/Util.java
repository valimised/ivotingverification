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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import ee.vvk.ivotingverification.ErrorActivity;
import ee.vvk.ivotingverification.R;
import ee.vvk.ivotingverification.dialog.LoadingSpinner;

/**
 * Utilities.
 * 
 * @version 16.05.2013
 */
public class Util {

	public final static String QR_CODE = "ee.vvk.ivotingverification.QR_CODE";
	public final static String WEB_RESULT = "ee.vvk.ivotingverification.WEB_RESULT";
	public final static String VERSION_NUMBER = "ee.vvk.ivotingverification.VERSION_NUMBER";
	public final static String ERROR_MESSAGE = "ee.vvk.ivotingverification.ERROR_MESSAGE";
	public final static String NETWORK_STATUS = "ee.vvk.ivotingverification.NETWORK_STATUS";
	public final static String EXTRA_MESSAGE = "ee.vvk.ivotingverification.MESSAGE";
	public final static String EXIT = "ee.vvk.ivotingverification.EXIT";
	public final static String RESULT = "ee.vvk.ivotingverification.RESULT";
	public final static String POST_REQUEST_METHOD = "POST";
	public final static String GET_REQUEST_METHOD = "GET";
	public final static String TLS_PROTOCOL = "TLS";
	public final static String ENCODING = "UTF-8";
	public final static String VOTE_PARAMETR = "vote";

	public final static int TIMEOUT = 10 * 1000;
	public final static long VIBRATE_DURATION = 350L;

	public static boolean DEBUGGABLE = false;

	public static KeyStore loadTrustStore(final Activity currentActivity) {

		try {
			KeyStore localTrustStore = KeyStore.getInstance("BKS");
			InputStream in = currentActivity.getResources().openRawResource(
					R.raw.mytruststore);

			try {
				localTrustStore.load(in, C.trustStorePass.toCharArray());

			} catch (NoSuchAlgorithmException e) {
				Util.startErrorIntent((Activity) currentActivity,
						C.badServerResponseMessage, true);
			} catch (IOException e) {
				Util.startErrorIntent((Activity) currentActivity,
						C.badServerResponseMessage, true);
			} catch (CertificateException e) {
				Util.startErrorIntent((Activity) currentActivity,
						C.badServerResponseMessage, true);
			} finally {
				in.close();
			}

			return localTrustStore;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String readLines(InputStream in, String encoding)
			throws IOException {
		try {
			StringBuffer buff = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, encoding != null ? encoding : "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				buff.append(line + "\n");
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

	public static void startErrorIntent(Activity currentActivity,
			String error_msg, boolean networkStatus) {
		Intent error_intent = new Intent(currentActivity, ErrorActivity.class);
		error_intent.putExtra(Util.ERROR_MESSAGE, error_msg);
		error_intent.putExtra(Util.NETWORK_STATUS, networkStatus);
		currentActivity.startActivity(error_intent);
		currentActivity.finish();

		if (Util.DEBUGGABLE) {
			Log.e("Error intent", currentActivity.getClass().getSimpleName());
		}
	}

	public static int generateHexColorValue(String color) {
		int hexColor;
		try {
			hexColor = Color.parseColor(color);
		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.d("Util", "Color wrong format");
			}
			hexColor = Color.parseColor("#FFFFFF");
		}
		return hexColor;
	}


	public static float convertPixelsToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px * (metrics.densityDpi / 160f);
		return dp;
	}

	public static String readRawTextFile(Context context, int fileName) {
		InputStream inputStream = context.getResources().openRawResource(
				fileName);

		InputStreamReader inputReader = new InputStreamReader(inputStream);
		BufferedReader buffReader = new BufferedReader(inputReader);
		String line;
		StringBuilder text = new StringBuilder();

		try {
			while ((line = buffReader.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}
}