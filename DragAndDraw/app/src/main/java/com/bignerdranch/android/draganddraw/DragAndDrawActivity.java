package com.bignerdranch.android.draganddraw;

import android.support.v4.app.Fragment;


public class DragAndDrawActivity extends SingleFragmentActivity {

    DragAndDrawFragment mDragAndDrawFragment;

    @Override
    protected Fragment createFragment() {
        mDragAndDrawFragment = new DragAndDrawFragment();
        return mDragAndDrawFragment;
    }
}
