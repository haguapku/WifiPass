package com.haguapku.wificlient.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Parcelable;


import com.haguapku.wificlient.WifiClientLib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by MarkYoung on 15/9/18.
 */
public class WifiAdmin {

    private static WifiAdmin instance = null;
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;

//    private Object mWifLock = new Object();
    private List<ScanResult> mWifiList;
    private List<WifiConfiguration> mWifiConfigurations;

    private WifiLock mWifiLock;

    private Object mCallbackLock = new Object();
    private List<ScanResultChanged> mSRC;
    private List<WifiStateChanged> mSSC;

    // If disabled, call the method is invalid .
    private boolean acceptScanning = true;
    // If enable, we will post the scan result to user, or after call startScan
    // and then user receive scan result .
    private boolean autoScan = false;
    // Identify the scan is running.
    private boolean scanning = true;

    private AtomicBoolean mConnect = new AtomicBoolean(false);
    private boolean DEBUG = WifiClientLib.DEBUG;
    private LinkedList<String> logs = new LinkedList<String>();
    private Map<String, Integer> securityTypeMap = new HashMap<String, Integer>();

    private BroadcastReceiver mBroadcastReceiver;
    private ExecutorService mExecutor;
    private Context mContext;

    public WifiAdmin() {
    }

    public void onCreate(Context context){

        mContext = context.getApplicationContext();

        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();

        mSRC = new ArrayList<ScanResultChanged>();
        mSSC = new ArrayList<WifiStateChanged>();

        mWifiList = mWifiManager.getScanResults();
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();

        mExecutor = new ThreadPoolExecutor(3, 10, 60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        });

        createWifiLock();
        mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.EXTRA_WIFI_STATE);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        filter.addAction("android.net.wifi.LINK_CONFIGURATION_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        mContext.registerReceiver(mBroadcastReceiver,filter);
    }

    public void onDestroy(){
        if(mBroadcastReceiver != null){
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        if(mExecutor != null){
            mExecutor.shutdown();
            mExecutor = null;
        }
    }

    public static synchronized WifiAdmin getInstance(){
        if(instance == null){
            instance = new WifiAdmin();
        }
        return instance;
    }

    public void openWifi(){

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                closeWifiAp();
                if(!mWifiManager.isWifiEnabled()){
                    mWifiManager.setWifiEnabled(true);
                }
            }
        });
    }

    public void closeWifi(){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mWifiManager.isWifiEnabled()){
                    mWifiManager.setWifiEnabled(false);
                }
            }
        });
    }

    public int checkState(){
        return mWifiManager.getWifiState();
    }

    public void acquireWifiLock(){
        try{
            mWifiLock.acquire();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void releaseWifiLock(){
        try{
            if(mWifiLock.isHeld()){
                mWifiLock.release();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void createWifiLock(){
        mWifiLock = mWifiManager.createWifiLock("wifiadmin");
    }

    public void setAcceptScanning(boolean accept){
        acceptScanning = accept;
    }

    public void setAutoScan(boolean enable){
        autoScan = enable;
    }

    public WifiManager getWifiManager(){
        return mWifiManager;
    }

    public List<WifiConfiguration> getWifiConfiguration(){
        return mWifiManager.getConfiguredNetworks();
    }

    public void connectionConfiguration(final int index){
        if(index > mWifiConfigurations.size()){
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId,true);
            }
        });
    }

    public void startScan(){
        if(acceptScanning){
            scanning = true;
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    mWifiManager.startScan();
                    synchronized (mWifiLock){
                        List<ScanResult> r = null;
                        if(mWifiList != null){
                            r = new ArrayList<ScanResult>(mWifiList);
                        }
                        mWifiList = mWifiManager.getScanResults();
                        if(mWifiList == null){
                            mWifiList = r;
                        }
                        saveSecurityType(mWifiList);
                        mWifiConfigurations = mWifiManager.getConfiguredNetworks();
                    }
                    postResultChangedNotify(null);
                }
            });
        }
    }

    public void stopScan(){
        scanning = false;
    }

    public List<ScanResult> getWifiList(){
        return mWifiList;
    }

    public void registerCallback(ScanResultChanged scancb,WifiStateChanged wificb){
        synchronized (mCallbackLock){
            if(scancb != null){
                mSRC.add(scancb);
                postResultChangedNotify(scancb);
            }
            if(wificb != null){
                mSSC.add(wificb);
            }
        }
    }

    public void unRegisterCallback(ScanResultChanged scancb,WifiStateChanged wificb){
        synchronized (mCallbackLock){
            if(scancb != null){
                mSRC.remove(scancb);
            }
            if(wificb != null){
                mSSC.remove(wificb);
            }
        }
    }

    public void saveSecurityType(List<ScanResult> results){
        if(results == null || results.isEmpty()){
            return;
        }
        synchronized (securityTypeMap){
            for(ScanResult scanResult : results){
                securityTypeMap.put(scanResult.BSSID,WiFiUtil.wifiType(scanResult.capabilities));
            }
        }
    }

    public int getCurrentSecurityType(String bssid){
        if(securityTypeMap.containsKey(bssid)){
            return securityTypeMap.get(bssid);
        }
        return WiFiUtil.WIFI_WPA;
    }

    public void postWifiStateChangedNotify(final int state){
        for(final WifiStateChanged callback : mSSC){
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onCheckStatus(state);
                }
            });
        }
    }

    public void postDetailedWifiStateChangedNotify(final Intent intent, final DetailedState state, final int errorCode){
        for(final WifiStateChanged callback : mSSC){
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onChangeState(intent, state, errorCode);
                }
            });
        }
    }

    public void postResultChangedNotify(final ScanResultChanged cb){
        List<ScanResult> r = null;
        synchronized (mWifiLock){
            if(mWifiList != null && ! mWifiList.isEmpty()){
                r = new ArrayList<>(mWifiList);
            }
        }
        try{
            List<ScanResult> fr = r;
            if(cb != null){
                postResultChanged(cb,fr);
            }else {
                synchronized (mCallbackLock){
                    for(final ScanResultChanged callback : mSRC){
                        callback.onScanResultChanged(fr);
                    }
                }
            }
        }catch (ConcurrentModificationException e){
            e.printStackTrace();
        }
    }

    public void postResultChanged(final ScanResultChanged cb, final List<ScanResult> results){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                cb.onScanResultChanged(results);
            }
        });
    }

    public String getMacAddress(){
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    public String getBSSID(){
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    public int getIPAddress(){
        if(mWifiInfo != null){
            return mWifiInfo.getIpAddress();
        }
        return -1;
    }

    public int getNetWorkID(){
        return (mWifiInfo == null) ? -1 : mWifiInfo.getNetworkId();
    }

    public WifiInfo getWifiInfo(){
        return mWifiManager.getConnectionInfo();
    }

    public void addNetWork(WifiConfiguration configuration){
        int wcgId = mWifiManager.addNetwork(configuration);
        mWifiManager.enableNetwork(wcgId,true);
    }

    public void disConnectionWifi(int netId){
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    public boolean isWifiApEnabled(){
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean)method.invoke(mWifiManager);
        }
        catch(NoSuchMethodException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void closeWifiAp(){
        if(isWifiApEnabled()){
            try {
                Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration conf = (WifiConfiguration) method.invoke(mWifiManager);
                Method method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
                        WifiConfiguration.class,boolean.class);
                method1.invoke(mWifiManager,conf,false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e){
                e.printStackTrace();
            }catch (InvocationTargetException e){
                e.printStackTrace();
            }
        }
    }

    public ScanResult isExitInWifiList(ScanResult sr){
        for(ScanResult scanResult : mWifiList){
            if(sr.BSSID.equals(scanResult.BSSID)){
                return scanResult;
            }
        }
        return null;
    }

    class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            StringBuffer log = null;
            if (DEBUG) {
                log = new StringBuffer();
                log.append("Action:").append(action).append("\n");
            }

            if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)
                    || "android.net.wifi.LINK_CONFIGURATION_CHANGED".equals(action)
                    || "android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action)){
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,0);
                postWifiStateChangedNotify(state);
                if (DEBUG) {
                    log.append("state").append(state);
                }
            }else if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(null != parcelableExtra){
                    NetworkInfo networkInfo = (NetworkInfo)parcelableExtra;
                    DetailedState detailedState = networkInfo.getDetailedState();
                    int errorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,-1);
                    mConnect.set(networkInfo.isConnected());
                    postDetailedWifiStateChangedNotify(intent,detailedState,errorCode);
                    synchronized (mWifiLock){
                        mWifiList = mWifiManager.getScanResults();
                        mWifiConfigurations = mWifiManager.getConfiguredNetworks();
                    }
                    postResultChangedNotify(null);
                    if (DEBUG) {
                        log.append("DetailedState:").append(detailedState).append("\n");
                        log.append("errorCode:").append(errorCode);
                    }
                }
            }else if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
                if(scanning || autoScan){
                    synchronized (mWifiLock){
                        List<ScanResult> tempScanResult = mWifiManager.getScanResults();
                        if(tempScanResult != null && !tempScanResult.isEmpty()){
                            mWifiList = tempScanResult;
                            saveSecurityType(tempScanResult);
                        }
                        List<WifiConfiguration> tempConfigs = mWifiManager.getConfiguredNetworks();
                        if(tempConfigs != null && !tempConfigs.isEmpty()){
                            mWifiConfigurations = tempConfigs;
                        }
                    }
                    postResultChangedNotify(null);
                }
                if (DEBUG) {
                    log.append("scanning:").append(scanning).append("\n");
                    log.append("autoScan:").append(autoScan);
                }
            }else if(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)){
                SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                DetailedState detailedState = WifiInfo.getDetailedStateOf(supplicantState);
                int errorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,-1);
                postDetailedWifiStateChangedNotify(intent,detailedState,errorCode);
                if (DEBUG) {
                    log.append("SupplicantState:").append(supplicantState)
                            .append("\n");
                    log.append("DetailedState:").append(detailedState)
                            .append("\n");
                    log.append("errorCode:").append(errorCode);
                }
            }else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){

            }
            if (DEBUG) {
                addToLogList(log.toString());
            }
        }
    }

    public static boolean isHandshakeState(SupplicantState state) {
        switch (state) {
            case AUTHENTICATING:
            case ASSOCIATING:
            case ASSOCIATED:
            case FOUR_WAY_HANDSHAKE:
            case GROUP_HANDSHAKE:
                return true;
            case COMPLETED:
            case DISCONNECTED:
            case INTERFACE_DISABLED:
            case INACTIVE:
            case SCANNING:
            case DORMANT:
            case UNINITIALIZED:
            case INVALID:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

    private synchronized void addToLogList(String log) {
        if (logs.size() > 500) {
            logs.removeFirst();
        }
        logs.add(log);
    }

    public synchronized List<String> getLogs() {
        if (!logs.isEmpty()) {
            ArrayList<String> r = new ArrayList<String>(logs);
            logs.clear();
            return r;
        }
        return null;
    }

    public static interface ScanResultChanged {
        /**
         * If scan result changed, will callback to user's interface
         *
         * @param results
         */
        void onScanResultChanged(List<ScanResult> results);
    }

    public static interface WifiStateChanged {
        /**
         * If wifi status changed, will callback user interface
         *
         * @param state
         *            {@link android.net.wifi.WifiManager#EXTRA_WIFI_STATE}
         *
         * @see android.net.wifi.WifiManager#WIFI_STATE_DISABLED
         * @see android.net.wifi.WifiManager#WIFI_STATE_DISABLING
         * @see android.net.wifi.WifiManager#WIFI_STATE_ENABLED
         * @see android.net.wifi.WifiManager#WIFI_STATE_ENABLING
         * @see android.net.wifi.WifiManager#WIFI_STATE_UNKNOWN
         */
        void onCheckStatus(int state);

        /**
         * If connection status changed, will callback user's interface.
         *
         * @param detailedState
         *
         * @param errorCode
         *
         * @see android.net.NetworkInfo.DetailedState#IDLE
         * @see android.net.NetworkInfo.DetailedState#SCANNING
         * @see android.net.NetworkInfo.DetailedState#CONNECTING
         * @see android.net.NetworkInfo.DetailedState#AUTHENTICATING
         * @see android.net.NetworkInfo.DetailedState#OBTAINING_IPADDR
         * @see android.net.NetworkInfo.DetailedState#CONNECTED
         * @see android.net.NetworkInfo.DetailedState#SUSPENDED
         * @see android.net.NetworkInfo.DetailedState#DISCONNECTING
         * @see android.net.NetworkInfo.DetailedState#DISCONNECTED
         * @see android.net.NetworkInfo.DetailedState#FAILED
         * @see android.net.NetworkInfo.DetailedState#BLOCKED
         * @see android.net.NetworkInfo.DetailedState#VERIFYING_POOR_LINK
         * @see android.net.NetworkInfo.DetailedState#CAPTIVE_PORTAL_CHECK
         */
        void onChangeState(Intent intent,DetailedState detailedState, int errorCode);
    }

}
