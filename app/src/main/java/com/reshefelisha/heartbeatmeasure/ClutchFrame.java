package com.reshefelisha.heartbeatmeasure;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Reshef on 11/1/2015.
 */
public class ClutchFrame extends FrameLayout {
    public ClutchFrame(Context context) {
        super(context);
    }

    public ClutchFrame(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ClutchFrame(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    public ClutchFrame(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onDraw(Canvas c){
        try{super.onDraw(c);}
        catch(Exception e){e.printStackTrace();}
    }
}
