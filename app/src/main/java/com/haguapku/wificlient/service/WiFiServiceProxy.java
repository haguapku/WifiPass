package com.haguapku.wificlient.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.haguapku.wificlient.util.Constants;
import com.haguapku.wificlient.util.WiFiHttp;

/**
 * Created by MarkYoung on 15/10/29.
 */
public class WiFiServiceProxy {

    private static WiFiServiceProxy instance = null;

    private boolean isBind = false;
    private Context context = null;
    private IWiFiService mService = null;

    public static synchronized WiFiServiceProxy getInstance(){
        if(instance == null){
            instance = new WiFiServiceProxy();
        }
        return instance;
    }

    public void bindService(Context context){

        try{
            if(!isBind){
                this.context = context;
                Intent intent = new Intent(context,WifiService.class);
                intent.setAction(Constants.WIFI_SERVICE);

                context.bindService(intent,conn,Service.BIND_AUTO_CREATE);
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public void unBindService(){

        if (isBind && mService != null) {
            isBind = false;
            context.unbindService(conn);
        }

    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

//            Log.v("----hagua----","onServiceConnected");

            mService = IWiFiService.Stub.asInterface(service);
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isBind = false;
        }
    };

    public boolean isBind() {
        return isBind;
    }

    public int getNetworkState(){

        Log.v("----hagua----","isBind = "+isBind);

        if(isBind){

            try {
                return mService.getNetWorkState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return WiFiHttp.NETWORK_UNCHECK;
    }
}
