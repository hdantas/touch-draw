package com.bignerdranch.android.draganddraw;

import android.support.v4.app.Fragment;

/**
 * Created by hdantas.
 * Activity that instantiates the fragment responsible for the gallery of drawings.
 */

public class DrawingGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new DrawingGalleryFragment();
    }

}
