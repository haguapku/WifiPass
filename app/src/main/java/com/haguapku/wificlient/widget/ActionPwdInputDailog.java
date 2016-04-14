package com.haguapku.wificlient.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.haguapku.wificlient.R;
import com.haguapku.wificlient.bean.AccessPoint;
import com.haguapku.wificlient.util.WiFiUtil;

import static android.content.DialogInterface.*;
import static android.widget.CompoundButton.*;

/**
 * Created by MarkYoung on 15/11/9.
 */
public class ActionPwdInputDailog {

    public interface OnActionSheetSelected{

        void onClick(View v, int whichButton, String pwd,AccessPoint ap);
    }

    public ActionPwdInputDailog() {
    }

    static boolean bool = false;

    public static boolean isShow= false;
    static int  pwdCount = 8;

    public static EditText showSheet(Context context, final AccessPoint ap,
                                     final OnActionSheetSelected actionSheetSelected,
                                     OnCancelListener cancelListener,
                                     boolean isWorng, boolean isCrack){

        isShow = true;
        pwdCount = 8;
        if (ap.getType()== WiFiUtil.WIFI_WEP) {
            pwdCount = 5;
        }

        final Dialog dlg = new Dialog(context, R.style.dm_alert_dialog);
        View view = View.inflate(context,R.layout.wifi_pwd_dialog,null);
        TextView mContent = (TextView) view.findViewById(R.id.wifi_ssid);
        final EditText pwd = (EditText) view.findViewById(R.id.et_pwd);
        CheckBox eye = (CheckBox) view.findViewById(R.id.ctv_checktext);
        TextView cancel = (TextView) view.findViewById(R.id.btn_cancel);
        TextView hint = (TextView) view.findViewById(R.id.wifi_hint);
        final TextView connect = (TextView) view.findViewById(R.id.btn_connect);
        if(isWorng || isCrack){
            hint.setVisibility(View.VISIBLE);
        }else {
            hint.setVisibility(View.GONE);
        }
        if(isCrack){
            hint.setText(R.string.wifi_crack_wrong_pwd_prompt);
        }
        pwd.setFocusable(true);
        pwd.setFocusableInTouchMode(true);
        pwd.requestFocus();
        mContent.setText(ap.getSsid());

        pwd.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pwd.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        eye.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    pwd.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    pwd.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                Editable etable = pwd.getText();
                pwd.setSelection(etable.length());
            }
        });
        connect.setEnabled(false);
        connect.setClickable(false);
        pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() >= pwdCount) {

                    bool = true;
                    connect.setEnabled(true);
                    connect.setClickable(true);
                    connect.setTextColor(Color.parseColor("#00dde5"));
                } else {
                    bool = false;
                    connect.setTextColor(Color.parseColor("#3f00dde5"));
                    connect.setEnabled(false);
                    connect.setClickable(false);
                }
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSheetSelected.onClick(v, 0, pwd.getText().toString(), ap);
                dlg.dismiss();
                bool = false;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSheetSelected.onClick(v, 1, null, ap);
                dlg.dismiss();
            }
        });

        dlg.setCanceledOnTouchOutside(false);

        if(cancelListener != null){
            dlg.setOnCancelListener(cancelListener);
        }

        dlg.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isShow = false;
            }
        });

        dlg.setContentView(view);
        dlg.show();

        return pwd;
    }
}
