package com.haguapku.wificlient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;

import com.haguapku.wificlient.WifiClientLib;
import com.haguapku.wificlient.util.Constants;
import com.haguapku.wificlient.util.WiFiHttp;

public class WifiService extends Service {

    private static final int CHECK_NETWROK = 1;
    private static final int CHECK_PROTAL = 2;
    private static final int CHECK_TIMEOUT = 3;
    private static final int NOTIFY = 4;

    private static final boolean DEBUG = WifiClientLib.DEBUG;

    private WiFiMonitor mWiFiMonitor;
    private Handler mThreadHandler;
    private HandlerThread mThread;
    private int mCheckSeq = 0;
    private boolean mNeedShowNotify = false;
    public int mResultCode = WiFiHttp.NETWORK_UNCHECK;
    private Object lock = new Object();

    private final String[] URLS = { Constants.CHECK_APPLE_URL,
            Constants.CHECK_URL, Constants.CHECK_APPLE_URL };

    class WiFiServiceImpl extends IWiFiService.Stub{

        @Override
        public int getNetWorkState() throws RemoteException {
            return mResultCode == WiFiHttp.NETWORK_TIMEOUT ? WiFiHttp.NETWORK_UNUSED : mResultCode;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new WiFiServiceImpl();
    }
}
