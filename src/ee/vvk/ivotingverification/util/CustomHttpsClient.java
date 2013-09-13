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

import java.security.KeyStore;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class CustomHttpsClient extends DefaultHttpClient {

	private static final String TAG = CustomHttpsClient.class.getSimpleName();
	final Context context;

	public CustomHttpsClient(Context context) {
		this.context = context;
	}

	@Override
	protected ClientConnectionManager createClientConnectionManager() {

		SchemeRegistry registry = new SchemeRegistry();

		registry.register(new Scheme("https", newSslSocketFactory(), 443));

		return new SingleClientConnManager(getParams(), registry);
	}

	private SSLSocketFactory newSslSocketFactory() {
		KeyStore trustStore;
		try {

			trustStore = Util.loadTrustStore((Activity) context);
			
			SSLSocketFactory sf = new SSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

			return sf;
		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		}

		return null;
	}
}