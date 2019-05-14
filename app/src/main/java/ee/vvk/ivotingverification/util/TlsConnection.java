package ee.vvk.ivotingverification.util;

import android.net.SSLCertificateSocketFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

    public TlsConnection(String[] tlsCerts) throws Exception {
        sslCertificateSocketFactory = (SSLCertificateSocketFactory)
                SSLCertificateSocketFactory.getDefault(0);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(Util.createTrustStore(tlsCerts));
        sslCertificateSocketFactory.setTrustManagers(tmf.getTrustManagers());
    }

    /**
     * @throws IOException if sending the request failed
     * @throws UnsupportedOperationException if the device does not support SNI
     */
    public InputStream sendRequest(String[] urlArray, String hostName, ByteBuffer buf)
            throws IOException, UnsupportedOperationException {
        SSLSocket socket;
        try {
            socket = createConnection(urlArray, C.connectionTimeout1);
        } catch (IOException e) {
            if (Util.DEBUGGABLE) {
                Log.w(TAG, "First round did not connect, retrying", e);
            }
            socket = createConnection(urlArray, C.connectionTimeout2);
        }

        socket.setSoTimeout(15000);
        socket.setEnabledProtocols(new String[]{"TLSv1.2"});

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            sslCertificateSocketFactory.setHostname(socket, hostName);
        } else {
            // No documented SNI support on Android <4.2, trying with reflection
            try {
                Method setHostnameMethod = socket.getClass().getMethod("setHostname", String.class);
                setHostnameMethod.invoke(socket, hostName);
            } catch (Exception e) {
                throw new UnsupportedOperationException("setHostname", e);
            }
        }
        //socket.startHandshake();
        WritableByteChannel out = Channels.newChannel(socket.getOutputStream());
        out.write(buf);
        return socket.getInputStream();
    }

    private SSLSocket createConnection(String[] urlArray, int timeout) throws IOException {
        List<String> domains = Arrays.asList(urlArray);
        Collections.shuffle(domains);
        for (String domain : domains) {
            String[] urlParts = domain.split(":");
            int port = Integer.parseInt(urlParts[1]);
            List<InetAddress> ips;
            try {
                ips = Arrays.asList(InetAddress.getAllByName(urlParts[0]));
            } catch (UnknownHostException e) {
                if (Util.DEBUGGABLE) {
                    Log.w(TAG, "Unknown host " + domain, e);
                }
                continue;
            }
            Collections.shuffle(ips);
            for (InetAddress ip : ips) {
                try {
                    SSLSocket socket = (SSLSocket) sslCertificateSocketFactory.createSocket();
                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    return socket;
                } catch (SocketTimeoutException e) {
                    if (Util.DEBUGGABLE) {
                        Log.w(TAG, String.format("Socket timed out: %s:%s", ip, port), e);
                    }
                }
            }
        }
        throw new ConnectException("Could not connect to " + TextUtils.join(", ", urlArray));
    }
}
