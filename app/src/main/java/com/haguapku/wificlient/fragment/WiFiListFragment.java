package com.haguapku.wificlient.fragment;

import android.app.Dialog;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.haguapku.wificlient.R;
import com.haguapku.wificlient.activity.ApDetailActivity;
import com.haguapku.wificlient.activity.SpeedTestActivity;
import com.haguapku.wificlient.adapter.WifiListAdapter;
import com.haguapku.wificlient.bean.AccessPoint;
import com.haguapku.wificlient.service.WifiService;
import com.haguapku.wificlient.util.Constants;
import com.haguapku.wificlient.util.DmPreferenceManager;
import com.haguapku.wificlient.util.ToastUtil;
import com.haguapku.wificlient.util.WifiAdmin;
import com.haguapku.wificlient.util.WiFiUtil;
import com.haguapku.wificlient.view.ConnectView;
import com.haguapku.wificlient.widget.ActionPwdInputDailog;
import com.haguapku.wificlient.widget.XExpandableListView;
import com.haguapku.wificlient.widget.XListViewHeader;

import java.util.List;
import java.util.Map;


/**
 * Created by MarkYoung on 15/10/14.
 */
public class WiFiListFragment extends Fragment implements View.OnClickListener,
        XExpandableListView.IXListViewListener,ExpandableListView.OnChildClickListener,
        WifiAdmin.ScanResultChanged,WifiAdmin.WifiStateChanged,
        AdapterView.OnItemLongClickListener,ActionPwdInputDailog.OnActionSheetSelected,
        Dialog.OnCancelListener{

    private XExpandableListView mListView;
    private View mCrackButton;
    private View mProgressView;
    private Button mCancelButton;
    private Button mWifiSwitchButton;
    private MyReceiver myReceiver;
    private WiFiUtil mWifiUtil;
    InputMethodManager imm;
    private View emptyView;
    private View guideView1;
    private View guideView2;
//    private WiFiPwdServer wps;
    public static AccessPoint ap;
    private WifiAdmin mWifiAdmin;
    private WifiListAdapter adapter;
    private ConnectView mConnectView;
    CheckedTextView ctv;
    AnimationDrawable animationDrawable;
    ImageView wifiImg;

    public static final int TIME_OUT = 30000;
    public static final int GUIDE_SHOW_TIME= 6500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerDateTransReceiver();
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mWifiAdmin = WifiAdmin.getInstance();
        mWifiUtil = new WiFiUtil(getActivity(),mWifiAdmin.getWifiManager(),handler);
        mWifiAdmin.registerCallback(this,this);
        mWifiAdmin.startScan();
        adapter = new WifiListAdapter(getActivity(),null);
        handler.postDelayed(flow,1000);
        Intent intent = new Intent(getActivity(),WifiService.class);
        intent.setAction(Constants.CHECK_NETWROK_ACTION);
        getActivity().startService(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wifilist,null,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mCrackButton = view.findViewById(R.id.btn_getwifi_info);
        mCrackButton.setOnClickListener(this);
        mProgressView = view.findViewById(R.id.lay_inner);
        mCancelButton = (Button) view.findViewById(R.id.btn_getwifi_cancel);
        mCancelButton.setOnClickListener(this);
        emptyView = view.findViewById(R.id.lay_empty);
        guideView1 = view.findViewById(R.id.lay_guide1);
        guideView2 = view.findViewById(R.id.lay_guide2);
        guideView1.setOnClickListener(this);
        guideView2.setOnClickListener(this);
        mWifiSwitchButton = (Button) view.findViewById(R.id.btn_wifi_switch);
        mWifiSwitchButton.setOnClickListener(this);
        ctv = (CheckedTextView) view.findViewById(R.id.checked_text_wifi);
        wifiImg = (ImageView) view.findViewById(R.id.anim_open_wifi);
        mListView = (XExpandableListView) view.findViewById(R.id.list);
        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(true);
        mListView.setXListViewListener(this);
        mListView.setOnChildClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mConnectView = new ConnectView(getActivity(),handler);
        mListView.addHeaderView(mConnectView);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_add,null,false);
        v.findViewById(R.id.item_add).setOnClickListener(this);
        mListView.setAdapter(adapter);
        mListView.expandAllGroup();
        boolean isOpen = mWifiAdmin.getWifiManager().isWifiEnabled();
        if(isOpen && mListView.getRefreshState() != XListViewHeader.STATE_REFRESHING){
            mListView.startRefresh(false);
        }
        mConnectView.hideConnectedView();
        mConnectView.initOriginalWifi(true);

    }

    @Override
    public void onDestroy() {

        mWifiAdmin.unRegisterCallback(this,this);
        getActivity().unregisterReceiver(myReceiver);

        if(flow != null){
            handler.removeCallbacks(flow);
            flow = null;
        }

        super.onDestroy();
    }

    @Override
    public void onDestroyView() {

        mConnectView.unRegisterReceiver();

        super.onDestroyView();
    }

    private void guideAction1(){
        guideView1.setVisibility(View.GONE);
        guideView2.setVisibility(View.VISIBLE);
        handler.sendEmptyMessageDelayed(Constants.MSG_ACTION_GUIDE_ACTION2, GUIDE_SHOW_TIME);
    }

    private void guideAction2(){
        guideView2.setVisibility(View.GONE);
        DmPreferenceManager.getInstance().setDisplayNewGuidePage(false);
    }

    private void timeout(){
        WifiInfo wi = mWifiAdmin.getWifiManager().getConnectionInfo();
        SupplicantState state = wi.getSupplicantState();

        if(mConnectView.isShow()){
            mWifiAdmin.getWifiManager().disconnect();
            mWifiAdmin.startScan();
            mWifiAdmin.releaseWifiLock();
            mConnectView.hideConnectedView();
            int rssi = 0;
            if (wi != null && (wi.getBSSID() != null)) {
                rssi = WiFiUtil.signalPercent(wi.getRssi(), 100, 1);
            }
            if (rssi < 35 && rssi > 1) {
                ToastUtil.show(getActivity(), R.string.wifi_rssi_timeout);
            } else {
                ToastUtil.show(getActivity(), R.string.wifi_state_timeout);
            }
        }

    }

    private void sendTimeOutMsg() {
        handler.removeMessages(Constants.MSG_ACTION_CONNECT_TIMEOUT);
        Message msg = new Message();
        msg.what = Constants.MSG_ACTION_CONNECT_TIMEOUT;
        handler.sendMessageDelayed(msg, TIME_OUT);
    }

    Thread flow = new Thread(new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.obj = WiFiUtil.getSpeed();
            msg.what = Constants.MSG_ACTION_CURRENT_SPEED;
            handler.sendMessage(msg);
        }
    });

    private Handler handler = new Handler(){

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case Constants.MSG_ACTION_GUIDE_ACTION1:
                    guideAction1();
                    break;
                case Constants.MSG_ACTION_GUIDE_ACTION2:
                    guideAction2();
                    break;
                case Constants.MSG_ACTION_REFRESH_LIST:
                    mListView.stopRefresh();
                    mWifiAdmin.stopScan();
                    break;
                case Constants.MSG_ACTION_WIFI_SHARE:
                    break;
                case Constants.MSG_ACTION_FILL_APS_LIST:
                    adapter.setList((Map<String, List<AccessPoint>>) msg.obj);
                    mConnectView.setKeys(adapter.getKeyDatas());
                    mListView.expandAllGroup();
                    break;
                case Constants.MSG_ACTION_HIDE_IMM:
                    try {
                        InputMethodManager imm = (InputMethodManager) getActivity().
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput((EditText) msg.obj, InputMethodManager.RESULT_SHOWN);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.MSG_ACTION_CONNECT_TIMEOUT:
                    timeout();
                    break;
                case 90:
                    mWifiAdmin.startScan();
                    handler.removeMessages(Constants.MSG_ACTION_CONNECT_TIMEOUT);
                    mConnectView.hideConnectedView();
                    break;
                case Constants.MSG_ACTION_CURRENT_SPEED:
                    String speed = (String) msg.obj;
                    mConnectView.setSpeed(speed);
                    handler.postDelayed(flow,1000);
                    break;
                case Constants.MSG_ACTION_GET_LOCATION_SHARE:
                    break;
                case Constants.MSG_ACTION_CONNECT_DISAPPEAR:
                    ConnectivityManager connectivityManager =
                            (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if(info == null || !info.isConnectedOrConnecting()){
                        authBSSID = null;
                        mWifiAdmin.startScan();
                        mConnectView.hideConnectedView();
                        handler.removeMessages(Constants.MSG_ACTION_CONNECT_TIMEOUT);
                    }
                    break;
                case Constants.MSG_ACTION_NEGATIVE_1:
                    Intent intent = new Intent(getActivity(),WifiService.class);
                    intent.setAction(Constants.CHECK_NETWROK_ACTION);
                    intent.putExtra("NA",Constants.MSG_ACTION_NEGATIVE_1);
                    getActivity().startService(intent);
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.item_add:
//                getActivity().startActivityForResult(new Intent(getActivity(),
//                        AddNetWorkActivity.class),111);
                break;
            case R.id.btn_getwifi_info:
//                getWiFiInfo();
                break;
            case R.id.wifi_test:
                startActivity(new Intent(getActivity(), SpeedTestActivity.class));
                break;
            case R.id.btn_wifi_switch:
                mWifiAdmin.openWifi();
                break;
            case R.id.btn_getwifi_cancel:
                mProgressView.setVisibility(View.INVISIBLE);
                mCrackButton.setVisibility(View.VISIBLE);
                break;
            case R.id.image_avatar:
                Intent intent = new Intent(getActivity(), ApDetailActivity.class);
                intent.putExtra("wi", "wi");
                startActivity(intent);
                break;
            case R.id.lay_guide1:
                handler.removeMessages(Constants.MSG_ACTION_GUIDE_ACTION1);
                guideAction1();
                break;
            case R.id.lay_guide2:
                handler.removeMessages(Constants.MSG_ACTION_GUIDE_ACTION2);
                guideAction2();
                break;

            default:
                break;
        }

    }



    public static final String ADD_WIFI = "action.add.new.network";
    public static final String UPDATE_WIFI_DESC = "action.update.network.desc";
    public static final String DISCONNECT_WIFI = "action.disconnect.current.network";
    public static final String CONNECT_WIFI_QRCODE = "action.connect.network.fromqrcode";

    private void registerDateTransReceiver(){
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction(ADD_WIFI);
        filter.addAction(UPDATE_WIFI_DESC);
        filter.addAction(DISCONNECT_WIFI);
        filter.addAction(CONNECT_WIFI_QRCODE);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.setPriority(1000);
        getActivity().registerReceiver(myReceiver,filter);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onClick(View v, int whichButton, String pwd, AccessPoint ap) {

        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
        switch (whichButton){
            case 0:
                sendTimeOutMsg();
                mConnectView.spwd = pwd;
                mConnectView.showConnectedView(ap);
                mWifiUtil.sfWifiConnect(ap.getBssid(), ap.getSsid(), pwd,
                        ap.getType());
                mListView.scrollTo(0, 0);
                mListView.setSelection(0);
                WiFiListFragment.ap = ap;
                break;
            case 1:
                if(ap == null){
                    return;
                }
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {

    }

    class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){

//                Log.v("----hagua----","**********onReceive  intent.getAction()="+intent.getAction()+"  WiFiUtil.isWifiAvailable()="+WiFiUtil.isWifiAvailable());

                boolean isOpen = mWifiAdmin.getWifiManager().isWifiEnabled();
                if(emptyView != null){
                    if(isOpen){
                        emptyView.setVisibility(View.GONE);
                        mListView.setVisibility(View.VISIBLE);
                        mConnectView.initOriginalWifi(false);
                        mWifiAdmin.startScan();
                        boolean bool = DmPreferenceManager.getInstance().getDisplayNewGuidePage();
                        if(bool && guideView1 != null){
                            guideView1.setVisibility(View.VISIBLE);
                            handler.sendEmptyMessageDelayed(Constants.MSG_ACTION_GUIDE_ACTION1,GUIDE_SHOW_TIME);
                        }
                    }else {
                        if(mListView.getRefreshState() == XListViewHeader.STATE_REFRESHING){
                            mListView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                            mConnectView.hideConnectedView();
                        }
                    }
                    if (!WiFiUtil.isWifiAvailable()) {
                        mConnectView.hideConnectedView();
                    }
                }
            }
            if(WifiManager.RSSI_CHANGED_ACTION.equals(intent.getAction())){
                mConnectView.notifySignalChanged();
            }

        }
    }

    @Override
    public void onScanResultChanged(List<ScanResult> results) {

        Map<String,List<AccessPoint>> datas = mWifiUtil.sortApInfo(results);
        handler.sendMessage(handler.obtainMessage(Constants.MSG_ACTION_FILL_APS_LIST,datas));

    }

    @Override
    public void onCheckStatus(int state) {

    }


    NetworkInfo.DetailedState cdState;
    String authSSID;
    String authBSSID;

    @Override
    public void onChangeState(Intent intent, NetworkInfo.DetailedState detailedState, int errorCode) {

        String action = intent.getAction();


    }

    @Override
    public void onRefresh() {

        new Thread(){
            @Override
            public void run() {

                try {
                    handler.sendEmptyMessageDelayed(Constants.MSG_ACTION_REFRESH_LIST,2000);
                    mWifiAdmin.startScan();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mWifiAdmin.stopScan();
                }
            }
        }.start();

    }

    @Override
    public void onLoadMore() {

    }

    int clickRefresh = 0;

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {

        ImageView icon = (ImageView) v.findViewById(R.id.image_avatar);
//        Log.v("----hagua----","Tag = "+icon.getTag() );
        if(icon.getTag() == null){
            return false;
        }
        final AccessPoint _ap = (AccessPoint) icon.getTag();
        if(_ap == null){
            return false;
        }
        if(TextUtils.isEmpty(_ap.getBssid())){
            if(clickRefresh == 0){
                clickRefresh++;
                mListView.startRefresh(true);
                return true;
            }else if(clickRefresh == 1){
                if(adapter.getGroupCount() == 1){
                    ToastUtil.show(getActivity(),
                            R.string.wifi_refresh_try_no_keys);
                }else {
                    ToastUtil.show(getActivity(),
                            R.string.wifi_refresh_try_has_keys);
                }
                clickRefresh = 0;
                return false;
            }
        }

        if(_ap.getLevelPercent() <= 50){

            final Dialog dlg = new Dialog(getActivity(),R.style.dm_alert_dialog);
            View view = View.inflate(getActivity(),R.layout.wifi_dialog,null);
            ((TextView) view.findViewById(R.id.title))
                    .setText(R.string.dialog_prompt_title);
            ((TextView) view.findViewById(R.id.content))
                    .setText(R.string.wifi_connect_weak_signal_prompt);

            view.findViewById(R.id.cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dlg.dismiss();
                        }
                    }
            );

            TextView ok = (TextView) view.findViewById(R.id.ok);
            ok.setText(R.string.dialog_action_confirm);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectWiFi(_ap);
                    dlg.dismiss();
                }
            });

            dlg.setContentView(view);
            dlg.show();

        }else {
            connectWiFi(_ap);
        }

        return true;
    }

    private void connectWiFi(AccessPoint ap){

        try {
            mWifiAdmin.acquireWifiLock();
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter.removeAp(ap);
        WiFiListFragment.ap = ap;

        if(ap.isLock() && ap.getType() != WiFiUtil.WIFI_PASS){

            EditText et = ActionPwdInputDailog.showSheet(getActivity(),ap,
                    WiFiListFragment.this,WiFiListFragment.this,false,false);
            Message msg = new Message();
            msg.what = Constants.MSG_ACTION_HIDE_IMM;
            msg.obj = et;
            handler.sendMessageDelayed(msg,100);
        }else {

        }
        mListView.scrollTo(0, 0);
        mListView.setSelection(0);
    }

}
