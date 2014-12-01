package net.henriquedantas.android.touchanddraw;

import android.support.v4.app.Fragment;
import android.view.KeyEvent;

/**
 * Created by hdantas.
 * Activity that instantiates the fragment responsible for the Drawing's editor.
 */

public class EditorActivity extends SingleFragmentActivity {

    private EditorFragment mTouchAndDrawFragment;

    @Override
    protected Fragment createFragment() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTouchAndDrawFragment = new EditorFragment();
        return mTouchAndDrawFragment;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mTouchAndDrawFragment != null) {
            mTouchAndDrawFragment.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
