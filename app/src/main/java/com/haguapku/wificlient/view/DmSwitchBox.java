package com.haguapku.wificlient.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;

import com.haguapku.wificlient.activity.R;

/**
 * Created by MarkYoung on 15/9/17.
 */
public class DmSwitchBox extends CheckBox implements View.OnTouchListener{

    private static Bitmap switch_off_Bg,switch_on_Bg,slip_Btn;
    private Rect on_Rect,off_Rect;
    private boolean isSlipping = false;
    private float previousX,currentX;
    private long downTime = 0;

    public DmSwitchBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DmSwitchBox(Context context) {
        super(context);
        init();
    }

    private void init(){
        setOnTouchListener(this);
        if(switch_off_Bg == null){
            switch_off_Bg = BitmapFactory.decodeResource(getResources(),
                    R.drawable.zapya_drawer_setting_switch_off);
            switch_on_Bg = BitmapFactory.decodeResource(getResources(),
                    R.drawable.zapya_drawer_setting_switch_on);
            slip_Btn = BitmapFactory.decodeResource(getResources(),
                    R.drawable.zapya_drawer_setting_switch_handle);
        }
        on_Rect = new Rect(switch_off_Bg.getWidth()-slip_Btn.getWidth(),0,
                switch_off_Bg.getWidth(),switch_off_Bg.getHeight());
        off_Rect = new Rect(0,0,slip_Btn.getWidth(),slip_Btn.getHeight());
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                currentX = event.getX();
                break;
            case MotionEvent.ACTION_DOWN:
                if(event.getX()>switch_on_Bg.getWidth()||
                        event.getY()>switch_on_Bg.getHeight()){
                    return false;
                }
                isSlipping = true;
                previousX = event.getX();
                currentX = previousX;
                downTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                currentX = event.getX();
            case MotionEvent.ACTION_CANCEL:
                isSlipping = false;
                if(System.currentTimeMillis() - downTime > 500){
                    if(currentX >= (switch_on_Bg.getWidth()/2)){
                        setChecked(true);
                    }
                    else{
                        setChecked(false);
                    }
                }
                else{
                    toggle();
                }
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Matrix matrix = new Matrix();
        Paint paint = new Paint();

        float left_SlipBtn;

        if(isSlipping){
            if(currentX > switch_on_Bg.getWidth()){
                left_SlipBtn = switch_on_Bg.getWidth() - slip_Btn.getWidth();
            }
            else{
                left_SlipBtn = currentX - slip_Btn.getWidth()/2;
            }

            if(currentX < switch_on_Bg.getWidth()/2){
                canvas.drawBitmap(switch_off_Bg,matrix,paint);
            }
            else{
                canvas.drawBitmap(switch_on_Bg,matrix,paint);
            }
        }
        else{
            if(isChecked()){
                left_SlipBtn = on_Rect.left;
            }
            else{
                left_SlipBtn = off_Rect.left;
            }

            if(!isChecked()){
                canvas.drawBitmap(switch_off_Bg,matrix,paint);
            }
            else{
                canvas.drawBitmap(switch_on_Bg,matrix,paint);
            }
        }

        if(left_SlipBtn < 0){
            left_SlipBtn = 0;
        }
        else if(left_SlipBtn > switch_on_Bg.getWidth() - slip_Btn.getWidth()){
            left_SlipBtn = switch_on_Bg.getWidth() - slip_Btn.getWidth();
        }

        canvas.drawBitmap(slip_Btn,left_SlipBtn,0,paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if(switch_on_Bg!=null){
            setMeasuredDimension(switch_on_Bg.getWidth(),switch_on_Bg.getHeight());
        }

    }
}
