package com.haguapku.wificlient.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.haguapku.wificlient.R;
import com.haguapku.wificlient.bean.AccessPoint;
import com.haguapku.wificlient.bean.DmWifiInfo;
import com.haguapku.wificlient.producer.ItemBase;
import com.haguapku.wificlient.producer.ProducerUtils;
import com.haguapku.wificlient.producer.WifiItem;
import com.haguapku.wificlient.util.Constants;
import com.haguapku.wificlient.util.DmPreferenceManager;
import com.haguapku.wificlient.util.SSIDTranslator;
import com.haguapku.wificlient.util.SSIDTranslator.SsidInfo;
import com.haguapku.wificlient.util.WiFiHttp;
import com.haguapku.wificlient.util.WiFiUtil;
import com.haguapku.wificlient.util.WifiAdmin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MarkYoung on 15/10/20.
 */
public class ConnectView extends LinearLayout implements WifiAdmin.WifiStateChanged,
        View.OnClickListener,OnCancelListener{

    private Context context;
    private WifiAdmin mWifiAdmin;
    private View root;
    private ImageView mConnectedIcon;
    private TextView mConnectedSSID;
    private TextView mConnectedLuyou;
    private TextView mConnectedDesc;
    private TextView mConnectedShare;
    private TextView mConnectedTest;
    private TextView mConnectedSpeed;
    private TextView mConnectedLogin;
    private ImageView showLuncher;
    private View popView;
    private PopupWindow popAppLuncher;
    private boolean isShow = false;

    private ImageView mCloseInfo;
    private WifiSignalView mSignalView;
    private ProgressBar progressBar;
    private View option;
    private View etinfo;
    private View disconnect;
    private Handler handler;
    public String spwd = null;
    InputMethodManager imm;
    boolean bool = false;
    public boolean isSShow = false;
//    WiFiPwdServer wps;
    AccessPoint ap;
    public DmWifiInfo sinfo;
    private WiFiUtil mWiFiUtil;
    public static final long DAY_SECOND = 86400000;
    private long lastSharePressedTime = 0;
    Dialog dlg;
    int pwdCount = 8;
    private BroadcastReceiver myBroadcastReceiver;
    public String cSSID;
    public List<AccessPoint> keys = new ArrayList<>();

    private int screenHeight;

    public ConnectView(Context context, Handler handler) {
        super(context);
        this.context = context;
        this.handler = handler;
        root = LayoutInflater.from(context).inflate(R.layout.include_connected,null,false);
        addView(root);
        init();
        registerReceiver();
    }

    public void registerReceiver(){
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.CHECK_NETWROK_RESULT);
        context.registerReceiver(myBroadcastReceiver,filter);
    }

    public void unRegisterReceiver(){
        if(context != null && myBroadcastReceiver != null){
            context.unregisterReceiver(myBroadcastReceiver);
        }
    }

    public void init(){
        dlg = new Dialog(context,R.style.dm_alert_dialog);
        imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mWifiAdmin = WifiAdmin.getInstance();
        mWifiAdmin.registerCallback(null,this);
        mWiFiUtil = new WiFiUtil(context,mWifiAdmin.getWifiManager(),handler);
        mConnectedSSID = (TextView) root.findViewById(R.id.text_wifi_name);
        mConnectedDesc = (TextView) root.findViewById(R.id.text_wifi_desc);
        mConnectedLuyou = (TextView) root.findViewById(R.id.text_luyou_name);
        mConnectedShare = (TextView) root.findViewById(R.id.wifi_share);
        mConnectedTest = (TextView) root.findViewById(R.id.wifi_test);
        mConnectedSpeed = (TextView) root.findViewById(R.id.wifi_speed);
        mConnectedIcon = (ImageView) root.findViewById(R.id.image_avatar);
        mConnectedLogin = (TextView) root.findViewById(R.id.wifi_login);

        mConnectedIcon.setOnClickListener(this);
        mConnectedTest.setOnClickListener(this);
        mConnectedShare.setOnClickListener(this);
        mConnectedLogin.setOnClickListener(this);
        mConnectedSpeed.setOnClickListener(this);

        mSignalView = (WifiSignalView) root.findViewById(R.id.signalView);
        progressBar = (ProgressBar) root.findViewById(R.id.connect_progress);

        option = root.findViewById(R.id.lay_option);
        disconnect = root.findViewById(R.id.lay_disconnect);
        disconnect.setOnLongClickListener(lcl);

        etinfo = root.findViewById(R.id.lay_etinfo);
        etinfo.setOnClickListener(this);

        mCloseInfo = (ImageView) root.findViewById(R.id.action_closeinfo);
        mCloseInfo.setOnClickListener(this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
    }

    OnLongClickListener lcl = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            disconnect();
            return true;
        }
    };

    private void disconnect(){

    }

    private void resetView(){
        showProgressView();
        etinfo.setVisibility(View.GONE);
        mConnectedLogin.setVisibility(View.GONE);
        hideOptionView();
        mConnectedDesc.setText(R.string.wifi_sate_connecting);
        mConnectedSSID.setText("");
        mConnectedLuyou.setText("");
        mConnectedIcon.setImageResource(R.drawable.wifi_default_logo);
    }

    public void showConnectedView(AccessPoint ap){
        this.ap = ap;
        resetView();
        root.setVisibility(View.VISIBLE);
        mConnectedSSID.setText(ap.getSsid());
        mConnectedLuyou.setText(ap.getLuyouType());
        if(ap.getIcon() == 0){
            mConnectedIcon.setImageResource(ap.getIcon());
        }else {
            setApIcon(ap.getSsid().replace("\"", ""), ap.getBssid());
        }
        mSignalView.setValues(ap.getSignalIcon(), ap.getLevelPercent(), false, false, true);
    }

    public void hideConnectedView(){
        resetView();
        root.setVisibility(View.GONE);
    }

    public void setApIcon(String ssid,String bssid){
        SsidInfo ssidInfo = SSIDTranslator.generateSsidInfo(ssid);

        if(ssidInfo != null){
            mConnectedIcon.setImageResource(R.drawable.wifi_zapya_pc);
            mConnectedLuyou.setText(R.string.zapya_pc_ap);
        }else {
            ItemBase item = ProducerUtils.find(bssid, ssid);
            if(item != null){
                if(item instanceof WifiItem){
                    mConnectedLuyou.setText(((WifiItem) item).name);
                    String id = ((WifiItem) item).id;
                    if(!TextUtils.isEmpty(id)){
                        mConnectedIcon.setImageResource(ProducerUtils.getWiFiIcon(Integer.parseInt(id)));
                    }
                }
            }
        }
    }

    public void notifySignalChanged(){
        WifiInfo wifiInfo = mWifiAdmin.getWifiManager().getConnectionInfo();
        if(wifiInfo != null && wifiInfo.getBSSID() !=null &&
                wifiInfo.getSupplicantState() == SupplicantState.COMPLETED){
            int rssi = wifiInfo.getRssi();
            if(rssi == -200){
                return;
            }
            mSignalView.setValues(WiFiUtil.wifiLevel(rssi),WiFiUtil.signalPercent(rssi,100,1),false,false,true);
        }
    }

    public void showProgressView(){
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressView(){
        progressBar.setVisibility(View.GONE);
    }

    public void showOptionView(){
        Animation animation = AnimationUtils.loadAnimation(context,R.anim.search_scale);
        if(mConnectedSpeed != null && DmPreferenceManager.getInstance().getDisplayPassCodeAnim()){
            mConnectedSpeed.clearAnimation();
            mConnectedSpeed.setAnimation(animation);
        }
        option.setVisibility(View.VISIBLE);
    }

    public void hideOptionView(){
        option.setVisibility(View.GONE);
    }

    public void hideLoginView(){
        mConnectedLogin.setVisibility(View.GONE);
    }

    public void setDescView(int desc) {
        mConnectedDesc.setText(desc);
    }

    public boolean isOptionShow() {
        return option.getVisibility() == View.VISIBLE;
    }

    public boolean isShow() {

        return root.isShown() && (root.getVisibility() == View.VISIBLE);
    }

    public void initOriginalWifi(boolean isHave){
        WifiInfo wifiInfo = mWifiAdmin.getWifiManager().getConnectionInfo();
        if(wifiInfo != null && wifiInfo.getBSSID() != null &&
                wifiInfo.getSupplicantState() == SupplicantState.COMPLETED &&
                wifiInfo.getSupplicantState() == SupplicantState.FOUR_WAY_HANDSHAKE){
            root.setVisibility(View.VISIBLE);

            mConnectedIcon.setImageResource(R.drawable.wifi_default_logo);

            String ssid = wifiInfo.getSSID();
            String bssid = wifiInfo.getBSSID();


        }
    }

    final static int CHECK_WHAT = 200;

    public void checkNetworkState(String bssid){
        if(!WiFiUtil.isWifiAvailable()){
            hideConnectedView();
            return;
        }
        hideOptionView();
        etinfo.setVisibility(View.GONE);
        mConnectedLogin.setVisibility(View.GONE);
        showProgressView();
        mConnectedDesc.setText(R.string.wifi_sate_checking);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Constants.CHECK_NETWROK_RESULT.equals(action)){
                int code = intent.getIntExtra(Constants.CHECK_RESULT, WiFiHttp.NETWORK_UNUSED);
                String bssid = intent.getStringExtra(Constants.CHECK_BSSID);
                Message msg = descHandler.obtainMessage();
                msg.what = CHECK_WHAT;
                msg.arg1 = code;
                msg.arg2 = 1;
                msg.obj = bssid;
                descHandler.sendMessage(msg);
            }
        }
    }

    Handler descHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            WifiInfo wifiInfo = mWifiAdmin.getWifiManager().getConnectionInfo();
            String bssid = wifiInfo.getBSSID();
            int code = msg.arg1;
            boolean bind = msg.arg2 == 1 ? true : false;
            if(!bind || (bind && code == -1)){

            }
        }
    };

    public void getCheckResultCode(int delayMillis,String bssid){

    }

    @Override
    public void onCheckStatus(int state) {

    }

    @Override
    public void onChangeState(Intent intent, NetworkInfo.DetailedState detailedState, int errorCode) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCancel(DialogInterface dialog) {

    }
}
