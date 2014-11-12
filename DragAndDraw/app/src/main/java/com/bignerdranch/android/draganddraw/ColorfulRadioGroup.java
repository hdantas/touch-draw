package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

/**
 * Created by nuno on 12/11/14.
 */
public class ColorfulRadioGroup extends RadioGroup {
    private final static String TAG = ColorfulRadioGroup.class.getSimpleName();

    public ColorfulRadioGroup(Context context) {
        super(context);
    }

    public ColorfulRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
    }

    public void setButtonsColor(int buttonsColor) {
        for (int j = 0; j < getChildCount(); j++) {
            View child = getChildAt(j);
            if (child instanceof ColorfulRadioButton) {
                ((ColorfulRadioButton) child).setButtonColor(buttonsColor);
            }
        }
    }

}
