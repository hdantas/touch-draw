package com.bignerdranch.android.draganddraw;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nuno on 22/11/14.
 */
public class DrawingAdapter extends ArrayAdapter<Drawing> {
    private static final String TAG = DrawingAdapter.class.getSimpleName();

    private static Context mContext;
    private static Toast mToast;
    private DrawingManager mDrawingManager;
    private ListView mListView;

    public DrawingAdapter(Context context, ArrayList<Drawing> items,
                          DrawingManager drawingManager, ListView listView) {
        super(context, 0, items);
        mContext = context;
        mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        mDrawingManager = drawingManager;
        mListView = listView;
    }

    // View lookup cache for DrawingAdapter
    private static class ViewHolder {
        ImageView mImageView;
        ImageView mViewSelected;
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
            convertView = ((ActionBarActivity) mContext).getLayoutInflater()
                    .inflate(R.layout.item_drawing_gallery, parent, false);
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
            String path = getItem(position).getUri(mContext).getPath();
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

        if (mListView.isItemChecked(position)) {
            ColorFilter filter = new PorterDuffColorFilter(
                    mContext.getResources().getColor(R.color.aqua_translucent),
                    PorterDuff.Mode.DARKEN);
            viewHolder.mImageView.setColorFilter(filter);
            viewHolder.mViewSelected.setVisibility(View.VISIBLE);

        } else {
            viewHolder.mImageView.clearColorFilter();
            viewHolder.mViewSelected.setVisibility(View.GONE);
        }
    }
}
