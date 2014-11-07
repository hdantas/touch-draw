package com.bignerdranch.android.draganddraw;

import android.content.Intent;
import android.support.v4.app.Fragment;


public class DragAndDrawActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
//        return new Fragment();
        return new DragAndDrawFragment();
    }

}
