package com.haguapku.wificlient.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.haguapku.wificlient.R;
import com.haguapku.wificlient.WifiClientLib;
import com.haguapku.wificlient.bean.AccessPoint;
import com.haguapku.wificlient.producer.FilterItem;
import com.haguapku.wificlient.producer.ItemBase;
import com.haguapku.wificlient.producer.ProducerUtils;
import com.haguapku.wificlient.producer.WifiItem;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static boolean isReport = false;

    public final static int WIFI_PASS = 0;
    public final static int WIFI_WEP = 1;
    public final static int WIFI_WPA = 2;
    public final static int WIFI_WPS = 3;

    public WiFiUtil(Context mContext, WifiManager mWifiManager, Handler handler) {
        this.mContext = mContext;
        this.mWifiManager = mWifiManager;
        this.handler = handler;
    }

    Comparator<AccessPoint> comparatorLevel = new Comparator<AccessPoint>() {
        @Override
        public int compare(AccessPoint lhs, AccessPoint rhs) {

            if (lhs.getLevel() == rhs.getLevel()) {
                return 0;
            }
            if (lhs.getLevel() > rhs.getLevel()) {
                return -1;
            } else {
                return 1;
            }
        }
    };

    public void sfWifiConnect(String BSSID, String SSID, String PWD, int type){

        try {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            int netId = wifiInfo.getNetworkId();
            if(netId != -1)
            {
                mWifiManager.disableNetwork(netId);
            }
            mWifiManager.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int wcgID = -1;
        WifiConfiguration wifiConfig = CreateWifiInfo(SSID,BSSID,PWD,type);
        WifiConfiguration tempConfig = isExsits(SSID,BSSID,mWifiManager.getConfiguredNetworks());
        if(tempConfig != null){
            try {
                wcgID = mWifiManager.updateNetwork(tempConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(wcgID == -1){
                wcgID = tempConfig.networkId;
            }
        }else {
            wcgID = mWifiManager.addNetwork(wifiConfig);
        }
        boolean flag = mWifiManager.enableNetwork(wcgID, true);

        if(!flag){
            handler.removeMessages(Constants.MSG_ACTION_CONNECT_TIMEOUT);
            Message msg = new Message();
            msg.what = 90;
            handler.sendMessage(msg);
            Toast.makeText(mContext, R.string.wifi_connect_fail,
                    Toast.LENGTH_SHORT).show();
            mWifiManager.removeNetwork(wcgID);
        }
    }

    public WifiConfiguration CreateWifiInfo(String SSID, String BSSID,String Password,
                                            int Type) {

        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (!TextUtils.isEmpty(BSSID)) {
            config.BSSID = BSSID;
        }
        WifiConfiguration tempConfig = isExsits(SSID,BSSID,mWifiManager.getConfiguredNetworks());
        if(tempConfig != null){
            if(TextUtils.isEmpty(Password) && Type != WIFI_PASS){
                return tempConfig;
            }else {
                mWifiManager.removeNetwork(tempConfig.networkId);
            }
        }

        if (Type == WIFI_PASS) // WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "\"" + "" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WIFI_WEP) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            int length=Password.length();
            if(length==5||length==13||length==16){
                config.wepKeys[0]= "\""+Password+"\"";
            }else{
                config.wepKeys[0]= Password;
            }
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;

        }
        if (Type == WIFI_WPA) // WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
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

    public static NetworkInfo getNetworkInfo(){
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

    private static long block = 0;
    private static long time = 0;
    private static float lastsp = 0.f;

    private static long beginTime = 0;
    private static long beginTotalbytes = 0;

    public static long getTotalRxBytes() { // receive：Mobile&WiFi
        long rx = TrafficStats.getTotalRxBytes();
        return rx == TrafficStats.UNSUPPORTED ? 0 : rx;
    }

    public static long getTotalTxBytes() { // send：Mobile&WiFi
        long tx = TrafficStats.getTotalTxBytes();
        return tx == TrafficStats.UNSUPPORTED ? 0 : tx;
    }

    public static boolean isWifiAvailable(){
        NetworkInfo networkInfo = getNetworkInfo();
        return (networkInfo  != null && networkInfo.isConnected() &&
                networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static String normalizeBSSID(String bssid) {
        if (bssid != null) {
            return bssid.replaceAll("\"", "");
        }
        return null;
    }


    @SuppressLint("DefaultLocale")
    public static String getSpeed() {
        float sp = getSpeedF();
        String lastbps = null;
        if (sp > 1000) {
            lastbps = String.format("%1$.1fM/s", sp / 1024);
        } else if (sp < 1024 * 1024) {
            lastbps = String.format("%1$.1fK/s", sp);
        }
        return lastbps;
    }

    public static float getSpeedF() {
        long totalbytes = getTotalRxBytes() + getTotalTxBytes();
        long currenttime = System.currentTimeMillis();
        long temp = totalbytes - block;
        long temp2 = currenttime - time;
        if (time == 0) {
            time = currenttime;
            block = totalbytes;
        } else if (temp2 > 100) {
            // we use 1000 as 1024
            block = totalbytes;
            time = currenttime;
            lastsp = temp / temp2;
        }
        return lastsp;
    }

    private boolean filterAP(ScanResult sr) {
        return sr.SSID.equals("<unknown ssid>")||sr.BSSID.equals("<none>");
    }

    public WifiConfiguration isExsits(String SSID,String BSSID,
                                      List<WifiConfiguration> existingConfigs) {
        if (existingConfigs == null) {
            existingConfigs = WifiAdmin.getInstance().getConfiguration();
        }
        if (existingConfigs == null) {
            return null;
        }
        WifiConfiguration wc = null;
        for (WifiConfiguration existingConfig : existingConfigs) {
            String ssid = existingConfig.SSID;
            String bssid = existingConfig.BSSID;
            if (!TextUtils.isEmpty(bssid)&&(!"00:00:00:00:00:00".equals(bssid))) {
                if (BSSID.equals(bssid)&&("\"" + SSID + "\"").equals(ssid)) {
                    //DmLog.d("wf", "existingConfig="+existingConfig.SSID+"  --"+existingConfig.BSSID);
                    wc = existingConfig;
                }
            }
            if (!TextUtils.isEmpty(ssid)) {
                if (ssid.equals("\"" + SSID + "\"")) {
                    wc = existingConfig;
                }
            }
        }
        return wc;
    }



    public Map<String, List<AccessPoint>> sortApInfo(List<ScanResult> datas) {
        Map<String, List<AccessPoint>> map = new HashMap<String, List<AccessPoint>>();
        if(mWifiManager==null||datas == null) {
            return map;
        }
        WifiInfo wi = null;
        try {
            wi = mWifiManager.getConnectionInfo();
        } catch (Exception e) {
        }
        @SuppressWarnings("unused")
        String cssid = "";
        boolean bool = true;
        HashMap<String, ScanResult> hms = new HashMap<String, ScanResult>();
        List<ScanResult> datass=new ArrayList<ScanResult>();
        for (ScanResult sr : datas) {
            if (sr == null) {
                continue;   //Stop single circle
            }
            hms.put(sr.SSID, sr);
        }
        for (ScanResult sr : hms.values()) {
            datass.add(sr);
        }
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        List<AccessPoint> frees = new ArrayList<AccessPoint>();
        List<AccessPoint> keys = new ArrayList<AccessPoint>();
        List<AccessPoint> current = new ArrayList<AccessPoint>();
        for (final ScanResult sr : datass) {
            if(sr==null){
                continue;
            }
            if (TextUtils.isEmpty(sr.SSID)) {
                continue;
            }
            //DmLog.w("wf", "sr.SSID="+sr.SSID+"   sr.capabilities="+sr.capabilities);
            int filter = ProducerUtils.filter(sr.SSID);
            if (filterAP(sr) || (filter == FilterItem.TYPE_HIDE)) {
                continue;
            }

            SSIDTranslator.SsidInfo ssidInfo = SSIDTranslator.generateSsidInfo(sr.SSID);
            //SsidInfo ssidInfo = SSIDTranslator.isSsidPrefixMatched(sr.SSID);
            if (ssidInfo != null) {
                // Is zapya windows production
                //DmLog.e("wf", "快牙热点：" + sr.SSID + " --- "+ssidInfo.osType + " --- " + sr.capabilities);
                if (ssidInfo.osType != SSIDTranslator.WINDOWS) {
                    continue;
                }
            }
            if (wifiType(sr.capabilities)==WIFI_WPS) {
                continue;
            }

            final AccessPoint ap = new AccessPoint();
            ap.setBssid(sr.BSSID);
            ap.setSsid(sr.SSID);
            ap.setCapabilities(sr.capabilities);
            //DmLog.w("wf", "sr.SSID="+sr.SSID+"   sr.capabilities="+sr.capabilities);
            ap.setType(wifiType(sr.capabilities));
            ap.setFrequency(sr.frequency);
            ap.setDescribeContents(sr.describeContents());
            ap.setLevel(sr.level);
            ap.setIcon(R.drawable.wifi_default_logo);
            ap.setLevelPercent(WiFiUtil.signalPercent(sr.level, 100, 1));
            ap.setSignalIcon(WiFiUtil.wifiLevel(sr.level));
            ap.setNetworkId(-1);
            if (ssidInfo != null) {
                ap.setZapyaHot(true);
                ap.setIcon(R.drawable.wifi_zapya_pc);
                ap.setLuyouType(mContext.getString(R.string.zapya_pc_ap));
            } else {
                ItemBase item = ProducerUtils.find(sr.BSSID, sr.SSID);
                if (item != null) {
                    if (item instanceof WifiItem) {
                        WifiItem wifiItem = (WifiItem) item;
                        ap.setLuyouType(wifiItem.name);
                        String id = wifiItem.id;
                        if (!TextUtils.isEmpty(id)) {
                            ap.setIcon(ProducerUtils.getWiFiIcon(Integer
                                    .parseInt(id)));
                        } else {
                            ap.setIcon(R.drawable.wifi_default_logo);
                        }
                        ap.setNeedLogin(wifiItem.isNeedLogin);
                        ap.setCheck(wifiItem.check);
                    }
                }
            }

            if (bool && wi != null && wi.getSSID() != null&& sr.SSID.equals(wi.getSSID().replaceAll("\"", ""))) {
                cssid = wi.getSSID().replaceAll("\"", "");
                current.add(ap);
                bool = false;
                continue;
            }

            //DmLog.e("wf", sr.SSID+"  "+sr.BSSID+"    "+wifiType(sr.capabilities));
            if(filter == FilterItem.TYPE_MOVE_TO_KEY){
                ap.setLock(true);
                keys.add(ap);
                continue;
            }

//            if (WiFiDbUtils.isCracked(sr.BSSID, wifiType(sr.capabilities))&&wifiType(sr.capabilities)!=WIFI_PASS) {
//                ap.setLock(false);
//                ap.setCrack(true);
//                //ap.setType(WIFI_CRACK);
//                ap.setDesc(mContext.getResources().getString(R.string.wifi_desc_crack));
//                // DmLog.e("wf", "WIFI_CRACK="+sr.BSSID + sr.SSID);
//                frees.add(ap);
//                continue;
//            }

            WifiConfiguration exsits = isExsits(sr.SSID, sr.BSSID,existingConfigs);
            if (exsits != null || wifiType(sr.capabilities) == WIFI_PASS) {
                ap.setLock(false);
                if (ap.isNeedLogin()) {
                    ap.setDesc(mContext.getResources().getString(R.string.wifi_desc_needlogin));
                }
                if (exsits != null) {
                    ap.setDesc(mContext.getResources().getString(R.string.wifi_desc_saveed));
                    ap.setNetworkId(exsits.networkId);
                }
                if (wifiType(sr.capabilities) == WIFI_PASS) {
                    ap.setDesc(mContext.getResources().getString(R.string.wifi_desc_nopwd));
                }
                frees.add(ap);
            } else {
                ap.setLock(true);
                keys.add(ap);
            }
        }

        Collections.sort(keys, comparatorLevel);
        Collections.sort(frees, comparatorLevel);
        if (frees.isEmpty()) {
            AccessPoint ap = new AccessPoint();
            ap.setBssid("");
            ap.setSsid(mContext.getResources().getString(R.string.wifi_free_empty));
            ap.setCapabilities("");
            ap.setType(0);
            ap.setFrequency(0);
            ap.setDescribeContents(0);
            ap.setLevel(-55);
            ap.setIcon(R.drawable.wifi_default_logo);
            ap.setLevelPercent(WiFiUtil.signalPercent(-55, 100, 1));
            ap.setSignalIcon(2);
            ap.setLuyouType("");
            ap.setIcon(R.drawable.free_icon);
            ap.setLock(false);
            frees.add(ap);
        }
        map.put("current", current);
        map.put("key", keys);
        map.put("free", frees);
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        return map;
    }

}
