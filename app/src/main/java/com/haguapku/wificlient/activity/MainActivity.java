package com.haguapku.wificlient.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.haguapku.wificlient.WifiClientLib;
import com.haguapku.wificlient.fragment.NewsCenterFragment;
import com.haguapku.wificlient.fragment.WiFiListFragment;
import com.haguapku.wificlient.util.DmPreferenceManager;

import com.haguapku.wificlient.util.WifiAdmin;
import com.haguapku.wificlient.util.WiFiUtil;
import com.haguapku.wificlient.view.ConnectView;
import com.haguapku.wificlient.view.DmSwitchBox;

import com.haguapku.wificlient.R;


public class MainActivity extends FragmentActivity implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener,CompoundButton.OnCheckedChangeListener,
        ConnectView.NotifyConnected{

    private ImageView mMapBtn;
    private ImageView mScanBtn;
    private ImageView mTipss,mTips;
    private RadioGroup mMainTabView;
    private DmSwitchBox mSwitchBox;
    private ImageView mOptionBtn;
    private PopupWindow mMenuPopup;
    private TextView mTitle;

    private TabInfo[] mTabInfo = new TabInfo[]{
            new TabInfo(R.id.main_tab_wifi, WiFiListFragment.class),
            new TabInfo(R.id.main_tab_news, NewsCenterFragment.class)
//            new TabInfo(R.id.main_tab_wifi,null),
//            new TabInfo(R.id.main_tab_news,null)
    };
    private int lastTabIndex = 0;
    private Fragment[] fragments = new Fragment[mTabInfo.length];
    private int tabIndex = -1;

    private WifiAdmin wifiAdmin;

    private long lastBackPressedTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        wifiAdmin = WifiAdmin.getInstance();
        initView();
    }

    private void initView(){
        mMapBtn = (ImageView)findViewById(R.id.wifi_map);
        mMapBtn.setOnClickListener(this);
        mScanBtn = (ImageView)findViewById(R.id.wifi_scan);
        mScanBtn.setOnClickListener(this);
        mOptionBtn = (ImageView)findViewById(R.id.bar_option);
        mOptionBtn.setOnClickListener(this);
        mTitle = (TextView)findViewById(R.id.wifi_title);
        mTips = (ImageView)findViewById(R.id.tips);
        mTipss = (ImageView)findViewById(R.id.tipss);
        mMainTabView = (RadioGroup)findViewById(R.id.maintab);
        mMainTabView.setOnCheckedChangeListener(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RadioButton) mMainTabView.getChildAt(0)).toggle();
            }
        });
        WiFiUtil.isAppRun = true;
    }

    @Override
    protected void onResume() {

        if(DmPreferenceManager.getInstance().getFeedNew()){
            mTipss.setVisibility(View.VISIBLE);
        }
        else{
            mTipss.setVisibility(View.GONE);
        }

        if(DmPreferenceManager.getInstance().getDisplayScan()){
            mTips.setVisibility(View.VISIBLE);
        }
        else{
            mTips.setVisibility(View.GONE);
        }

        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bar_option:
                showMoreMenu();
                break;
            case R.id.wifi_scan:
                break;
            case R.id.wifi_map:
                break;
            case R.id.menu_setting:
                hidePopAction();

                break;
            case R.id.menu_exit:
                hidePopAction();
//                EventUtil.sendEvent(EventUtil.EVENT_STOP);
                finish();
                System.gc();
                break;
            default:
                break;
        }
    }

    private void showMoreMenu(){
        int[] location = new int[2];
        View view = View.inflate(WifiClientLib.context,R.layout.more_menu_list,null);
        ImageView mTips = (ImageView)view.findViewById(R.id.tips);
        if(DmPreferenceManager.getInstance().getFeedNew()){
            mTips.setVisibility(View.VISIBLE);
        }
        else {
            mTips.setVisibility(View.GONE);
        }
        mMenuPopup = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mMenuPopup.setBackgroundDrawable(new ColorDrawable());
        mMenuPopup.setFocusable(true);
        mMenuPopup.setTouchable(true);
        mMenuPopup.setOutsideTouchable(true);
        View wifiView = view.findViewById(R.id.menu_wifi);
        View settingView = view.findViewById(R.id.menu_setting);
        View exitView  = view.findViewById(R.id.menu_exit);
        wifiView.setOnClickListener(this);
        settingView.setOnClickListener(this);
        exitView.setOnClickListener(this);
        mSwitchBox = (DmSwitchBox) view.findViewById(R.id.wifi_switch);
        mSwitchBox.setOnCheckedChangeListener(this);
        mSwitchBox.setChecked(wifiAdmin.getWifiManager().isWifiEnabled());
        mOptionBtn.getLocationInWindow(location);
        mMenuPopup.showAtLocation(mOptionBtn, Gravity.NO_GRAVITY,
                location[0],location[1]+mOptionBtn.getHeight());
    }

    private void hidePopAction(){
        if(mMenuPopup != null && mMenuPopup.isShowing()){
            mMenuPopup.dismiss();
        }
    }

    @Override
    protected void onDestroy() {

        WiFiUtil.isAppRun = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if(System.currentTimeMillis() - lastBackPressedTime < 2500){
//            EventUtil.sendEvent(EventUtil.EVENT_STOP);
            super.onBackPressed();
            System.gc();
        }else {
            Toast.makeText(this,R.string.wifi_main_quit_toast,Toast.LENGTH_LONG).show();
            lastBackPressedTime = System.currentTimeMillis();
        }


    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        int index = 0;
        for(int i=0;i<mTabInfo.length;i++){
            if(mTabInfo[i].buttonId == checkedId){
                index = i;
            }
        }
        if(index == tabIndex){
            return;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if(index > lastTabIndex){
            ft.setCustomAnimations(R.anim.fragment_slide_in_from_right,R.anim.fragment_slide_out_from_left);
        }else {
            ft.setCustomAnimations(R.anim.fragment_slide_in_from_left,R.anim.fragment_slide_out_from_right);
        }

        if(fragments[index] == null){
            try {
                fragments[index] = (Fragment)mTabInfo[index].mClass.newInstance();
                ft.add(R.id.container,fragments[index],"page_"+index);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }else {
            ft.show(fragments[index]);
        }

        if(tabIndex != -1 && fragments[tabIndex] != null){
            ft.hide(fragments[tabIndex]);
        }

        tabIndex = index;
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        hidePopAction();

        if(isChecked){
            wifiAdmin.openWifi();
        }
        else{
            wifiAdmin.closeWifi();
        }
    }

    @Override
    public void onConnected(int descId) {
        Toast.makeText(this, "已连接", Toast.LENGTH_SHORT).show();
//        mMainTabView.check(R.id.main_tab_news);
    }

    public static class TabInfo{
        private int buttonId;
        private Class<?> mClass;

        public TabInfo(int buttonId, Class<?> mClass) {
            this.buttonId = buttonId;
            this.mClass = mClass;
        }
    }

}
