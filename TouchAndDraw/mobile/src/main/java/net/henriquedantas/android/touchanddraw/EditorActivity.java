package net.henriquedantas.android.touchanddraw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by hdantas.
 * Activity that instantiates the fragment responsible for the Drawing's editor.
 */

public class EditorActivity extends SingleFragmentActivity
        implements EditorFragment.Callbacks {
    private static final String TAG = EditorActivity.class.getSimpleName();

    public static final String EXTRA_DRAWING_ID =
            "net.henriquedantas.android.touchanddraw.extra_drawing_id";

    @Override
    public void onDrawingUpdated(long drawingId) {
    }

    @Override
    public void onDrawingFinished() {
        Intent intent = new Intent(this, DrawingGalleryActivity.class);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private EditorFragment mTouchAndDrawFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarActionBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Log.d(TAG, "onCreate mDrawingId");
    }

    @Override
    protected Fragment createFragment() {
        long drawingId = getIntent().getExtras().getLong(EXTRA_DRAWING_ID, -1L);
        Log.d(TAG, "createFragment drawingId " + drawingId);
        mTouchAndDrawFragment = EditorFragment.newInstance(drawingId);
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
