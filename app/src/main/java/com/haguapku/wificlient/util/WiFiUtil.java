package com.haguapku.wificlient.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.haguapku.wificlient.WifiClientLib;

import java.io.ByteArrayOutputStream;

/**
 * Created by MarkYoung on 15/9/17.
 */
public class WiFiUtil {

    private static char[] encodes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            .toCharArray();
    private static byte[] decodes = new byte[128];
    static {
        for (int i = 0; i < encodes.length; i++) {
            decodes[encodes[i]] = (byte) i;
        }
    }

    private Context mContext;
    private WifiManager mWifiManager;
    Handler handler;

    public static boolean isAppRun = false;

    public final static int WIFI_PASS = 0;
    public final static int WIFI_WEP = 1;
    public final static int WIFI_WPA = 2;
    public final static int WIFI_WPS = 3;

    public WiFiUtil(Context mContext, WifiManager mWifiManager, Handler handler) {
        this.mContext = mContext;
        this.mWifiManager = mWifiManager;
        this.handler = handler;
    }

    public static int signalPercent(int level, int max, int min) {
        if (level <= -100) {
            return min + 0;
        }
        if (level >= -55) {
            return min + (max - 1);
        } else {
            return min + (int) ((max - 1) * (level + 100) / 45.0F);
        }
    }

    public static String encodeB62(byte[] data) {
        StringBuffer sb = new StringBuffer(data.length * 2);
        try {
            int pos = 0, val = 0;
            for (int i = 0; i < data.length; i++) {
                val = (val << 8) | (data[i] & 0xFF);
                pos += 8;
                while (pos > 5) {
                    char c = encodes[val >> (pos -= 6)];
                    sb.append(
					/**/c == 'i' ? "ia" :
					/**/c == '+' ? "ib" :
					/**/c == '/' ? "ic" : c);
                    val &= ((1 << pos) - 1);
                }
            }
            if (pos > 0) {
                char c = encodes[val << (6 - pos)];
                sb.append(
				/**/c == 'i' ? "ia" :
				/**/c == '+' ? "ib" :
				/**/c == '/' ? "ic" : c);
            }
        } catch (Exception e) {
        }
        return sb.toString();
    }

    public static byte[] decodeB62(char[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
        try {
            int pos = 0, val = 0;
            for (int i = 0; i < data.length; i++) {
                char c = data[i];
                if (c == 'i') {
                    c = data[++i];
                    c =
					/**/c == 'a' ? 'i' :
					/**/c == 'b' ? '+' :
					/**/c == 'c' ? '/' : data[--i];
                }
                val = (val << 6) | decodes[c];
                pos += 6;
                while (pos > 7) {
                    baos.write(val >> (pos -= 8));
                    val &= ((1 << pos) - 1);
                }
            }
        } catch (Exception e) {
        }
        return baos.toByteArray();
    }

    public static int wifiLevel(int i) {
        if (i >= -65) {
            return 4;
        } else if (-75 <= i && i < -65) {
            return 3;
        } else if (-85 <= i && i < -75) {
            return 2;
        } else if (-100 <= i && i < -85) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int wifiType(String capabilities){
        if(capabilities.contains("WPA")){
            return WIFI_WPA;
        }else if(capabilities.contains("WEP")){
            return WIFI_WEP;
        }else if(capabilities.contains("WPS")){
            return WIFI_WPS;
        }else {
            return WIFI_PASS;
        }
    }

    public static NetworkInfo getNetworInfo(){
        if(WifiClientLib.context == null){
            return null;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager)WifiClientLib.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            try {
                return connectivityManager.getActiveNetworkInfo();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isWifiAvailable(){
        NetworkInfo networkInfo = getNetworInfo();
        return (networkInfo  != null && networkInfo.isConnected() &&
                networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

}
