package net.henriquedantas.android.touchanddraw;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

/**
 * Created by hdantas.
 * Fragment for the Drawing's gallery.
 */

public class DrawingGalleryFragment extends Fragment implements
        AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    private static final String TAG = DrawingGalleryFragment.class.getSimpleName();

    private GridView mGridView;
    private int mGridViewNumColumns;

    private Toast mToast;
    private ArrayList<Drawing> mItems;
    private DrawingManager mDrawingManager;
    private DrawingGalleryAdapter mDrawingGalleryAdapter;

    private Callbacks mCallBacks;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onDrawingSelected(long drawingId);
        void onDrawingCreated(long drawingId);
        void onDrawingRemoved(long drawingId);
        void onDrawingRemoved(ArrayList<Long> drawingIds);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallBacks = (Callbacks) activity;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
    }

    @SuppressLint("ShowToast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
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

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewDrawing();
            }
        });

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarActionBar);
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
                            ArrayList<Long> drawingsToRemove = new ArrayList<>();
                            for (int i = mDrawingGalleryAdapter.getCount() - 1; i >= 0; i--) {
                                // with the toolbar the position of the gridView starts in NUM_COLUMNS instead of 0
                                if (mGridView.isItemChecked(i + mGridViewNumColumns)) {
                                    Drawing drawingToRemove = mDrawingGalleryAdapter.getItem(i);
                                    Log.d(TAG, "onActionItemClicked Delete item in position " + i +
                                            " with id " + drawingToRemove.getId());
                                    drawingsToRemove.add(drawingToRemove.getId());
                                    mDrawingGalleryAdapter.remove(drawingToRemove);
                                }
                            }
                            mode.finish();
                            updateItems();
                            mCallBacks.onDrawingRemoved(drawingsToRemove);
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

    public void updateUI() {
        updateItems();
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
                mCallBacks.onDrawingRemoved(drawing.getId());
                updateItems();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        updateItems();
        super.onResume();
    }

    private void createNewDrawing() {
        Drawing drawing = mDrawingManager.startNewDrawing();
        mCallBacks.onDrawingCreated(drawing.getId());

    }

    private void setupAdapter() {
        Log.d(TAG, "setupAdapter");

//        if (getActivity() == null || mGridView == null) {
//            Log.d(TAG, "setupAdapter mGridView == null");
//            return;
//        }

        if (mDrawingGalleryAdapter == null) {
            Log.d(TAG, "setupAdapter mDrawingGalleryAdapter!= null\tNumColumns: " + mGridViewNumColumns);
            if (mItems == null) {
                mItems = new ArrayList<>();
            }

            mDrawingGalleryAdapter = new DrawingGalleryAdapter(
                    getActivity(),
                    mDrawingManager,
                    R.layout.item_drawing_gallery,
                    R.id.drawing_item_textView,
                    mGridView);

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
        if (position >= 0) {
            Drawing drawing = mItems.get(position);
            Log.d(TAG, "onItemClick with drawing id " + drawing.getId());
            mCallBacks.onDrawingSelected(drawing.getId());
        }
    }
}
