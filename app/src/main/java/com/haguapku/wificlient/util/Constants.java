package com.haguapku.wificlient.util;

/**
 * Created by MarkYoung on 15/10/21.
 */
public class Constants {

    /**
     * Handler Message action
     */
    public final static int MSG_ACTION_REFRESH_LIST=0;
    public final static int MSG_ACTION_FILL_APS_LIST=11;
    public final static int MSG_ACTION_HIDE_IMM=101;
    public final static int MSG_ACTION_CONNECT_TIMEOUT=102;
    public final static int MSG_ACTION_CURRENT_SPEED=66;
    public final static int MSG_ACTION_GET_LOCATION_SHARE=67;
    public final static int MSG_ACTION_CONNECT_DISAPPEAR=201;
    public final static int MSG_ACTION_NEGATIVE_1=204;
    public final static int MSG_ACTION_WIFI_CRACK=77;
    public final static int MSG_ACTION_WIFI_LBS=78;
    public final static int MSG_ACTION_WIFI_INFO_UPDATE=79;
    public final static int MSG_ACTION_WIFI_DETAIL=80;
    public final static int MSG_ACTION_WIFI_SHARE=81;
    public final static int MSG_ACTION_WIFI_LOACTION_TIMEOUT=15;

    public final static int MSG_ACTION_GUIDE_ACTION1=103;
    public final static int MSG_ACTION_GUIDE_ACTION2=104;


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
