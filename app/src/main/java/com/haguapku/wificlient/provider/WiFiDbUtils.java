package com.haguapku.wificlient.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.haguapku.wificlient.bean.LocalPWD;
import com.haguapku.wificlient.bean.PWD;
import com.haguapku.wificlient.util.AESUtil;
import com.haguapku.wificlient.util.WiFiUtil;
import com.haguapku.wificlient.util.WifiAdmin;

import java.security.Security;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by MarkYoung on 15/10/30.
 */
public class WiFiDbUtils {

    public static final String AUTHORITY = "com.haguapku.wificlient.provider";

    public static final Uri AP_LIST_CONTENTURI = Uri.parse("content://" + AUTHORITY + "/aplist");
    public static final Uri AP_SPEED_CONTENTURI = Uri.parse("content://" + AUTHORITY + "/apspeed");
    public static final Uri AP_PWD_CONTENTURI = Uri.parse("content://" + AUTHORITY + "/appwd");
    public static final Uri AP_PWD_LOCAL_CONTENTURI = Uri.parse("content://" + AUTHORITY + "/appwdlocal");

    // tables
    public static final String TABLE_AP_LIST = "ap_list";
    public static final String TABLE_AP_SPEEDREC = "ap_speedrec";
    public static final String TABLE_AP_PWD = "ap_crack_pwd";
    public static final String TABLE_AP_LOCAL_PWD = "ap_local_pwd";

    // columns of tables ap_list
    public static final String AP_ID = "_id";
    public static final String AP_SSID = "ssid";
    public static final String AP_BSSID = "bssid";
    public static final String AP_LATITUDE = "latitude";
    public static final String AP_LONGITUDE = "longitude";
    public static final String AP_RSSI = "rssi";
    public static final String AP_SECURITY = "security";
    public static final String AP_CAPABILITIES = "capabilities";
    public static final String AP_LASTLINKTIME = "lastlinktime";
    public static final String AP_UPDATETIME = "updatetime";

    // columns of tables ap_speed
    public static final String AP_SPEED = "speed";
    public static final String AP_PORTAL = "portal";
    public static final String AP_TT = "testtime";

    // columns of tables ap_crack_pwd
    public static final String AP_PWD = "pwd";
    public static final String AP_CSUCCESS_NUM = "successNum";
    public static final String AP_CFAIL_NUM = "failNum";
    public static final String AP_ISAVAILABLE = "isavailable";

    private static Context context = null;
    private static ExecutorService executorService = null;
    private static ContentResolver cr = null;

    private static HashMap<String, PWD> cachePwds = new HashMap<String, PWD>();
    private static HashMap<String, LocalPWD> cacheLocalPwds = new HashMap<String, LocalPWD>();

    private static final String[] PWD_PROJECT = new String[] { AP_BSSID,
            AP_SSID, AP_PWD, AP_CSUCCESS_NUM, AP_CFAIL_NUM, AP_SPEED };
    private static final String[] PWD_LOCAL_PROJECT = new String[] { AP_BSSID,
            AP_SSID, AP_PWD };
    private static final String WHERE_BSSID = AP_BSSID + "=?";

    public static void onCreate(Context context){

        WiFiDbUtils.context = context;
        WiFiDbUtils.executorService = Executors.newSingleThreadExecutor();
        WiFiDbUtils.cr = context.getContentResolver();
        loadPwds();
    }

    public static void onDestroy(){

        WiFiDbUtils.executorService.shutdown();

    }

    private static void loadPwds(){

        Runnable command = new Runnable() {
            @Override
            public void run() {
                Cursor cursor = cr.query(WiFiDbUtils.AP_PWD_CONTENTURI,PWD_PROJECT,null,null,null);
                if(cursor != null){
                    while (cursor.moveToNext()){
                        PWD pwd = new PWD();
                        String bssidAndSecurity = cursor.getString(0);
                        String[] s = spiltBssidAndSecurity(bssidAndSecurity);
                        if(s == null){
                            continue;
                        }
                        pwd.bssid = s[0];
                        pwd.security = Integer.parseInt(s[1]);
                        pwd.ssid = cursor.getString(1);
                        pwd.pwdCrack = cursor.getString(2);
                        pwd.csuccessNum = cursor.getInt(3);
                        pwd.cfailNum = cursor.getInt(4);
                        pwd.speed = cursor.getInt(5);
                        synchronized (cachePwds){
                            cachePwds.put(bssidAndSecurity,pwd);
                        }
                    }
                    cursor.close();
                }
                cursor = cr.query(WiFiDbUtils.AP_PWD_LOCAL_CONTENTURI,
                        PWD_LOCAL_PROJECT, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        LocalPWD pwd = new LocalPWD();
                        String bssidAndSecurity = cursor.getString(0);
                        String[] s = spiltBssidAndSecurity(bssidAndSecurity);
                        if (s == null) {
                            continue;
                        }
                        pwd.bssid = s[0];
                        pwd.security = Integer.parseInt(s[1]);
                        pwd.ssid = cursor.getString(1);
                        pwd.pwd = cursor.getString(2);
                        synchronized (cacheLocalPwds) {
                            cacheLocalPwds.put(bssidAndSecurity, pwd);
                        }
                    }
                    cursor.close();
                }
            }
        };
        Thread thread = new Thread(command);
        thread.setPriority(Thread.NORM_PRIORITY - 2);
        thread.start();
    }

    private static String[] spiltBssidAndSecurity(String bssidAndSecurity) {
        if (bssidAndSecurity == null || bssidAndSecurity.length() < 19) {
            return null;
        }
        int pos = bssidAndSecurity.lastIndexOf(":");
        if (pos < 0) {
            return null;
        }
        String bssid = bssidAndSecurity.substring(0, pos);
        String security = bssidAndSecurity.substring(pos + 1);
        try {
            Integer.parseInt(security);
        } catch (Exception e) {
            return null;
        }
        return new String[] { bssid, security };
    }

    private static String combineBssidAndSecurity(String bssid, int security) {
        return bssid + ":" + security;
    }

    private static String[] toArray(String... args) {
        return args;
    }

    public static boolean isLocalPwd(String bssid, int security){

        synchronized (cacheLocalPwds){
            String key = combineBssidAndSecurity(bssid,security);
            return cacheLocalPwds.containsKey(key);
        }
    }

    public static LocalPWD getLocalPwd(String bssid, int security){

        synchronized (cacheLocalPwds) {
            String key = combineBssidAndSecurity(bssid, security);
            return cacheLocalPwds.get(key);
        }
    }

    public static boolean removeLocalPwd(String bssid,int security){

        final String key = combineBssidAndSecurity(bssid,security);
        boolean has = false;
        synchronized (cacheLocalPwds){
            has = cacheLocalPwds.containsKey(key);
            cacheLocalPwds.remove(key);
        }
        if(has){
            Runnable command = new Runnable() {
                @Override
                public void run() {
                    cr.delete(WiFiDbUtils.AP_PWD_LOCAL_CONTENTURI,WHERE_BSSID,toArray(key));
                }
            };
            executorService.execute(command);
        }
        return has;
    }

    public static void saveLocalPwd(final String ssid, final String bssid, final String pwd){

        Runnable command = new Runnable() {
            @Override
            public void run() {
                int security = WifiAdmin.getInstance().getCurrentSecurityType(bssid);
                String key = combineBssidAndSecurity(bssid, security);
                String fpwd = AESUtil.encrypt(pwd);
                LocalPWD pwd = cacheLocalPwds.get(key);
                if(pwd != null && !TextUtils.equals(fpwd,pwd.pwd)){
                    pwd.pwd = fpwd;
                    ContentValues values = new ContentValues();
                    values.put(AP_PWD,fpwd);
                    cr.update(WiFiDbUtils.AP_PWD_LOCAL_CONTENTURI,values,WHERE_BSSID,toArray(key));
                }else {
                    pwd = new LocalPWD();
                    pwd.bssid = bssid;
                    pwd.ssid = ssid;
                    pwd.pwd = fpwd;
                    cacheLocalPwds.put(key,pwd);
                    ContentValues values = new ContentValues();
                    values.put(AP_BSSID,bssid);
                    values.put(AP_SSID,ssid);
                    values.put(AP_PWD,fpwd);
                    cr.insert(WiFiDbUtils.AP_PWD_LOCAL_CONTENTURI,values);
                }
            }
        };
        executorService.execute(command);
    }

    public static void savePortalContent(final String ssid, final String bssid, final String html){

        Runnable command = new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put(AP_BSSID,bssid);
                values.put(AP_SSID,ssid);
                values.put(AP_TT,System.currentTimeMillis());
                values.put(AP_PORTAL,html);
                cr.insert(AP_SPEED_CONTENTURI,values);
            }
        };
        executorService.execute(command);
    }

}
