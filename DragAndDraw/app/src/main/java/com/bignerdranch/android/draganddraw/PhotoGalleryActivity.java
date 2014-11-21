package com.bignerdranch.android.draganddraw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;


public class PhotoGalleryActivity extends SingleFragmentActivity {
    private static final String TAG = PhotoGalleryActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }

}
