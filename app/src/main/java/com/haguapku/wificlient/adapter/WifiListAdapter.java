package com.haguapku.wificlient.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.haguapku.wificlient.activity.R;
import com.haguapku.wificlient.bean.AccessPoint;
import com.haguapku.wificlient.util.WiFiUtil;
import com.haguapku.wificlient.view.WifiSignalView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by MarkYoung on 15/10/14.
 */
public class WifiListAdapter extends BaseExpandableListAdapter implements View.OnClickListener{

    private Map<String,List<AccessPoint>> datas;
    private LayoutInflater inflater;
    private Context context;

    private static String[] keys = {"free","key"};
    private static int[] types = {R.string.wifi_type_free,R.string.wifi_type_key};
    private static int[] icons = {R.drawable.wifi_common_unlock,R.drawable.wifi_common_lock};

    public WifiListAdapter(Map<String, List<AccessPoint>> datas, Context context) {
        super();
        this.datas = datas;
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        if(datas == null){
            this.datas = new HashMap<>();
            for(int i=0;i<keys.length;i++){
                this.datas.put(keys[i],new ArrayList<AccessPoint>());
            }
        }
    }

    private List<AccessPoint> getKeyDatas(){
        if(datas == null)
            return null;
        return datas.get("key");
    }

    @Override
    public int getGroupCount() {
        return getKeyDatas().size() == 0 ? 1 : 2;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int size = datas.get(keys[groupPosition]).size();
        if(groupPosition == 0 && size == 0)
            size = 1;
        return size;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return datas.get(keys[groupPosition]);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return datas.get(keys[groupPosition]).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_group,null,false);
        }
        ImageView icon = (ImageView)convertView.findViewById(R.id.icon);
        TextView text = (TextView)convertView.findViewById(R.id.text);
        icon.setImageResource(icons[groupPosition]);
        text.setText(types[groupPosition]);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = View.inflate(context,R.layout.item_ssid,null);
            holder = new ViewHolder();
            holder.wifiName = (TextView) convertView
                    .findViewById(R.id.text_wifi_name);
            holder.luyou = (TextView) convertView
                    .findViewById(R.id.text_luyou_name);
            holder.desc = (TextView) convertView
                    .findViewById(R.id.text_wifi_desc);
            holder.signalView = (WifiSignalView) convertView
                    .findViewById(R.id.signalView);
            holder.icon = (ImageView) convertView
                    .findViewById(R.id.image_avatar);
            holder.hline = convertView.findViewById(R.id.hline);
            holder.icon.setOnClickListener(this);
            convertView.setTag(holder);
        }

        holder = (ViewHolder)convertView.getTag();
        AccessPoint ap;
        if(groupPosition == 0 && datas.get(keys[groupPosition]).size() == 0){
            ap = addDefult();
        }else{
            ap = (AccessPoint)getChild(groupPosition,childPosition);
        }

        holder.signalView.setVisibility(View.VISIBLE);
        holder.desc.setVisibility(View.VISIBLE);
        holder.hline.setVisibility(View.VISIBLE);
        holder.wifiName.setText(ap.getDisplayName());

        holder.luyou.setVisibility(View.VISIBLE);
        if(TextUtils.isEmpty(ap.getLuyouType())){
            holder.luyou.setVisibility(View.GONE);
        }else {
            holder.luyou.setText(ap.getLuyouType());
        }

        holder.icon.setBackgroundResource(ap.getIcon());
        holder.icon.setTag(ap);

        holder.signalView.setValues(ap.getSignalIcon(),ap.getLevelPercent(),ap.isLock(),ap.isCrack(),false);
        if (TextUtils.isEmpty(ap.getBssid())) {
            holder.signalView.setVisibility(View.GONE);
        }
        holder.desc.setText(ap.getDesc());
        if (groupPosition == 1 || TextUtils.isEmpty(ap.getBssid())) {
            holder.desc.setVisibility(View.GONE);
        }

        if((groupPosition==0&&childPosition==getChildrenCount(groupPosition)-1)){
            holder.hline.setVisibility(View.GONE);
        }
        if(getGroupCount()==1&&getChildrenCount(groupPosition)==1){
            holder.hline.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.image_avatar:
//                AccessPoint ap = (AccessPoint)v.getTag();
//                if(ap == null || TextUtils.isEmpty(ap.getBssid())){
//                    return;
//                }
//                Intent intent = new Intent(context,ApDetailActivity.class);
//                intent.putExtra("ap",ap);
//                context.startActivity(intent);
                break;
            default:
                break;
        }
    }

    public void setList(Map<String,List<AccessPoint>> datas){
        synchronized (this.datas) {
            for (String key : keys) {
                List<AccessPoint> data = datas.get(key);
                if (data != null) {
                    this.datas.put(key, data);
                } else {
                    this.datas.clear();
                }
            }
        }
        notifyDataSetChanged();
    }

    public  AccessPoint addDefult(){
        AccessPoint ap = new AccessPoint();
        ap.setBssid("");
        ap.setSsid(context.getResources().getString(R.string.wifi_free_empty));
        ap.setCapabilities("");
        ap.setType(0);
        ap.setFrequency(0);
        ap.setDescribeContents(0);
        ap.setLevel(-55);
        ap.setIcon(R.drawable.wifi_default_logo);
        ap.setLevelPercent(WiFiUtil.signalPercent(-55, 100, 1));
        ap.setSignalIcon(2);
        ap.setLuyouType("");
        ap.setIcon(R.drawable.free_icon);
        ap.setLock(false);
        return ap;
    }

    private static class ViewHolder {
        TextView wifiName;
        TextView luyou;
        TextView desc;
        WifiSignalView signalView;
        ImageView icon;
        View hline;
    }
}
