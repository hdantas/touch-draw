package com.bignerdranch.android.draganddraw;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bignerdranch.android.utils.BitmapUtils;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by nuno on 10/16/14.
 */
public class DragAndDrawFragment extends Fragment {
    private static final String TAG = DragAndDrawFragment.class.getSimpleName();

    public static final String EXTRA_DRAWING_ID =
            "com.bignerdranch.android.criminalintent.extra_drawing_id";

    private RadioGroup mButtonShape;
    private ToggleButtonGroupTableLayout mButtonColor;
    private DrawableShape mShape;
    private int mColor;
    private int mAlpha;
    private BoxDrawingView mBoxView;
    private SeekBar mAlphaBar;
    private ShakeListener mShaker;
    private Drawing mDrawing;
    private DrawingManager mDrawingManager;
    private boolean mViewVisible = true;

    @Override
    public void onResume() {
        mShaker.resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: saved " + mDrawingManager.getBoxes().size());
        mShaker.pause();
        if (mDrawing != null) {
            saveDrawingCompressed();
        }
        super.onPause();
    }

    public void goBack() {
        // The user has pushed the back button
        Log.i(TAG, "goBack");
        returnFromIntent();
    }

    public void returnFromIntent() {
        Log.i(TAG, "returnFromIntent");
        // if mDrawing is null it has been deleted
        if (mDrawing != null) {
            mDrawingManager.updateDrawing(mDrawing);
        }
        Intent intent = new Intent(getActivity(), PhotoGalleryActivity.class);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        Bundle extras = getActivity().getIntent().getExtras();
        long drawingId = extras.getLong(EXTRA_DRAWING_ID, -1L);

        mDrawingManager = DrawingManager.get(getActivity());
        if (drawingId != -1) {
            mDrawing = mDrawingManager.loadDrawing(drawingId);
            Log.d(TAG, "onCreate: Loaded Drawing with id " + mDrawing.getId());
        } else {
            mDrawing = mDrawingManager.startNewDrawing();
            Log.d(TAG, "onCreate: Created new Drawing with id " + mDrawing.getId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drag_and_draw, container, false);

        mBoxView = (BoxDrawingView) v.findViewById(R.id.viewBox);
        mBoxView.setDrawingCacheEnabled(true);
        mBoxView.setDrawingManager(mDrawingManager);
        mBoxView.loadBoxes();

        mButtonColor = (ToggleButtonGroupTableLayout) v.findViewById(R.id.buttonColor);
        mButtonColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateColor();
            }
        });

        mButtonShape = (RadioGroup) v.findViewById(R.id.buttonShape);
        mButtonShape.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateShape();
            }
        });

        mAlphaBar = (SeekBar) v.findViewById(R.id.alphaBar);
        mAlpha = mAlphaBar.getProgress();
        mAlphaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAlpha = progress;
                updateColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        mShaker = new ShakeListener(getActivity());
        mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
            public void onShake() {
                vibe.vibrate(getResources().getInteger(R.integer.vibration_millis));
                mBoxView.clearBoxes();
            }
        });


        updateColor();
        updateShape();

        return v;
    }

    private void updateShape() {
        int checkedId = mButtonShape.getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.buttonTriangle:
                mShape = DrawableShape.TRIANGLE;
                break;
            case R.id.buttonCircle:
                mShape = DrawableShape.CIRCLE;
                break;
            default:
                mShape = DrawableShape.RECTANGLE;
        }

//        Log.d(TAG, "UpdatedShape checkedId: " + checkedId + " shape: " + mShape);
        if (mBoxView != null) {
            mBoxView.setDrawableShape(mShape);
        }
    }

    private void updateColor() {
        int checkedId = mButtonColor.getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.buttonRed:
                mColor = DrawableColor.RED;
                break;
            case R.id.buttonGreen:
                mColor = DrawableColor.GREEN;
                break;
            case R.id.buttonBlue:
                mColor = DrawableColor.BLUE;
                break;
            case R.id.buttonOrange:
                mColor = DrawableColor.ORANGE;
                break;
            case R.id.buttonYellow:
                mColor = DrawableColor.YELLOW;
                break;
            case R.id.buttonPurple:
                mColor = DrawableColor.PURPLE;
        }

        if (mBoxView != null) {
//            Log.d(TAG, "UpdatedColor color: " + mColor + " alpha " + mAlpha);
            mBoxView.setDrawableColor(mColor, mAlpha);
        }
        setSeekBarColor(mAlphaBar, getResources().getColor(mColor), mAlpha);
    }

    public void setSeekBarColor(SeekBar seekBar, int newColor, int newAlpha) {
        Drawable drawable = seekBar.getProgressDrawable();

        int transformedColor = (newColor & 0x00FFFFFF) | (newAlpha << 24);
//        Log.d(TAG, String.format("transformedColor: 0x%8s  |  alpha: 0x%2s",
//                        Integer.toHexString(transformedColor).replace(' ', '0'),
//                        Integer.toHexString(newAlpha).replace(' ', '0'))
//        );

        ColorFilter filter = new LightingColorFilter(0, transformedColor);
        drawable.setColorFilter(filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            seekBar.getThumb().setColorFilter(filter);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            seekBar.setAlpha(((float) newAlpha) / 255);
        }
    }


    private boolean saveDrawingCompressed() {
        if (!mBoxView.isDrawingCacheEnabled()) {
            Log.d(TAG, "saveDrawing Failed to save drawing");
            return false;
        }

        int thumbnail_width = (int) getResources().getDimension(R.dimen.thumbnail_width);
        float ratio = mBoxView.getWidth() / thumbnail_width;
        int thumbnail_height = (int) (mBoxView.getHeight() / ratio);

        Bitmap bitmap = Bitmap.createScaledBitmap(mBoxView.getDrawingCache(),
                thumbnail_width, thumbnail_height, false);

        Log.d(TAG, "saveDrawing compressed DrawingId " + mDrawing.getId() +
                " width/height: " + thumbnail_width + "/" + thumbnail_height +
                " ratio: " + ratio +
                " filename: " + mDrawing.getFilename());

        /* Write bitmap to file using format defined in Drawing */
        Bitmap.CompressFormat format = Bitmap.CompressFormat.valueOf(mDrawing.getFileFormat());
        return BitmapUtils.saveBitmapToPrivateInternalStorage
                (getActivity(),mDrawing.getFilename(), bitmap, format);

    }

    private boolean saveDrawingToGallery() {
        boolean success;

        Bitmap bitmap = Bitmap.createBitmap(mBoxView.getDrawingCache());
        Bitmap.CompressFormat format = Bitmap.CompressFormat.valueOf(mDrawing.getFileFormat());
        String filename = "Drawing " + mDrawing.getId() + " "
                + DateFormat.getDateInstance().format(new Date())
                + "." + mDrawing.getFileFormat().toLowerCase();

        success = BitmapUtils.saveBitmapToAlbumPublicExternalStorage
                (getActivity(), filename, bitmap, format);

        if (!success) {
            Log.e(TAG, "saveDrawingToGallery FAIL", new Exception());
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, mDrawing.getStartDate().getTime());

        String mime_type = "image/" + mDrawing.getFileFormat().toLowerCase();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + mime_type);

        String filePath = BitmapUtils.getAlbumPublicExternalStorageDir(getActivity()).getPath()
                + "/" + filename;
        values.put(MediaStore.MediaColumns.DATA, filePath);

        getActivity().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.drag_and_draw, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export_drawing:
                boolean success = saveDrawingToGallery();
                String toastText =
                        success ? "Successfully saved drawing" : "Failed to save drawing!";
                Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.share_drawing:
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.share_drawing),
                        Toast.LENGTH_SHORT).show();
                return true;
            case R.id.new_drawing:
                int translation = mViewVisible ? 100 - mBoxView.getHeight() : 0;
                mViewVisible = !mViewVisible;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mBoxView.animate().translationY(translation).withLayer();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    mBoxView.animate().translationY(translation);
                }
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.new_drawing),
                        Toast.LENGTH_SHORT).show();
                return true;
            case R.id.delete_drawing:
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.delete_drawing),
                        Toast.LENGTH_SHORT).show();
                deleteDrawing();
                returnFromIntent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteDrawing() {
        mDrawingManager.removeDrawing(mDrawing);
        mDrawing = null;
    }

}
