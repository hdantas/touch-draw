package com.bignerdranch.android.draganddraw;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.felipecsl.quickreturn.library.AbsListViewQuickReturnAttacher;
import com.felipecsl.quickreturn.library.QuickReturnAttacher;
import com.felipecsl.quickreturn.library.widget.AbsListViewScrollTarget;
import com.felipecsl.quickreturn.library.widget.QuickReturnAdapter;

import java.util.ArrayList;

/**
 * Created by hdantas.
 * Fragment for the Drawing's gallery.
 */

public class DrawingGalleryFragment extends Fragment implements
        AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    private static final String TAG = DrawingGalleryFragment.class.getSimpleName();
    private static final int REQUEST_CHANGE = 0;

    private GridView mGridView;
    private int mGridViewNumColumns;

    private Toast mToast;
    private ArrayList<Drawing> mItems;
    private DrawingManager mDrawingManager;
    private DrawingGalleryAdapter mDrawingGalleryAdapter;

    @SuppressLint("ShowToast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        setHasOptionsMenu(true);
        mDrawingManager = DrawingManager.get(getActivity());
        updateItems();
    }

    void updateItems() {
        Log.d(TAG, "updateItems");
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_drawing_grid, container, false);

        mGridView = (GridView) view.findViewById(R.id.grid_view);
        mGridViewNumColumns = getResources().getInteger(R.integer.num_columns);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_action_bar);
        ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);
        setupAdapter();

        final TypedArray styledAttributes = getActivity().getTheme().
                obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        final QuickReturnAttacher quickReturnAttacher = QuickReturnAttacher.forView(mGridView);
        Log.d(TAG, "OnCreateView toolbar.getHeight(): " + toolbarHeight);
        quickReturnAttacher.addTargetView(
                toolbar,
                AbsListViewScrollTarget.POSITION_TOP,
                toolbarHeight
        );

        if (quickReturnAttacher instanceof AbsListViewQuickReturnAttacher) {
            // This is the correct way to register an OnScrollListener.
            // You have to add it on the QuickReturnAttacher, instead
            // of on the viewGroup directly.
            final AbsListViewQuickReturnAttacher attacher = (AbsListViewQuickReturnAttacher) quickReturnAttacher;
            attacher.addOnScrollListener(DrawingGalleryFragment.this);
            attacher.setOnItemClickListener(DrawingGalleryFragment.this);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Use floating context menus on Froyo and Gingerbread
            registerForContextMenu(mGridView);
        } else {
            // Use contextual action bar on Honeycomb and higher
            mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
            mGridView.setMultiChoiceModeListener(new GridView.MultiChoiceModeListener() {

                @Override
                @TargetApi(11)
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.context_menu_drawing_gallery, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                @TargetApi(11)
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_item_delete_drawing:
                            for (int i = mDrawingGalleryAdapter.getCount() - 1; i >= 0; i--) {
                                // with the toolbar the position of the gridView starts in NUM_COLUMNS instead of 0
                                if (mGridView.isItemChecked(i + mGridViewNumColumns)) {
                                    Log.d(TAG, "onActionItemClicked Delete item in position " + i +
                                            " with id " + mDrawingGalleryAdapter.getItem(i).getId());
                                    mDrawingGalleryAdapter.remove(mDrawingGalleryAdapter.getItem(i));
                                }
                            }
                            mode.finish();
                            updateItems();
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    mDrawingGalleryAdapter.notifyDataSetChanged();
                }


                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });
        }
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.context_menu_drawing_gallery, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        // with the toolbar the position of the gridView starts in NUM_COLUMNS instead of 0
        Drawing drawing = mDrawingGalleryAdapter.getItem(position - mGridViewNumColumns);

        switch (item.getItemId()) {
            case R.id.menu_item_delete_drawing:
                mDrawingGalleryAdapter.remove(drawing);
                updateItems();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode " + requestCode + " resultCode " + resultCode);
        updateItems();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actions_drawing_gallery, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_drawing:
                Intent intent = new Intent(getActivity(), EditorActivity.class);
                intent.putExtra(EditorFragment.EXTRA_DRAWING_ID, -1L);
                startActivityForResult(intent, REQUEST_CHANGE);

                mToast.setText("Started new activity");
                mToast.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupAdapter() {
        Log.d(TAG, "setupAdapter");
        if (getActivity() == null || mGridView == null) {
            Log.d(TAG, "setupAdapter mGridView == null");
            return;
        }

        if (mDrawingGalleryAdapter == null) {
            Log.d(TAG, "setupAdapter mDrawingGalleryAdapter!= null\tNumColumns: " + mGridViewNumColumns);
            if (mItems == null) {
                mItems = new ArrayList<>();
                mItems.add(new Drawing());
            }
            mDrawingGalleryAdapter = new DrawingGalleryAdapter(
                    getActivity(),
                    mDrawingManager,
                    R.layout.item_drawing_gallery,
                    R.id.drawing_item_textView,
                    mGridView,
                    mGridViewNumColumns);

            mGridView.setAdapter(new QuickReturnAdapter(mDrawingGalleryAdapter, mGridViewNumColumns));
//            adapter = new ArrayAdapter<Drawing>(getActivity(), R.layout.item_drawing_gallery, R.id.drawing_item_textView);
//            mGridView.setAdapter(new QuickReturnAdapter(adapter, 1));
        } else if (mItems != null) {
            Log.d(TAG, "setupAdapter else");
            mDrawingGalleryAdapter.clear();
            for (Drawing drawing : mItems) {
                mDrawingGalleryAdapter.add(drawing);
//                adapter.add(drawing);
            }
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<Drawing>> {
        @Override
        protected ArrayList<Drawing> doInBackground(Void... voids) {
            Activity activity = getActivity();
            if (activity == null) {
                return new ArrayList<>();
            }

            ArrayList<Drawing> drawingArrayList = mDrawingManager.getAllDrawings();
            Log.d(TAG, "doInBackground, retrieved " + drawingArrayList.size() + " drawings.");

            return drawingArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Drawing> galleryItems) {
            mItems = galleryItems;
            setupAdapter();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    // On click start EditorFragment to edit it
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mToast.setText("Item " + position + " clicked");
        mToast.show();

        if (position >= 0) {
            Drawing item = mItems.get(position);
            Log.d(TAG, "onItemClick with drawing id " + item.getId());

            Intent intent = new Intent(getActivity(), EditorActivity.class);
            intent.putExtra(EditorFragment.EXTRA_DRAWING_ID, item.getId());
            startActivityForResult(intent, REQUEST_CHANGE);
        }

    }

}
