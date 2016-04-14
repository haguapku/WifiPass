package com.haguapku.wificlient.application;

import android.app.Application;
import android.util.Log;

import com.haguapku.wificlient.WifiClientLib;

/**
 * Created by MarkYoung on 15/9/17.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
//        Log.v("----hagua----","Applicaiton onCreate");
        super.onCreate();
        WifiClientLib.initialize(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        WifiClientLib.unInitialize();
        super.onTerminate();
    }
}
