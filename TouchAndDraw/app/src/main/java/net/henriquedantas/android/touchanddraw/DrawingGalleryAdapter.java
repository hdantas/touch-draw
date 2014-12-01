package net.henriquedantas.android.touchanddraw;

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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by hdantas on 22/11/14.
 * Controller class to defines the GridView adapter that controls the drawings' gallery.
 */
class DrawingGalleryAdapter extends ArrayAdapter<Drawing> {
    private static final String TAG = DrawingGalleryAdapter.class.getSimpleName();

    private static Context mContext;
    private final DrawingManager mDrawingManager;
    private final GridView mGridView;
    private final int mGridViewNumColumns;

    public DrawingGalleryAdapter(Context context,
                                 DrawingManager drawingManager,
                                 int itemLayout,
                                 int textViewResourceId,
                                 GridView gridView,
                                 int gridViewNumColumns) {
        super(context, itemLayout, textViewResourceId);
        mContext = context;
        mDrawingManager = drawingManager;
        mGridView = gridView;
        mGridViewNumColumns = gridViewNumColumns;
    }

    @Override
    public void remove(Drawing object) {
        Log.d(TAG, "remove drawing with id " + object.getId());
        Toast.makeText(mContext, "Drawing " + object.getId() + " deleted", Toast.LENGTH_SHORT).
                show();
        mDrawingManager.removeDrawing(object);
        super.remove(object);
    }

    // View lookup cache for DrawingAdapter
    private static class ViewHolder {
        ImageView mImageView;
        ImageView mViewSelected;
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
                    .findViewById(R.id.drawing_item_image_view);
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
            viewHolder.mImageView.setImageBitmap(bitmap);
            updateItemHue(position, viewHolder);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not load thumbnail", e);
        } catch (IOException e) {
            Log.e(TAG, "Could not close file", e);
        }

        return convertView;
    }

    @TargetApi(11)
    private void updateItemHue(int position, ViewHolder viewHolder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            viewHolder.mViewSelected.setVisibility(View.GONE);
            return;
        }

        // with the toolbar the position of the gridView starts in NUM_COLUMNS instead of 0
        boolean isItemChecked = mGridView.isItemChecked(position + mGridViewNumColumns);
        Log.d(TAG, "updateItemHue position: " + (position + mGridViewNumColumns) + "\tisChecked: " + isItemChecked);
        if (isItemChecked) {
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
