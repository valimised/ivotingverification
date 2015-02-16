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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class HttpRequest {
	private static String TAG = "HttpRequest";

	private Context context;
	private SSLContext sslcontext;

	public HttpRequest(Context cx) {
		this.context = cx;
		this.sslcontext = null;

		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(Util.loadTrustStore((Activity) context));
			sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, tmf.getTrustManagers(), null);
		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		}
	}

	public HttpResponse post(String url,
			List<NameValuePair> headerNameValuePairs,
			List<NameValuePair> entryNameValuePairs) {

		try {

			UrlEncodedFormEntity ent = new UrlEncodedFormEntity(
					entryNameValuePairs);
			URL request = new URL(url);
			HttpsURLConnection urlConnection = (HttpsURLConnection) request
					.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);

			urlConnection.setHostnameVerifier(new StrictHostnameVerifier());
			urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());
			urlConnection.setConnectTimeout(15000);

			OutputStream os = urlConnection.getOutputStream();
			ent.writeTo(os);

			InputStream in = urlConnection.getInputStream();
			BasicHttpEntity res = new BasicHttpEntity();
			res.setContent(in);
			HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
					urlConnection.getResponseCode(), "");
			response.setEntity(res);

			if (Util.DEBUGGABLE) {
				printResponseHeader(response);
			}
			return response;

		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "POST Error (" + e.getMessage() + ") URL: " + url);
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		}
		return null;
	}

	public HttpResponse get(String url, List<NameValuePair> headerNameValuePairs) {

		try {
			URL request = new URL(url);
			HttpsURLConnection urlConnection = (HttpsURLConnection) request
					.openConnection();
			System.setProperty("http.keepAlive", "false");
			urlConnection.setHostnameVerifier(new StrictHostnameVerifier());
			urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());
			urlConnection.setConnectTimeout(15000);

			InputStream in = urlConnection.getInputStream();

			BasicHttpEntity res = new BasicHttpEntity();
			res.setContent(in);
			HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
					urlConnection.getResponseCode(), "");
			response.setEntity(res);

			if (Util.DEBUGGABLE) {
				printResponseHeader(response);
			}
			return response;

		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "GET Error (" + e.getMessage() + ") URL: " + url);
			}
			Log.d(TAG, e.toString());
			Log.d(TAG, e.getMessage());
			Log.d(TAG, e.getCause().toString());

			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		}
		return null;
	}

	private void printResponseHeader(HttpResponse response) {
		System.out.println("Responce header:");
		org.apache.http.Header[] h = response.getAllHeaders();
		for (int i = 0; i < h.length; i++) {
			System.out.println(h[i].getName() + ": " + h[i].getValue());
		}
	}

}
