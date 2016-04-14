package com.haguapku.wificlient;

import android.content.Context;
import android.util.Log;

import com.haguapku.wificlient.provider.WiFiDbUtils;
import com.haguapku.wificlient.service.WiFiServiceProxy;
import com.haguapku.wificlient.util.WifiAdmin;

/**
 * Created by MarkYoung on 15/9/17.
 */
public class WifiClientLib {

    public static Context context;

    public final static boolean DEBUG = true;

    public static void initialize(Context context){
//        Log.v("----hagua----", "initialize");
        WifiClientLib.context = context;
        WiFiDbUtils.onCreate(context);
        WifiAdmin.getInstance().onCreate(context);
        WiFiServiceProxy.getInstance().bindService(context);
    }

    public static void unInitialize(){
        WifiAdmin.getInstance().onDestroy();
        WiFiDbUtils.onDestroy();
        WiFiServiceProxy.getInstance().unBindService();
    }
}
