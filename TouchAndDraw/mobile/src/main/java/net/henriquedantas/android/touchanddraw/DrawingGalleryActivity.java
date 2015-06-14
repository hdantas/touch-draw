package net.henriquedantas.android.touchanddraw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by hdantas.
 * Activity that instantiates the fragment responsible for the gallery of drawings.
 */

public class DrawingGalleryActivity extends SingleFragmentActivity
        implements DrawingGalleryFragment.Callbacks, EditorFragment.Callbacks {

    private static final String TAG = DrawingGalleryActivity.class.getSimpleName();
    private long mVisibleDrawingId = -1;
    private EditorFragment mVisibleEditorFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarActionBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (findViewById(R.id.detailFragmentContainer) != null) {
            // Dual pane view -> show empty detailFragmentContainer

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
            Fragment newDetail = EditorFragment.newInstance();
            mVisibleEditorFragment = (EditorFragment) newDetail;

            if (oldDetail != null) {
                ft.remove(oldDetail);
            }

            ft.add(R.id.detailFragmentContainer, newDetail);
            ft.commit();
        }
    }

    @Override
    protected Fragment createFragment() {
        return new DrawingGalleryFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_master_detail;
    }

    @Override
    public void onDrawingSelected(long drawingId) {
        if (findViewById(R.id.detailFragmentContainer) == null) {
            // Single pane view -> Start an instance of EditorActivity
            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra(EditorActivity.EXTRA_DRAWING_ID, drawingId);
            startActivity(intent);

        } else {
            // Dual pane view -> Update detailFragmentContainer
            mVisibleDrawingId = drawingId;
            Log.d(TAG, "onDrawingSelected\n" +
                    "\tdrawingId: " + drawingId);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
            Fragment newDetail = EditorFragment.newInstance(drawingId);
            mVisibleEditorFragment = (EditorFragment) newDetail;

            if (oldDetail != null) {
                ft.remove(oldDetail);
            }

            ft.add(R.id.detailFragmentContainer, newDetail);
            ft.commit();
        }
    }

    @Override
    public void onDrawingCreated(long drawingId) {
        onDrawingSelected(drawingId);
        if (findViewById(R.id.detailFragmentContainer) != null) {
            onDrawingUpdated(drawingId);

            findViewById(R.id.editorLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.editorLayoutPlaceholder).setVisibility(View.INVISIBLE);
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onDrawingRemoved(long drawingId) {
        Log.d(TAG, "onDrawingRemoved\n" +
                "\tdrawingId: " + drawingId +
                "\tmVisibleDrawingId" + mVisibleDrawingId);
        if (drawingId == mVisibleDrawingId) {
            mVisibleDrawingId = -1;
            findViewById(R.id.editorLayoutPlaceholder).setVisibility(View.VISIBLE);
            findViewById(R.id.editorLayout).setVisibility(View.INVISIBLE);
            mVisibleEditorFragment.deleteDrawing();
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onDrawingRemoved(ArrayList<Long> drawingIds) {
        if(drawingIds.contains(mVisibleDrawingId)) {
            onDrawingRemoved(mVisibleDrawingId);
        }
    }

    // Only called with two-panes
    @Override
    public void onDrawingUpdated(long drawingId) {
        FragmentManager fm = getSupportFragmentManager();
        DrawingGalleryFragment galleryFragment = (DrawingGalleryFragment)
                fm.findFragmentById(R.id.fragmentContainer);
        galleryFragment.updateUI();
    }

    // Only called with two-panes
    @Override
    public void onDrawingFinished() {
        FragmentManager fm = getSupportFragmentManager();
        DrawingGalleryFragment galleryFragment = (DrawingGalleryFragment)
                fm.findFragmentById(R.id.fragmentContainer);
        galleryFragment.updateUI();

        findViewById(R.id.editorLayoutPlaceholder).setVisibility(View.VISIBLE);
        findViewById(R.id.editorLayout).setVisibility(View.INVISIBLE);
        invalidateOptionsMenu();
    }
}
