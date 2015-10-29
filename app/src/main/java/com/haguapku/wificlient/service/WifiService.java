package com.haguapku.wificlient.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.haguapku.wificlient.WifiClientLib;
import com.haguapku.wificlient.util.Constants;
import com.haguapku.wificlient.util.NotificationUtil;
import com.haguapku.wificlient.util.WiFiHttp;

public class WifiService extends Service implements WiFiConnectChanged{

    private static final int CHECK_NETWORK = 1;
    private static final int CHECK_PORTAL = 2;
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
        mWiFiMonitor = WiFiMonitor.getInstance(this);
        mWiFiMonitor.registerListener(this);
        mThread = new HandlerThread("wifi");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper(),new ThreadHandler());
        if(mWiFiMonitor.isConnected()){
            mNeedShowNotify = false;
            sendMessage(CHECK_NETWORK,0,mWiFiMonitor.getBssid(),250);
        }
    }

    @Override
    public void onDestroy() {
        mWiFiMonitor.unRegisterListener(this);
        mThread.quit();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(Constants.CHECK_NETWORK_PORTAL.equals(action)){
                int portalAction = intent.getIntExtra(Constants.CHECK_PORTAL,0);
                if(portalAction == 0){
                    mThreadHandler.removeMessages(CHECK_PORTAL);
                }else {
                    sendMessage(CHECK_PORTAL,0,mWiFiMonitor.getBssid(),0);
                }
            }else if (Constants.CHECK_NETWROK_ACTION.equals(action)){
                int networkAction = intent.getIntExtra("NA",-1);
                if(mResultCode != WiFiHttp.NETWORK_CONNECTED && mWiFiMonitor.isConnected()){
                    mResultCode = WiFiHttp.NETWORK_UNCHECK;
                    mNeedShowNotify = true;
                    sendMessage(CHECK_NETWORK,0,mWiFiMonitor.getBssid(),250);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendMessage(int what,int arg,Object obj,long delay){
        Message message = mThreadHandler.obtainMessage(what,arg,0,obj);
        mThreadHandler.removeMessages(what);
        mThreadHandler.sendMessageDelayed(message,delay);
    }

    class ThreadHandler implements Handler.Callback{

        @Override
        public boolean handleMessage(Message msg) {
            String bssid;
            switch (msg.what){
                case CHECK_NETWORK:
                    bssid = mWiFiMonitor.getBssid();
                    if(TextUtils.isEmpty(bssid) || !bssid.equals(msg.obj)
                            || !mWiFiMonitor.isConnected()){
                        return true;
                    }
                    mCheckSeq++;
                    for(int i=0; i<4; i++){
                        if(WiFiHttp.isResultAccurate(mResultCode)){
                            break;
                        }
                        new Thread(new CheckNetworkTask(bssid,URLS[i%URLS.length],mCheckSeq)).start();
                    }
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    if(!WiFiHttp.isResultAccurate(mResultCode)){
                        sendMessage(CHECK_TIMEOUT,mCheckSeq,bssid,10000);
                    }
                    break;
                case CHECK_TIMEOUT:
                    if(mResultCode == WiFiHttp.NETWORK_UNCHECK && mCheckSeq == msg.arg1){
                        mResultCode = WiFiHttp.NETWORK_TIMEOUT;
                        sendMessage(NOTIFY,0,msg.obj,0);
                    }
                    break;
                case CHECK_PORTAL:
                    bssid = (String) msg.obj;
                    if(!mWiFiMonitor.isConnected() || bssid == null
                            ||bssid.equals(mWiFiMonitor.getBssid())){
                        return true;
                    }
                    new Thread(new checkPortalTask(bssid)).start();
                    sendMessage(CHECK_PORTAL,0,bssid,1000);
                    break;
                case NOTIFY:
                    notifyResultCode((String)msg.obj);
                    break;
            }
            return false;
        }
    }

    public class CheckNetworkTask implements Runnable{

        private String bssid;
        private String url;
        private int checkSeq;

        public CheckNetworkTask(String bssid, String url, int checkSeq) {
            this.bssid = bssid;
            this.url = url;
            this.checkSeq = checkSeq;
        }

        @Override
        public void run() {
            if(mResultCode == WiFiHttp.NETWORK_UNCHECK && checkSeq == mCheckSeq){
                long time = System.currentTimeMillis();
                int result = WiFiHttp.checkNetWork(url);

                long interval = System.currentTimeMillis() - time;
                if(result != WiFiHttp.NETWORK_NEEDLOGIN && result != WiFiHttp.NETWORK_CONNECTED
                        && interval < 2500){
                    try{
                        Thread.sleep(2500 - interval);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    result = WiFiHttp.checkNetWork(url);
                }
                synchronized (lock){
                    if(WiFiHttp.isResultAccurate(mResultCode) || checkSeq != mCheckSeq){
                        return;
                    }
                    if(WiFiHttp.isResultAccurate(result)){
                        checkSeq++;
                        mResultCode = result;
                        mThreadHandler.removeMessages(CHECK_TIMEOUT);
                        sendMessage(NOTIFY,0,bssid,0);
                    }
                }
            }
        }
    }

    public class checkPortalTask implements Runnable{

        private String bssid;

        public checkPortalTask(String bssid) {
            this.bssid = bssid;
        }

        @Override
        public void run() {

            if(mResultCode == WiFiHttp.NETWORK_NEEDLOGIN || mResultCode == WiFiHttp.NETWORK_UNCHECK ){
                int result = WiFiHttp.checkNetWork(Constants.CHECK_URL);
                if(result == WiFiHttp.NETWORK_CONNECTED){
                    synchronized (lock){
                        mResultCode = result;
                    }
                    mThreadHandler.removeMessages(CHECK_PORTAL);
                    sendMessage(NOTIFY,0,bssid,0);
                    return;
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new WiFiServiceImpl();
    }

    @Override
    public void onWifiConnected(String string) {
        mResultCode = WiFiHttp.NETWORK_UNCHECK;
        mNeedShowNotify = true;
        sendMessage(CHECK_NETWORK,0,mWiFiMonitor.getBssid(),0);
    }

    @Override
    public void onWifiDisConnected(String string) {
        mResultCode = WiFiHttp.NETWORK_UNCHECK;
        NotificationUtil.hideNotification(this);
        mThreadHandler.removeMessages(CHECK_PORTAL);
        mThreadHandler.removeMessages(CHECK_NETWORK);
        mThreadHandler.removeMessages(CHECK_TIMEOUT);
        mThreadHandler.removeMessages(NOTIFY);
    }

    private void notifyResultCode(String bssid){

        mThreadHandler.removeMessages(CHECK_TIMEOUT);
        mThreadHandler.removeMessages(NOTIFY);

        if (TextUtils.isEmpty(bssid)) {
            return;
        }
        if (!bssid.equals(mWiFiMonitor.getBssid())) {
            return;
        }

        String ssid = mWiFiMonitor.getSsid();
        if (TextUtils.isEmpty(ssid)) {
            return;
        }

        int result = mResultCode == WiFiHttp.NETWORK_TIMEOUT ? WiFiHttp.NETWORK_UNUSED : mResultCode;

        if(mNeedShowNotify){
            NotificationUtil.showNotification(this, bssid, ssid, result);
        }

        Intent intent = new Intent(Constants.CHECK_NETWROK_RESULT);
        intent.putExtra(Constants.CHECK_RESULT, result);
        intent.putExtra(Constants.CHECK_BSSID, bssid);
        sendBroadcast(intent);
    }
}
