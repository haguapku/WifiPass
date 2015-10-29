package com.haguapku.wificlient.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.DateUtils;
import com.haguapku.wificlient.R;

/**
 * Created by MarkYoung on 15/10/29.
 */
public class NotificationUtil {

    public static final String ACTION_WIFI = "com.haguapku.wificlient.notice.action";

    public static void showNotification(Context context, String bssid, String ssid, int resultCode){
        hideNotification(context);

        if (!(resultCode == WiFiHttp.NETWORK_CONNECTED || resultCode == WiFiHttp.NETWORK_NEEDLOGIN)) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("notify_map",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        long prev = sharedPreferences.getLong(bssid,0);
        editor.putLong(bssid, System.currentTimeMillis());
        editor.commit();
        if(System.currentTimeMillis() -  prev < DateUtils.HOUR_IN_MILLIS){
            return;
        }
        String subtitle = getSubtitle(context, resultCode);
        SSIDTranslator.SsidInfo ssidInfo = SSIDTranslator.generateSsidInfo(ssid);
        if(ssidInfo!=null&&!TextUtils.isEmpty(ssidInfo.displayName)){
            ssid = ssidInfo.displayName;
        }
        String title = context.getString(R.string.wifi_notify_title) + ssid;
        Intent intent = new Intent(ACTION_WIFI);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("resultCode", resultCode);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,R.string.app_name,
                intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.wifi_notify_icon)
                .setTicker(title).setWhen(System.currentTimeMillis())
                .setContentTitle(ssid)
                .setContentText(subtitle)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.NOTIFY_ID,notification);
    }

    public  static  void  hideNotification(Context context){
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.NOTIFY_ID);
    }

    private static String getSubtitle(Context context,int resultCode){

        String subtitle = "";
        switch (resultCode){
            case WiFiHttp.NETWORK_CONNECTED:
                subtitle = context.getString(R.string.wifi_sate_connected);
                break;
            case WiFiHttp.NETWORK_NEEDLOGIN:
                subtitle = context.getString(R.string.wifi_desc_needlogin);
                break;
            case WiFiHttp.NETWORK_TIMEOUT:
                subtitle = context.getString(R.string.wifi_sate_timeout);
                break;
            case WiFiHttp.NETWORK_UNUSED:
                subtitle = context.getString(R.string.wifi_desc_work_unused);
                break;
            default:
                break;
        }
        return subtitle;
    }

}
