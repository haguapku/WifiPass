package com.haguapku.wificlient.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haguapku.wificlient.activity.R;

/**
 * Created by MarkYoung on 15/10/14.
 */
public class WifiSignalView extends LinearLayout {

    int grays[] = { R.drawable.wifi_common_wifi_0,R.drawable.wifi_common_wifi_1,
            R.drawable.wifi_common_wifi_2, R.drawable.wifi_common_wifi_3,
            R.drawable.wifi_common_wifi_4 };

    int greens[] = { R.drawable.wifi_common_wifi_0_,R.drawable.wifi_common_wifi_1_,
            R.drawable.wifi_common_wifi_2_, R.drawable.wifi_common_wifi_3_,
            R.drawable.wifi_common_wifi_4_ };

    int whites[] = { R.drawable.wifi_common_wifi_00,R.drawable.wifi_common_wifi_01,
            R.drawable.wifi_common_wifi_02, R.drawable.wifi_common_wifi_03,
            R.drawable.wifi_common_wifi_04 };

    public WifiSignalView(Context context) {
        super(context);
        init();
    }

    public WifiSignalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WifiSignalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        View.inflate(getContext(), R.layout.layout_wifi_signal, this);
    }

    public final void setValues(int icon, int signal, boolean isLock,
                                boolean isCrack,boolean isCurrent){

        ImageView lock = (ImageView) findViewById(R.id.lock);
        ImageView img = (ImageView) findViewById(R.id.signal_img);
        TextView txt = (TextView) findViewById(R.id.signal_text);

        if(isLock){
            lock.setImageResource(R.drawable.wifi_lock);
            lock.setVisibility(VISIBLE);
            img.setImageResource(grays[icon]);
            txt.setTextColor(Color.parseColor("#707070"));
        }
        else {
            lock.setVisibility(GONE);
            if(isCrack){
                lock.setVisibility(VISIBLE);
                lock.setImageResource(R.drawable.wifi_key);
            }else{
                lock.setImageResource(R.drawable.wifi_lock);
            }
            img.setImageResource(greens[icon]);
            txt.setTextColor(Color.parseColor("#00dde5"));
        }
        if(isCurrent){
            img.setImageResource(whites[icon]);
            txt.setTextColor(Color.parseColor("#ffffff"));
        }
        txt.setText(signal+"%");
    }
}
