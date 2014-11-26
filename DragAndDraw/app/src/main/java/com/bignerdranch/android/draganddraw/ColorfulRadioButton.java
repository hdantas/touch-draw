package com.bignerdranch.android.draganddraw;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;

/**
 * Created by hdantas on 12/11/14.
 * Custom radio button that can change color.
 */
public class ColorfulRadioButton extends RadioButton {

    private Drawable mButtonDrawable;
    private int mButtonColor;

    private final static String TAG = ColorfulRadioButton.class.getSimpleName();

    public ColorfulRadioButton(Context context) {
        super(context);
    }

    public ColorfulRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorfulRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ColorfulRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void setButtonDrawable(Drawable d) {
        super.setButtonDrawable(d);
        mButtonDrawable = d;
    }

    public void setButtonColor(int buttonColor) {
        if (buttonColor != mButtonColor) {
            mButtonColor = buttonColor;
            mButtonDrawable.setColorFilter(mButtonColor, PorterDuff.Mode.SRC_ATOP);
            Log.d(TAG, "setButtonColor mButtonColor: " + mButtonColor);
            invalidate();
        }
    }
}
