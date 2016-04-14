package com.haguapku.wificlient.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.haguapku.wificlient.WifiClientLib;

/**
 * Created by MarkYoung on 15/11/2.
 */
public class ToastUtil {

    private static Toast toast;
    public static void show(Context paramContext, int paramInt) {
        if(paramContext==null){
            return;
        }
        show(paramContext, paramContext.getString(paramInt));
    }
    public static void show(Context paramContext, CharSequence paramCharSequence) {
        if(paramContext==null) {
            return;
        }

        if(toast==null) {
            toast = Toast.makeText(paramContext, paramCharSequence, Toast.LENGTH_SHORT);
        } else {
            toast.setText(paramCharSequence);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
