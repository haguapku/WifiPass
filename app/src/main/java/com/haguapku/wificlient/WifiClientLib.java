package com.haguapku.wificlient;

import android.content.Context;

import com.haguapku.wificlient.util.WifiAdmin;

/**
 * Created by MarkYoung on 15/9/17.
 */
public class WifiClientLib {

    public static Context context;

    public final static boolean DEBUG = true;

    public static void initialize(Context context){
        WifiClientLib.context = context;
        WifiAdmin.getInstance().onCreate(context);
    }

    public static void unInitialize(){
        WifiAdmin.getInstance().onDestroy();
    }
}
