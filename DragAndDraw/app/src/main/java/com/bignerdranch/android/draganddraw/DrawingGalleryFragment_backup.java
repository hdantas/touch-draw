package com.bignerdranch.android.draganddraw;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

public class DrawingGalleryFragment_backup extends Fragment {
    private static final String TAG = DrawingGalleryFragment_backup.class.getSimpleName();
    private static final int REQUEST_CHANGE = 0;

    private QuickReturnListView mListView;
    private Toolbar mQuickReturnView;
    private View mHeader;
    private View mPlaceHolder;

    private int mCachedVerticalScrollRange;
    private int mQuickReturnHeight;

    private static final int STATE_ONSCREEN = 0;
    private static final int STATE_OFFSCREEN = 1;
    private static final int STATE_RETURNING = 2;
    private int mState = STATE_ONSCREEN;
    private int mScrollY;
    private int mMinRawY = 0;

    private TranslateAnimation anim;
    private Toast mToast;
    ArrayList<Drawing> mItems;
    DrawingManager mDrawingManager;
    DrawingAdapter mDrawingAdapter;

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

        mQuickReturnView = (Toolbar) view.findViewById(R.id.toolbar_action_bar);
        ((ActionBarActivity) getActivity()).setSupportActionBar(mQuickReturnView);

        mHeader = inflater.inflate(R.layout.header, null);
        mPlaceHolder = mHeader.findViewById(R.id.placeholder);

        mListView = (QuickReturnListView) view.findViewById(R.id.list_view);
        mListView.addHeaderView(mHeader);
        setupAdapter();

        mListView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.d(TAG, "onGlobalLayout");
                        mQuickReturnHeight = mQuickReturnView.getHeight();
                        mListView.computeScrollY();
                        mCachedVerticalScrollRange = mListView.getListHeight();
                    }
                });

        // On click start DragAndDrawFragment to edit it
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // position - 1 since position = 0 of the list view refers to the header
                if(position > 0) {
                    Drawing item = mItems.get(position - 1);
                    Log.d(TAG, "onItemClick with drawing id " + item.getId());

                    Intent intent = new Intent(getActivity(), EditorActivity.class);
                    intent.putExtra(EditorFragment.EXTRA_DRAWING_ID, item.getId());
                    startActivityForResult(intent, REQUEST_CHANGE);
                }
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                Log.d(TAG, "onScroll");
                mScrollY = 0;
                int translationY = 0;

                if (mListView.scrollYIsComputed()) {
                    mScrollY = mListView.getComputedScrollY();
                }

                int rawY = mPlaceHolder.getTop()
                        - Math.min(
                        mCachedVerticalScrollRange
                                - mListView.getHeight(), mScrollY);

                switch (mState) {
                    case STATE_OFFSCREEN:
                        if (rawY <= mMinRawY) {
                            mMinRawY = rawY;
                        } else {
                            mState = STATE_RETURNING;
                        }
                        translationY = rawY;
                        break;

                    case STATE_ONSCREEN:
                        if (rawY < -mQuickReturnHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                        translationY = rawY;
                        break;

                    case STATE_RETURNING:
                        translationY = (rawY - mMinRawY) - mQuickReturnHeight;
                        if (translationY > 0) {
                            translationY = 0;
                            mMinRawY = rawY - mQuickReturnHeight;
                        }

                        if (rawY > 0) {
                            mState = STATE_ONSCREEN;
                            translationY = rawY;
                        }

                        if (translationY < -mQuickReturnHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                        break;
                }

                /** this can be used if the build is below honeycomb **/
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                    anim = new TranslateAnimation(0, 0, translationY,
                            translationY);
                    anim.setFillAfter(true);
                    anim.setDuration(0);
                    mQuickReturnView.startAnimation(anim);
                } else {
                    mQuickReturnView.setTranslationY(translationY);
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Use floating context menus on Froyo and Gingerbread
            registerForContextMenu(mListView);
        } else {
            // Use contextual action bar on Honeycomb and higher
            mListView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new GridView.MultiChoiceModeListener() {

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
        mListView.performItemClick(mHeader, 0, mHeader.getId());
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
        if (getActivity() == null || mListView == null) {
            return;
        }

        if (mItems != null) {
            mDrawingAdapter = new DrawingAdapter(
                    getActivity(),
                    mItems,
                    mDrawingManager,
                    mListView,
                    true);
            mListView.setAdapter(mDrawingAdapter);
            Log.d(TAG, "setupAdapter");
        } else {
            mDrawingAdapter = null;
            mListView.setAdapter(null);
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
}
