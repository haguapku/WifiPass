package com.haguapku.wificlient.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.haguapku.wificlient.WifiClientLib;

/**
 * Created by MarkYoung on 15/9/17.
 */
public class DmPreferenceManager {

    public static final String SETTING_FEED_NEW = "setting_feed_new";
    private static final boolean DEF_SETTING_FEED_NEW = false;

    public static final String DISPLAY_SCAN_RED_POINT = "display_scan_red_point";
    public static final String DISPLAY_PASS_CODE_ANIM = "display_pass_code_anim";

    public static final String DISPLAY_NEW_GUIDE_PAGE = "display_new_guide_page";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static DmPreferenceManager instance;
    private Context mContext;

    public DmPreferenceManager(Context context) {
        this.mContext = context;
        init();
    }

    public static synchronized DmPreferenceManager getInstance(){
        if(instance == null){
            instance = new DmPreferenceManager(WifiClientLib.context);
        }
        return instance;
    }

    private void init(){
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = preferences.edit();
    }

    public static void savePreference(final SharedPreferences.Editor editor) {
        try{
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                editor.apply();
            } else {
                editor.commit();
            }
        }catch(OutOfMemoryError e){
            e.printStackTrace();
        }
    }

    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        savePreference(editor);
    }

    public boolean getBoolean(String key, Boolean defValue){
        return preferences.getBoolean(key, defValue);
    }

    public boolean getFeedNew(){
        return getBoolean(SETTING_FEED_NEW,DEF_SETTING_FEED_NEW);
    }

    public boolean getDisplayScan(){
        return getBoolean(DISPLAY_SCAN_RED_POINT,true);
    }

    public boolean getDisplayPassCodeAnim() {
        return getBoolean(DISPLAY_PASS_CODE_ANIM,true);
    }

    public boolean getDisplayNewGuidePage() {
        return getBoolean(DISPLAY_NEW_GUIDE_PAGE, true);
    }

    public void setDisplayNewGuidePage(boolean value) {
        setBoolean(DISPLAY_NEW_GUIDE_PAGE, value);
    }
}
