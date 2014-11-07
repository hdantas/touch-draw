package com.bignerdranch.android.draganddraw;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = PhotoGalleryFragment.class.getSimpleName();
    private static final int REQUEST_CHANGE = 0;
    public static int THUMBNAILS_PER_ROW;

    GridView mGridView;
    ArrayList<Drawing> mItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        THUMBNAILS_PER_ROW = getActivity().getResources().getInteger(R.integer.thumbnails_per_row);

        setHasOptionsMenu(true);
        setRetainInstance(true);
        updateItems();
    }

    public void updateItems() {
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mGridView = (GridView) v.findViewById(R.id.gridView);
        int column_width;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            column_width = size.x / THUMBNAILS_PER_ROW;
        } else {
            int width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
            column_width = width / THUMBNAILS_PER_ROW;
        }
        mGridView.setColumnWidth(column_width);
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
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode " + requestCode + " resultCode " + resultCode);
        if (resultCode != Activity.RESULT_OK) {
            updateItems();
        }
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
            case R.id.new_image:
                Intent intent = new Intent(getActivity(), DragAndDrawActivity.class);
                intent.putExtra(DragAndDrawFragment.EXTRA_DRAWING_ID, -1);
                startActivityForResult(intent, REQUEST_CHANGE);

                String toastText = "Started new activity";
                Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
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

            DrawingManager drawingManager = DrawingManager.get(getActivity());
            ArrayList<Drawing> drawingArrayList = drawingManager.getAllDrawings();
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
    }

    private class DrawingAdapter extends ArrayAdapter<Drawing> {

        public DrawingAdapter(ArrayList<Drawing> items) {
            super(getActivity(), 0, items);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder; // view lookup cache stored in tag
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.drawing, parent, false);

                ImageView imageView = (ImageView) convertView
                        .findViewById(R.id.drawing_imageView);

                int thumbnail_width, thumbnail_height;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    Point size = new Point();
                    getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                    thumbnail_width = size.x / THUMBNAILS_PER_ROW;
                    thumbnail_height = size.y / THUMBNAILS_PER_ROW;
                } else {
                    thumbnail_width = getActivity().getWindowManager().getDefaultDisplay()
                            .getWidth() / THUMBNAILS_PER_ROW;
                    thumbnail_height = getActivity().getWindowManager().getDefaultDisplay()
                            .getHeight() / THUMBNAILS_PER_ROW;
                }

                imageView.getLayoutParams().width = thumbnail_width;
                imageView.getLayoutParams().height = thumbnail_height;
                viewHolder.mImageView = imageView;
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            try {
                String path = getItem(position).getUri(getActivity()).getPath();
                FileInputStream fileInputStream = new FileInputStream(path);
                final Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                viewHolder.mImageView.setImageBitmap(bitmap);
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Could not load thumbnail", e);
            } catch (IOException e) {
                Log.e(TAG, "Could not close file", e);
            }
            return convertView;
        }
    }
}
