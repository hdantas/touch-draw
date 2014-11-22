package com.bignerdranch.android.draganddraw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;


public class DrawingGalleryActivity extends SingleFragmentActivity {
    private static final String TAG = DrawingGalleryActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        return new DrawingGalleryFragment();
    }

}
