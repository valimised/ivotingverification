package ee.vvk.ivotingverification.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class HttpRequest {
	private static String TAG = "HttpRequest";

	private Context context;
	private SSLSocketFactory sslfactory;

	public HttpRequest(Context cx) {
		this.context = cx;
		this.sslfactory = null;

		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(Util.loadTrustStore((Activity) context));
			sslfactory = new TLSv12SocketFactory(null, tmf.getTrustManagers(), null);
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
			urlConnection.setSSLSocketFactory(sslfactory);
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
				printResponseHeader(urlConnection);
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
			urlConnection.setSSLSocketFactory(sslfactory);
			urlConnection.setConnectTimeout(15000);

			InputStream in = urlConnection.getInputStream();

			BasicHttpEntity res = new BasicHttpEntity();
			res.setContent(in);
			HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
					urlConnection.getResponseCode(), "");
			response.setEntity(res);

			if (Util.DEBUGGABLE) {
				printResponseHeader(urlConnection);

			}
			return response;

		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "GET Error (" + e.getMessage() + ") URL: " + url);
			}

			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		}
		return null;
	}

	private void printResponseHeader(URLConnection urlConnection) {
		Log.d(TAG, "Response header:");
		for (Map.Entry<String, List<String>> entry : urlConnection.getHeaderFields().entrySet()) {
			Log.d(TAG, entry.getKey() + ": " + entry.getValue());
		}
	}

	/**
	 * TLSv12SocketFactory wraps a "TLSv1.2" SSLContext's SSLSocketFactory to only enable TLS v1.2.
	 */
	private class TLSv12SocketFactory extends SSLSocketFactory {
		private SSLSocketFactory factory;

		public TLSv12SocketFactory(KeyManager[] km, TrustManager[] tm, SecureRandom random)
				throws NoSuchAlgorithmException, KeyManagementException {
			SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
			sslcontext.init(km, tm, random);
			factory = sslcontext.getSocketFactory();
		}

		@Override
		public String[] getDefaultCipherSuites() {
			return factory.getDefaultCipherSuites();
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return factory.getSupportedCipherSuites();
		}

		@Override
		public Socket createSocket(Socket s, String host, int port, boolean autoClose)
				throws IOException {
			return tlsv12(factory.createSocket(s, host, port, autoClose));
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			return tlsv12(factory.createSocket(host, port));
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
				throws IOException, UnknownHostException {
			return tlsv12(factory.createSocket(host, port, localHost, localPort));
		}

		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			return tlsv12(factory.createSocket(host, port));
		}

		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
				throws IOException {
			return tlsv12(factory.createSocket(address, port, localAddress, localPort));
		}

		private Socket tlsv12(Socket socket) {
			if (socket != null && socket instanceof SSLSocket) {
				((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.2"});
			}
			return socket;
		}
	}
}
