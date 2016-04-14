package com.haguapku.wificlient.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.haguapku.wificlient.util.WiFiUtil;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by MarkYoung on 15/10/27.
 */
public class WiFiMonitor extends BroadcastReceiver {

    private boolean isConnected = false;
    private int mNetworkId = -1;
    private String bssid;
    private String ssid;
    private Object lock = new Object();
    private Context mContext = null;
    private WifiManager mWifiManager = null;
    private ConnectivityManager mConnectivityManager = null;
    private Vector<WiFiConnectChanged> listeners = new Vector<>();
    private static WiFiMonitor instance = null;

    public WiFiMonitor(Context context) {
        this.mContext = context.getApplicationContext();
        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static synchronized WiFiMonitor getInstance(Context context){
        if(instance == null){
            instance = new WiFiMonitor(context);
            instance.init();
        }
        return instance;
    }

    public final boolean init(){

        Log.v("----hagua----","WiFiMonitor init");

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mContext.registerReceiver(this,filter);

        if(mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
            WifiInfo localWifiInfo = mWifiManager.getConnectionInfo();
            if(SupplicantState.COMPLETED == localWifiInfo.getSupplicantState()){

                Log.v("----hagua----","Wifi already connected");

                isConnected = true;
                bssid = WiFiUtil.normalizeBSSID(localWifiInfo.getBSSID());
                ssid = WiFiUtil.normalizeBSSID(localWifiInfo.getSSID());
                mNetworkId = localWifiInfo.getNetworkId();
            }
        }
        return true;
    }

    public final boolean registerListener(WiFiConnectChanged listener){
        synchronized (lock){
            if(listeners.contains(listener)){
                return false;
            }
            return listeners.add(listener);
        }
    }

    public final boolean unRegisterListener(WiFiConnectChanged listener){
        synchronized (lock){
            if(listeners.contains(listener)){
                return listeners.remove(listener);
            }
            return false;
        }
    }

    public boolean isConnected(){
        return isConnected;
    }

    public String getBssid(){
        return bssid;
    }

    public String getSsid(){
        return ssid;
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(networkInfo.isConnected()){
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if(wifiInfo == null || wifiInfo.getSSID() == null || wifiInfo.getBSSID() == null){
                return;
            }
            if(SupplicantState.COMPLETED == wifiInfo.getSupplicantState()){

                if(mNetworkId == wifiInfo.getNetworkId() || mWifiManager.getConfiguredNetworks() == null){
                    return;
                }
                synchronized (this.lock){
                    isConnected = true;
                    bssid = WiFiUtil.normalizeBSSID(wifiInfo.getBSSID());
                    ssid = WiFiUtil.normalizeBSSID(wifiInfo.getSSID());
                    mNetworkId = wifiInfo.getNetworkId();
                    Iterator<WiFiConnectChanged> it = listeners.iterator();
                    while(it.hasNext()){
                        it.next().onWifiConnected(bssid);
                    }
                }
            }
        }else {
            if(networkInfo.getState() != NetworkInfo.State.DISCONNECTED
                || networkInfo.getDetailedState() != NetworkInfo.DetailedState.DISCONNECTED
                    || !isConnected){
                return;
            }
            synchronized (this.lock) {
                isConnected = false;
                this.mNetworkId = -1;
                Iterator<WiFiConnectChanged> it = listeners.iterator();
                while (it.hasNext()) {
                    it.next().onWifiDisConnected(bssid);
                }
                bssid = null;
                ssid = null;
            }
        }
    }
}
