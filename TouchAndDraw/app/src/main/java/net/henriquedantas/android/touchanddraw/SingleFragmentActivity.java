package net.henriquedantas.android.touchanddraw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

/**
 * Created by hdantas on 10/8/14.
 * Controller class that defines an activity
 * composed of a single fragment.
 * This skeleton class is extended by all activities.
 */
public abstract class SingleFragmentActivity extends ActionBarActivity {

    private static final String TAG = SingleFragmentActivity.class.getSimpleName();

    protected abstract Fragment createFragment();

    protected int getLayoutResId() {
        return R.layout.activity_single_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        showOverflowMenu();
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }

    /**
     * Hack to show overflow menu in ALL devices
     * src: https://stackoverflow.com/questions/15492791/how-do-i-show-overflow-menu-items-to-action-bar-in-android
     **/
    private void showOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Log.e(TAG, "showOverflowMenu", e);
        }
    }
}
