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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class HttpRequest {
	private static String TAG = "HttpRequest";

	private HttpClient mHttpsClient;
	private Context context;

	public HttpRequest(Context cx) {
		this.context = cx;
		mHttpsClient = new CustomHttpsClient(cx);
	}

	public HttpResponse post(String url,
			List<NameValuePair> headerNameValuePairs,
			List<NameValuePair> entryNameValuePairs) {

		HttpPost post = new HttpPost(url);
		try {
			if (headerNameValuePairs != null && headerNameValuePairs.size() > 0) {
				for (NameValuePair nameValuePair : headerNameValuePairs) {
					post.addHeader(nameValuePair.getName(),
							nameValuePair.getValue());
				}
			}

			post.setEntity(new UrlEncodedFormEntity(entryNameValuePairs));

			if (Util.DEBUGGABLE) {
				printRequestHeaderPOST(post);
			}

			HttpResponse response = mHttpsClient.execute(post);

			if (Util.DEBUGGABLE) {
				printResponseHeader(response);
			}
			return response;

		} catch (ClientProtocolException e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "POST Error (" + e.getMessage() + ") URL: " + url);
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		} catch (SocketTimeoutException e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "POST Error (" + e.getMessage() + ") URL: " + url);
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		} catch (IOException e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "POST Error (" + e.getMessage() + ") URL: " + url);
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		}
		return null;
	}

	public HttpResponse get(String url, List<NameValuePair> headerNameValuePairs) {

		HttpGet get = new HttpGet(url);

		if (headerNameValuePairs != null && headerNameValuePairs.size() > 0) {
			for (NameValuePair nameValuePair : headerNameValuePairs) {
				get.addHeader(nameValuePair.getName(), nameValuePair.getValue());
			}
		}

		if (Util.DEBUGGABLE) {
			printRequestHeaderGET(get);
		}
		try {
			HttpResponse response = mHttpsClient.execute(get);
			if (Util.DEBUGGABLE) {
				printResponseHeader(response);
			}
			return response;

		} catch (ClientProtocolException e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "GET Error (" + e.getMessage() + ") URL: " + url);
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		} catch (IOException e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "GET Error (" + e.getMessage() + ") URL: " + url);
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		}
		return null;
	}

	// DEBUG:
	private void printRequestHeaderGET(HttpGet get) {
		System.out.println("Request header. GET: " + get.getURI());
		org.apache.http.Header[] h = get.getAllHeaders();
		for (int i = 0; i < h.length; i++) {
			System.out.println(h[i].getName() + ": " + h[i].getValue());
		}
	}

	private void printRequestHeaderPOST(HttpPost post) {
		System.out.println("Request header. POST:" + post.getURI());
		org.apache.http.Header[] h = post.getAllHeaders();
		for (int i = 0; i < h.length; i++) {
			System.out.println(h[i].getName() + ": " + h[i].getValue());
		}
	}

	private void printResponseHeader(HttpResponse response) {
		System.out.println("Responce header:");
		org.apache.http.Header[] h = response.getAllHeaders();
		for (int i = 0; i < h.length; i++) {
			System.out.println(h[i].getName() + ": " + h[i].getValue());
		}
	}
}
