package com.haguapku.wificlient.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.haguapku.wificlient.adapter.WifiListAdapter;
import com.haguapku.wificlient.bean.AccessPoint;
import com.haguapku.wificlient.util.WifiAdmin;
import com.haguapku.wificlient.util.WiFiUtil;
import com.haguapku.wificlient.widget.XExpandableListView;

/**
 * Created by MarkYoung on 15/10/14.
 */
public class WiFiListFragment extends Fragment implements View.OnClickListener,
        XExpandableListView.IXListViewListener,ExpandableListView.OnChildClickListener{

    private XExpandableListView mListView;
    private View mCrackButton;
    private View mProgressView;
    private Button mCancelButton;
    private Button mWifiSwitchButton;
    private MyReceiver myReceive;
    private WiFiUtil mWifiUtil;
    InputMethodManager imm;
    private View emptyView;
    private View guideView1;
    private View guideView2;
//    private WiFiPwdServer wps;
    public static AccessPoint ap;
    private WifiAdmin mWifiAdmin;
    private WifiListAdapter adapter;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        return false;
    }
}
