package ee.vvk.ivotingverification.util;

import android.app.Activity;

import java.security.cert.X509Certificate;

import ee.vvk.ivotingverification.R;

public class EstEidLoader {

	public static X509Certificate[] getEsteidCerts(Activity activity) throws Exception {
		byte[] certData = Util.toBytes(activity.getResources().openRawResource(R.raw.esteid_sk_2015));
		X509Certificate sk2015 = Util.loadCertificate(certData);

		certData = Util.toBytes(activity.getResources().openRawResource(R.raw.esteid2018));
		X509Certificate esteid2018 = Util.loadCertificate(certData);

		certData = Util.toBytes(activity.getResources().openRawResource(R.raw.test_of_esteid_sk_2015));
		X509Certificate test_esteid2015 = Util.loadCertificate(certData);

		certData = Util.toBytes(activity.getResources().openRawResource(R.raw.test_of_esteid2018));
		X509Certificate test_esteid2018 = Util.loadCertificate(certData);
		
		certData = Util.toBytes(activity.getResources().openRawResource(R.raw.test_of_eid2016));
		X509Certificate test_eid2016 = Util.loadCertificate(certData);

		return new X509Certificate[] {sk2015, esteid2018, test_esteid2015, test_esteid2018, test_eid2016};
	}
}
