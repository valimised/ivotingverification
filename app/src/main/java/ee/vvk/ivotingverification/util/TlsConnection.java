package ee.vvk.ivotingverification.util;

import android.app.Activity;
import android.net.SSLCertificateSocketFactory;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class TlsConnection {
    private final static String TAG = "TLS";

    private final SSLCertificateSocketFactory sslCertificateSocketFactory;
    private final Activity context;

    public TlsConnection(Activity cx, String[] tlsCerts) {
        this.context = cx;
        sslCertificateSocketFactory =
                (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0);
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(Util.createTrustStore(tlsCerts));
            sslCertificateSocketFactory.setTrustManagers(tmf.getTrustManagers());
        } catch (Exception e) {
            if (Util.DEBUGGABLE) {
                Log.e(TAG, "Tehniline viga: " + e.getMessage(), e);
            }
            Util.startErrorIntent(context, C.badServerResponseMessage, true);
        }
    }

    public Socket sendRequest(String[] urlArray, String hostName, ByteBuffer buf) {
        try {
            SSLSocket socket = createConnection(urlArray, C.connectionTimeout1);
            socket = socket == null ? createConnection(urlArray, C.connectionTimeout2) : socket;

            if (socket == null) {
                return null;
            }
            socket.setSoTimeout(15000);
            socket.setEnabledProtocols(new String[]{"TLSv1.2"});

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                sslCertificateSocketFactory.setHostname(socket, hostName);
            } else {
                // "No documented SNI support on Android <4.2, trying with reflection
                try {
                    Method setHostnameMethod = socket.getClass().getMethod("setHostname", String.class);
                    setHostnameMethod.invoke(socket, hostName);
                } catch (Exception e) {
                    if (Util.DEBUGGABLE) {
                        Log.e(TAG, "SNI Error", e);
                    }
                    Util.startErrorIntent(context,
                            C.badDeviceMessage, true);
                    return null;
                }
            }
            //socket.startHandshake();
            WritableByteChannel out = Channels.newChannel(socket.getOutputStream());
            out.write(buf);
            return socket;
        } catch (Exception e) {
            if (Util.DEBUGGABLE) {
                e.printStackTrace();
                Log.e(TAG, "Network Connection Error", e);
            }
            Util.startErrorIntent(context,
                    C.badServerResponseMessage, true);
        }
        return null;
    }

    private SSLSocket createConnection(String[] urlArray, int timeout) throws Exception {
        List<String> domains = Arrays.asList(urlArray);
        SSLSocket socket = (SSLSocket) sslCertificateSocketFactory.createSocket();
        Collections.shuffle(domains);
        for (String domain : domains) {
            String[] urlParts = domain.split(":");
            int port = Integer.parseInt(urlParts[1]);
            List<InetAddress> ips;
            try {
                ips = Arrays.asList(InetAddress.getAllByName(urlParts[0]));
            } catch (UnknownHostException e) {
                if (Util.DEBUGGABLE) {
                    Log.w(TAG, "Unknown host" + domain, e);
                }
                continue;
            }
            Collections.shuffle(ips);
            for (InetAddress ip : ips) {
                try {
                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    return socket;
                } catch (SocketTimeoutException e) {
                    if (Util.DEBUGGABLE) {
                        Log.w(TAG, String.format("Socket timed out: %s:%s", ip, port), e);
                    }
                }
            }
        }
        return null;
    }
}
