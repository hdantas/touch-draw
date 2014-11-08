package com.bignerdranch.android.draganddraw;

import android.support.v4.app.Fragment;
import android.view.KeyEvent;


public class DragAndDrawActivity extends SingleFragmentActivity {

    DragAndDrawFragment mDragAndDrawFragment;

    @Override
    protected Fragment createFragment() {
        mDragAndDrawFragment = new DragAndDrawFragment();
        return mDragAndDrawFragment;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mDragAndDrawFragment != null) {
            mDragAndDrawFragment.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
