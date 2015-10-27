package com.haguapku.wificlient.util;

import com.server.ServerSDK;
import com.server.mode.Method;
import com.server.mode.Mode;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by MarkYoung on 15/9/21.
 */
public class EventUtil {

    public static final int EVENT_START = 0;
    public static final int EVENT_STOP = 0;
    public static final int EVENT_UPGRADE = 0;
    public static final int EVENT_DOWNLOAD = 0;
    public static final int EVENT_EXCEPTION = 0;

    public static void sendEvent(int type, JSONObject data){

        JSONObject json = new JSONObject();
        try {
            json.put("a",type);
            json.put("ts",System.currentTimeMillis());
            if(data != null){
                json.put("m",data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServerSDK.getInstance().addJob(Mode.ANY, Method.POST,"/v1/events",json.toString());
    }

    public static final void sendEvent(int type){
        sendEvent(type,null);
    }

}
