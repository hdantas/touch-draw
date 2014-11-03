package com.bignerdranch.android.draganddraw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

/**
 * Created by nuno on 10/8/14.
 */
public abstract class SingleFragmentActivity extends ActionBarActivity {

    private static String TAG = SingleFragmentActivity.class.getSimpleName();

    protected abstract Fragment createFragment();

    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }

        showOverflowMenu();
    }

    private void showOverflowMenu() {
        /* Hack to show overflow menu in ALL devices
         * src: http://stackoverflow.com/questions/9286822/how-to-force-use-of-overflow-menu-on-devices-with-menu-button
         */
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error changing config options", e);
        }

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Intercept HW menu button clicks to prevent showing duplicate menus
        return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
    }
}
