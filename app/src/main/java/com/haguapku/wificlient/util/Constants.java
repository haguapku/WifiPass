package com.haguapku.wificlient.util;

/**
 * Created by MarkYoung on 15/10/21.
 */
public class Constants {

    public static final String SUCCESS_RESULT = "<HEAD><TITLE>Success</TITLE></HEAD><BODY>Success</BODY>";
    public static final String CHECK_APPLE_URL = "http://www.apple.com/library/test/success.html";
    public static final String CHECK_URL = "http://210.73.213.237/success.html";
    public static final String CHECK_360WIFI = "http://www.360.cn/wifi/sm.html?t=360wifi&d=";
    public static final String CHECK_KYWIFI = "http://www.kuaiya.cn/wifiMobileNewB?d=";

    public static final int NOTIFY_ID = 2211;

    /**
     * Check network connected action
     *
     * resultcode - check result code bssid - the checked network's bssid
     * isstart - if true is start checking
     *
     */
    public static final String CHECK_NETWROK_ACTION = "com.haguapku.wificlient.service.WiFiService.check";
    public static final String CHECK_NETWROK_RESULT = "com.haguapku.wificlient.service.WiFiService.result";
    public static final String CHECK_RESULT = "resultcode";
    public static final String CHECK_BSSID = "bssid";

    /**
     * Check network portal page portalAction: 0, stop; 1 start
     */
    public static final String CHECK_NETWORK_PORTAL = "com.haguapku.wificlient.service.WiFiService.portal";
    public static final String CHECK_PORTAL = "protalaction";
    public static final String WIFI_SERVICE = "com.haguapku.wificlient.service.WiFiService.main";

}
