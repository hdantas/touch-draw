package com.bignerdranch.android.draganddraw;

import android.support.v4.app.Fragment;


public class PhotoGalleryActivity extends SingleFragmentActivity {
    private static final String TAG = PhotoGalleryActivity.class.getSimpleName();

    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }

}
