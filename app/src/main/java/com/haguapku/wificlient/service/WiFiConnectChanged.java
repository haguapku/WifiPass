package com.haguapku.wificlient.service;

/**
 * Created by MarkYoung on 15/10/28.
 */
public interface WiFiConnectChanged {

    void onWifiConnected(String string);
    void onWifiDisConnected(String string);

}
