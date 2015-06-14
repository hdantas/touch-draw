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

import java.io.File;

/**
 * Created by hdantas on 22/11/14.
 * Controller class to defines the GridView adapter that controls the drawings' gallery.
 */
class DrawingGalleryAdapter extends ArrayAdapter<Drawing> {
    private static final String TAG = DrawingGalleryAdapter.class.getSimpleName();

    private static Context mContext;
    private final DrawingManager mDrawingManager;
    private final GridView mGridView;

    public DrawingGalleryAdapter(Context context,
                                 DrawingManager drawingManager,
                                 int itemLayout,
                                 int textViewResourceId,
                                 GridView gridView) {
        super(context, itemLayout, textViewResourceId);
        mContext = context;
        mDrawingManager = drawingManager;
        mGridView = gridView;
    }

    @Override
    public void remove(Drawing object) {
        Log.d(TAG, "remove drawing with id " + object.getId());
        String toastText = String.format(
                mContext.getResources().getString(R.string.drawing_deleted),
                object.getId()
        );
        Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();
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
        String thumbnailPath = getItem(position).getUri(mContext).getPath();
        File thumbnail = new File(thumbnailPath);

        float ratio;
        int thumbnailWidth, thumbnailHeight;
        int numColumns = mContext.getResources().getInteger(R.integer.num_columns);
        if(((ActionBarActivity) mContext).findViewById(R.id.detailFragmentContainer) == null) { //single pane
            ratio = numColumns;
        } else { //dual pane
            int editorRatio = mContext.getResources().getInteger(R.integer.ratio_editor);
            int galleryRatio = mContext.getResources().getInteger(R.integer.ratio_gallery);
            ratio = numColumns * editorRatio / galleryRatio;
        }

        if (thumbnail.exists()) {
            // load thumbnail from storage
            Bitmap src = BitmapFactory.decodeFile(thumbnailPath);
            thumbnailWidth = (int) (src.getWidth() / ratio);
            thumbnailHeight = (int) (src.getHeight() / ratio);
            bitmap = Bitmap.createScaledBitmap(src, thumbnailWidth, thumbnailHeight, false);
        } else {
            // Create white bitmap
            thumbnailWidth = (int) (mGridView.getWidth() / ratio);
            thumbnailHeight = (int) (mGridView.getHeight() / ratio);

            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            bitmap = Bitmap.createBitmap(thumbnailWidth, thumbnailHeight, conf);
            bitmap.eraseColor(mContext.getResources().getColor(R.color.white));
        }

        viewHolder.mImageView.setImageBitmap(bitmap);
        updateItemHue(position, viewHolder);
        return convertView;
    }

    @TargetApi(11)
    private void updateItemHue(int position, ViewHolder viewHolder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            viewHolder.mViewSelected.setVisibility(View.GONE);
            return;
        }

        // with the toolbar the position of the gridView starts in NUM_COLUMNS instead of 0
        int numColumns = mContext.getResources().getInteger(R.integer.num_columns);
        boolean isItemChecked = mGridView.isItemChecked(position + numColumns);
        Log.d(TAG, "updateItemHue position: " + (position + numColumns) + "\tisChecked: " + isItemChecked);
        if (isItemChecked) {
            ColorFilter filter = new PorterDuffColorFilter(
                    mContext.getResources().getColor(R.color.aqua_translucent),
                    PorterDuff.Mode.DARKEN);
            viewHolder.mImageView.setColorFilter(filter);
            viewHolder.mViewSelected.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mImageView.clearColorFilter();
            viewHolder.mViewSelected.setVisibility(View.INVISIBLE);
        }
    }
}
