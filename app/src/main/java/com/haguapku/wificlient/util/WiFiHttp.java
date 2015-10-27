package com.haguapku.wificlient.util;

import com.haguapku.wificlient.WifiClientLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by MarkYoung on 15/10/23.
 */
public class WiFiHttp {

    public final static int NETWORK_UNCHECK = -1;
    public final static int NETWORK_CONNECTED = 1;
    public final static int NETWORK_NEEDLOGIN = 2;
    public final static int NETWORK_UNUSED = 3;
    public final static int NETWORK_TIMEOUT = 4;

    private final static boolean DEBUG = WifiClientLib.DEBUG;

    public static int checkNetWork(String strURL) {
        int resultCode = NETWORK_UNCHECK;
        // No wifi network, don't run the task.
        if (!WiFiUtil.isWifiAvailable()) {
            return resultCode;
        }
        // Check from apple
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            URL url = new URL(strURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(1000);
            connection.setDefaultUseCaches(false);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            is = connection.getInputStream();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String sCurrentLine = "";
                String sTotalString = "";
                while ((sCurrentLine = bufferedReader.readLine()) != null) {
                    sTotalString += sCurrentLine;
                    if (sTotalString.length() > 256) {
                        break;
                    }
                }
                bufferedReader.close();

                if (sTotalString.toLowerCase().contains(
                        Constants.SUCCESS_RESULT.toLowerCase())
                        || sTotalString.contains("360WiFi")) {
                    resultCode = NETWORK_CONNECTED;
                } else {
                    resultCode = NETWORK_NEEDLOGIN;
                }
            } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP|| responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                resultCode = NETWORK_NEEDLOGIN;
            } else {
                resultCode = NETWORK_UNUSED;
            }
        } catch (Exception e) {
            if (e instanceof SocketTimeoutException) {
                resultCode = NETWORK_TIMEOUT;
            } else {
                resultCode = NETWORK_UNUSED;
            }

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return resultCode;
    }

    public static boolean isResultAccurate(int result) {
        return (result != NETWORK_UNCHECK && result != NETWORK_TIMEOUT);
    }

    public static boolean ping(String url) {
        boolean result = false;
        Process process = null;
        try {
            String str1 = "/system/bin/ping -c 1 -w " + 2 + "  "+ new URL(url).getHost();
            process = Runtime.getRuntime().exec(str1);
            process.waitFor();
            BufferedReader localBufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String str2;
            do {
                str2 = localBufferedReader.readLine();
                if (str2 == null)
                    break;

                if (str2.contains("bytes from")) {
                    result = true;
                    break;
                }
            } while (true);
        } catch (Exception e) {

        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    public static boolean testDNS(String hostname) {
        try {
            DNSResolver dnsRes = new DNSResolver(hostname);
            Thread t = new Thread(dnsRes);
            t.start();
            t.join(8000);
            InetAddress inetAddr = dnsRes.get();

            return inetAddr != null;
        } catch (Exception e) {

            return false;
        }
    }

    private static class DNSResolver implements Runnable {
        private String domain;
        private InetAddress inetAddr;

        public DNSResolver(String domain) {
            this.domain = domain;
        }

        public void run() {
            try {
                InetAddress addr = InetAddress.getByName(domain);
                set(addr);
            } catch (UnknownHostException e) {
            }
        }

        public synchronized void set(InetAddress inetAddr) {
            this.inetAddr = inetAddr;
        }

        public synchronized InetAddress get() {
            return inetAddr;
        }
    }


}
