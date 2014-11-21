package com.bignerdranch.android.draganddraw;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import com.android.photos.views.HeaderGridView;

/**
 * Created by nuno on 20/11/14.
 */
public class HeaderGridViewCompat extends HeaderGridView {

    public HeaderGridViewCompat(Context context) {
        super(context);
    }

    public HeaderGridViewCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderGridViewCompat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int getNumColumns() {
        return getNumColumnsCompat();
    }


    private int getNumColumnsCompat() {
        if (Build.VERSION.SDK_INT >= 11) {
            return getNumColumnsCompat11();

        } else {
            int columns = 0;
            int children = getChildCount();
            if (children > 0) {
                int width = getChildAt(0).getMeasuredWidth();
                if (width > 0) {
                    columns = getWidth() / width;
                }
            }
            return columns > 0 ? columns : AUTO_FIT;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private int getNumColumnsCompat11() {
        return super.getNumColumns();
    }

}
