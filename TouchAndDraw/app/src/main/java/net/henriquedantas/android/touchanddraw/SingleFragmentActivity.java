package net.henriquedantas.android.touchanddraw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by hdantas on 10/8/14.
 * Controller class that defines an activity
 * composed of a single fragment.
 * This skeleton class is extended by all activities.
 */
public abstract class SingleFragmentActivity extends ActionBarActivity {

    private static final String TAG = SingleFragmentActivity.class.getSimpleName();

    protected abstract Fragment createFragment();

    int getLayoutResId() {
        return R.layout.activity_single_fragment;
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
    }
}
