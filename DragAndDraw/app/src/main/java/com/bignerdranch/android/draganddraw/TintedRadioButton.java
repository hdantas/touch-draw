package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;

/**
 * Created by nuno on 12/11/14.
 */
public class TintedRadioButton extends RadioButton {

//    private Drawable mButtonDrawable;
//    private int mButtonColor;

    private final static String TAG = TintedRadioButton.class.getSimpleName();

    public TintedRadioButton(Context context) {
        super(context);
        Log.d(TAG, "1");
    }

    public TintedRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs, com.android.internal.R.attr.radioButtonStyle);
        Log.d(TAG, "2");
    }

    public TintedRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        Log.d(TAG, "3");
    }

    public TintedRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Log.d(TAG, "4");
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

    }

    //    @Override
//    public void setButtonDrawable(Drawable d) {
//        super.setButtonDrawable(d);
//        if (mButtonDrawable != null && mButtonColor != 0) {
//            mButtonDrawable = d;
//            Log.d(TAG, "setButtonDrawable mButtonColor: " + mButtonColor);
//            mButtonDrawable.setColorFilter(mButtonColor, PorterDuff.Mode.SRC_ATOP);
//            refreshDrawableState();
//        }
//    }

}
