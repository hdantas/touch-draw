package com.bignerdranch.android.draganddraw;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class PhotoGalleryFragment extends Fragment implements AbsListView.OnScrollListener {
    private static final String TAG = PhotoGalleryFragment.class.getSimpleName();
    private static final int REQUEST_CHANGE = 0;

    private Toast mToast;
    HeaderGridViewCompat mGridView;
    ArrayList<Drawing> mItems;
    DrawingManager mDrawingManager;
    ActionBar mActionBar;
    private int mLastFirstVisibleItem;


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d(TAG, "onScroll" +
                        "\n\tfirstVisibleItem: " + firstVisibleItem +
                        "\tmLastFirstVisibleItem: " + mLastFirstVisibleItem +
                        "\tvisibleItemCount: " + visibleItemCount +
                        "\ttotalItemCount: " + totalItemCount
        );

        final int currentFirstVisibleItem = view.getFirstVisiblePosition();

        if (currentFirstVisibleItem > mLastFirstVisibleItem) {
            if (mActionBar.isShowing()) {
                mActionBar.hide();
            }
        } else if(currentFirstVisibleItem < mLastFirstVisibleItem) {
            if (!mActionBar.isShowing()) {
                mActionBar.show();
            }
        }

        mLastFirstVisibleItem = currentFirstVisibleItem;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        setHasOptionsMenu(true);
//        setRetainInstance(true);

        mDrawingManager = DrawingManager.get(getActivity());

        updateItems();

    }

    public void updateItems() {
        Log.i(TAG, "updateItems");
        new FetchItemsTask().execute();
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

        View v = inflater.inflate(R.layout.fragment_drawing_gallery, container, false);
        View headerView = inflater.inflate(R.layout.grid_view_header, null);

        mGridView = (HeaderGridViewCompat) v.findViewById(R.id.gridView);
        mGridView.addHeaderView(headerView);
        mGridView.setOnScrollListener(this);
        setupAdapter();

        // On click start DragAndDrawFragment to edit it
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Drawing item = mItems.get(i);
                Log.d(TAG, "onItemClick with drawing id " + item.getId());

                Intent intent = new Intent(getActivity(), DragAndDrawActivity.class);
                intent.putExtra(DragAndDrawFragment.EXTRA_DRAWING_ID, item.getId());
                startActivityForResult(intent, REQUEST_CHANGE);
            }
        });


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Use floating context menus on Froyo and Gingerbread
            registerForContextMenu(mGridView);
        } else {
            // Use contextual action bar on Honeycomb and higher
            mDrawingManager = DrawingManager.get(getActivity());
            mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
            mGridView.setMultiChoiceModeListener(new GridView.MultiChoiceModeListener() {

                @Override
                @TargetApi(11)
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.drawing_gallery_item_context, menu);
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

                            DrawingAdapter adapter = (DrawingAdapter) mGridView.getAdapter();
                            for (int i = adapter.getCount(); i >= 0; i--) {
                                if (mGridView.isItemChecked(i)) {
                                    Log.d(TAG, " onActionItemClicked Delete item in position " + i + " with id " + adapter.getItem(i).getId());
                                    adapter.remove(adapter.getItem(i));
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
                    DrawingAdapter adapter = (DrawingAdapter) mGridView.getAdapter();
                    adapter.notifyDataSetChanged();
                }


                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });

        }
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.drawing_gallery_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        DrawingAdapter adapter = (DrawingAdapter) mGridView.getAdapter();
        Drawing drawing = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.menu_item_delete_drawing:
                adapter.remove(drawing);
                updateItems();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode " + requestCode + " resultCode " + resultCode);
        updateItems();

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photo_gallery, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_drawing:
                Intent intent = new Intent(getActivity(), DragAndDrawActivity.class);
                intent.putExtra(DragAndDrawFragment.EXTRA_DRAWING_ID, -1L);
                startActivityForResult(intent, REQUEST_CHANGE);

                mToast.setText("Started new activity");
                mToast.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupAdapter() {
        if (getActivity() == null || mGridView == null) {
            return;
        }

        if (mItems != null) {
            mGridView.setAdapter(new DrawingAdapter(mItems));
        } else {
            mGridView.setAdapter(null);
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

    // View lookup cache for DrawingAdapter
    private static class ViewHolder {
        ImageView mImageView;
        ImageView mViewSelected;
    }

    private class DrawingAdapter extends ArrayAdapter<Drawing> {

        public DrawingAdapter(ArrayList<Drawing> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public void remove(Drawing object) {
            Log.d(TAG, "remove drawing with id " + object.getId());
            mToast.setText("Drawing " + object.getId() + " deleted");
            mToast.show();
            mDrawingManager.removeDrawing(object);
            super.remove(object);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder; // view lookup cache stored in tag
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.drawing_gallery_overlay_item, parent, false);
                viewHolder.mImageView = (ImageView) convertView
                        .findViewById(R.id.drawing_item_imageView);
                viewHolder.mViewSelected = (ImageView) convertView
                        .findViewById(R.id.drawing_item_selected);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Bitmap bitmap;
            try {
                String path = getItem(position).getUri(getActivity()).getPath();
                FileInputStream fileInputStream = new FileInputStream(path);
                bitmap = BitmapFactory.decodeStream(fileInputStream);
                fileInputStream.close();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    updateItemHue(bitmap, position, viewHolder);
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Could not load thumbnail", e);
            } catch (IOException e) {
                Log.e(TAG, "Could not close file", e);
            }


            return convertView;
        }

        @TargetApi(11)
        private void updateItemHue(Bitmap bitmap, int position, ViewHolder viewHolder) {
            viewHolder.mImageView.setImageBitmap(bitmap);

            if (mGridView.isItemChecked(position)) {
                ColorFilter filter = new PorterDuffColorFilter(
                        getResources().getColor(R.color.aqua_translucent),
                        PorterDuff.Mode.DARKEN);
                viewHolder.mImageView.setColorFilter(filter);
                viewHolder.mViewSelected.setVisibility(View.VISIBLE);

            } else {
                viewHolder.mImageView.clearColorFilter();
                viewHolder.mViewSelected.setVisibility(View.GONE);
            }
        }
    }
}
