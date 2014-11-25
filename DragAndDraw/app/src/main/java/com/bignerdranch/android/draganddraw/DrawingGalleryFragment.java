package com.bignerdranch.android.draganddraw;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.felipecsl.quickreturn.library.AbsListViewQuickReturnAttacher;
import com.felipecsl.quickreturn.library.QuickReturnAttacher;
import com.felipecsl.quickreturn.library.widget.AbsListViewScrollTarget;
import com.felipecsl.quickreturn.library.widget.QuickReturnAdapter;
import com.felipecsl.quickreturn.library.widget.QuickReturnTargetView;

import java.util.ArrayList;

public class DrawingGalleryFragment extends Fragment implements
        AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    private static final String TAG = DrawingGalleryFragment.class.getSimpleName();
    private static final int REQUEST_CHANGE = 0;

    private ListView mListView;
    private Toolbar mToolbar;
    private QuickReturnTargetView topTargetView;

    private Toast mToast;
    ArrayList<Drawing> mItems;
    DrawingManager mDrawingManager;
    DrawingAdapter mDrawingAdapter;
    ArrayAdapter<Drawing> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mDrawingManager = DrawingManager.get(getActivity());
        updateItems();
    }

    public void updateItems() {
        Log.d(TAG, "updateItems");
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_drawing_gallery, container, false);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar_action_bar);
        ((ActionBarActivity) getActivity()).setSupportActionBar(mToolbar);

        ViewTreeObserver vto = mToolbar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "OnCreateView mToolbar.getHeight(): " + mToolbar.getHeight());
                final QuickReturnAttacher quickReturnAttacher = QuickReturnAttacher.forView(mListView);
                topTargetView = quickReturnAttacher.addTargetView(
                        mToolbar,
                        AbsListViewScrollTarget.POSITION_TOP,
                        mToolbar.getHeight()
                );

                if (quickReturnAttacher instanceof AbsListViewQuickReturnAttacher) {
                    // This is the correct way to register an OnScrollListener.
                    // You have to add it on the QuickReturnAttacher, instead
                    // of on the viewGroup directly.
                    final AbsListViewQuickReturnAttacher attacher = (AbsListViewQuickReturnAttacher) quickReturnAttacher;
                    attacher.addOnScrollListener(DrawingGalleryFragment.this);
                    attacher.setOnItemClickListener(DrawingGalleryFragment.this);
                }

                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mToolbar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mToolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });


        mListView = (ListView) view.findViewById(R.id.list_view);
        setupAdapter();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Use floating context menus on Froyo and Gingerbread
            registerForContextMenu(mListView);
        } else {
            // Use contextual action bar on Honeycomb and higher
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {

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
                            for (int i = mDrawingAdapter.getCount(); i >= 0; i--) {
                                // +1 since item i = 0 of the list view refers to the header
                                if (mListView.isItemChecked(i + 1)) {
                                    Log.d(TAG, " onActionItemClicked Delete item in position " + i + " with id " + mDrawingAdapter.getItem(i).getId());
                                    mDrawingAdapter.remove(mDrawingAdapter.getItem(i));
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
                    mDrawingAdapter.notifyDataSetChanged();
                }


                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });
        }
        return view;
    }

    public static int dpToPx(final Context context, final float dp) {
        // Took from http://stackoverflow.com/questions/8309354/formula-px-to-dp-dp-to-px-android
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.context_menu_drawing_gallery, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        Drawing drawing = mDrawingAdapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.menu_item_delete_drawing:
                mDrawingAdapter.remove(drawing);
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
        if (getActivity() == null || mListView == null) {
            Log.d(TAG, "setupAdapter mListView == null");
            return;
        }

        if (mDrawingAdapter == null) {
            Log.d(TAG, "setupAdapter mDrawingAdapter!= null");
            if(mItems == null) {
                mItems = new ArrayList<Drawing>();
            }
            mDrawingAdapter = new DrawingAdapter(
                    getActivity(),
                    mDrawingManager,
                    R.layout.item_drawing_gallery,
                    R.id.drawing_item_textView,
                    mListView,
                    true);
            mListView.setAdapter(new QuickReturnAdapter(mDrawingAdapter, 1));
//            adapter = new ArrayAdapter<Drawing>(getActivity(), R.layout.item_drawing_gallery, R.id.drawing_item_textView);
//            mListView.setAdapter(new QuickReturnAdapter(adapter, 1));
        } else if(mItems != null) {
            Log.d(TAG, "setupAdapter else");
            mDrawingAdapter.clear();
            for (Drawing drawing:mItems) {
                mDrawingAdapter.add(drawing);
//                adapter.add(drawing);
            }
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<Drawing>> {
        @Override
        protected ArrayList<Drawing> doInBackground(Void... voids) {
            Activity activity = getActivity();
            if (activity == null) {
                return new ArrayList<Drawing>();
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

        if(position >= 0) {
            Drawing item = mItems.get(position);
            Log.d(TAG, "onItemClick with drawing id " + item.getId());

            Intent intent = new Intent(getActivity(), EditorActivity.class);
            intent.putExtra(EditorFragment.EXTRA_DRAWING_ID, item.getId());
            startActivityForResult(intent, REQUEST_CHANGE);
        }

    }

}
