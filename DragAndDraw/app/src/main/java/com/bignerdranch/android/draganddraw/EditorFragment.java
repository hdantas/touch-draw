package com.bignerdranch.android.draganddraw;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
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

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by hdantas on 10/16/14.
 * Controller class that describes the fragment containing the drawings' editor
 */
public class EditorFragment extends Fragment {
    private static final String TAG = EditorFragment.class.getSimpleName();

    public static final String EXTRA_DRAWING_ID =
            "com.bignerdranch.android.draganddraw.extra_drawing_id";

    private ColorfulRadioGroup mButtonShape;
    private ToggleButtonGroupTableLayout mButtonColor;
    private int mColor;
    private int mAlpha;
    private EditView mBoxView;
    private SeekBar mAlphaBar;
    private ShakeListener mShaker;
    private Drawing mDrawing;
    private DrawingManager mDrawingManager;
    private Toast mToast;

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

    void returnFromIntent() {
        Log.i(TAG, "returnFromIntent");
        // if mDrawing is null it has been deleted
        if (mDrawing != null) {
            mDrawingManager.updateDrawing(mDrawing);
        }
        Intent intent = new Intent(getActivity(), DrawingGalleryActivity.class);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @SuppressLint("ShowToast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

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
        View v = inflater.inflate(R.layout.fragment_editor, container, false);

        mBoxView = (EditView) v.findViewById(R.id.viewBox);

        mBoxView.setDrawingCacheEnabled(true);
        mBoxView.setDrawingManager(mDrawingManager);
        mBoxView.setToolbar((Toolbar) v.findViewById(R.id.toolBar));
        mBoxView.loadBoxes();


        mButtonColor = (ToggleButtonGroupTableLayout) v.findViewById(R.id.buttonColor);
        mButtonColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateColor();
            }
        });

        mButtonShape = (ColorfulRadioGroup) v.findViewById(R.id.buttonShape);
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
                mBoxView.undoLastBox();
            }
        });


        updateColor();
        updateShape();

        return v;
    }

    private void updateShape() {
        int checkedId = mButtonShape.getCheckedRadioButtonId();
        DrawableShape shape;
        switch (checkedId) {
            case R.id.buttonTriangle:
                shape = DrawableShape.TRIANGLE;
                break;
            case R.id.buttonCircle:
                shape = DrawableShape.CIRCLE;
                break;
            default:
                shape = DrawableShape.RECTANGLE;
        }

        if (mBoxView != null) {
            mBoxView.setDrawableShape(shape);
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
            mBoxView.setDrawableColor(mColor, mAlpha);
        }
        setSeekBarColor(mAlphaBar, getResources().getColor(mColor), mAlpha);
        mButtonShape.setButtonsColor(getResources().getColor(mColor));

    }

    void setSeekBarColor(SeekBar seekBar, int newColor, int alpha) {
        Drawable drawable = seekBar.getProgressDrawable();

        int transformedColor = (newColor & 0x00FFFFFF) | (alpha << 24);
        ColorFilter filter = new LightingColorFilter(0, transformedColor);
        drawable.setColorFilter(filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            seekBar.getThumb().setColorFilter(filter);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            float newAlpha = ((float) alpha) / 255;
            if (newAlpha < .3) {
                newAlpha = 0.3f;
            }
            seekBar.setAlpha(newAlpha);
        }
    }

    private boolean saveDrawingCompressed() {
        if (!mBoxView.isDrawingCacheEnabled()) {
            Log.d(TAG, "saveDrawing Failed to save drawing");
            return false;
        }

        int numColumns = getResources().getInteger(R.integer.num_columns);
        int thumbnail_width = (int) ((mBoxView.getWidth() / numColumns)
                - getResources().getDimension(R.dimen.item_horizontal_spacing));
        int thumbnail_height = (int) ((mBoxView.getHeight() / numColumns)
                - getResources().getDimension(R.dimen.item_vertical_spacing));
        Bitmap bitmap = Bitmap.createScaledBitmap(mBoxView.getDrawingCache(),
                thumbnail_width, thumbnail_height, false);


        Log.d(TAG, "saveDrawing compressed DrawingId " + mDrawing.getId() +
                " width/height: " + thumbnail_width + "/" + thumbnail_height +
                " filename: " + mDrawing.getFilename());

        /* Write bitmap to file using format defined in Drawing */
        Bitmap.CompressFormat format = Bitmap.CompressFormat.valueOf(mDrawing.getFileFormat());
        File file = BitmapUtils.saveBitmapToPrivateInternalStorage
                (getActivity(), mDrawing.getFilename(), bitmap, format);

        return file != null;

    }

    private boolean saveDrawingToGallery() {

        Bitmap bitmap = Bitmap.createBitmap(mBoxView.getDrawingCache());
        Bitmap.CompressFormat format = Bitmap.CompressFormat.valueOf(mDrawing.getFileFormat());
        String filename = "Drawing " + mDrawing.getId() + " "
                + DateFormat.getDateTimeInstance().format(new Date())
                + "." + mDrawing.getFileFormat().toLowerCase();
        filename = filename.replace(":", "_"); // ':' is an illegal char for a filename
        File file = BitmapUtils.saveBitmapToAlbum(getActivity(), filename, bitmap, format);

        if (file == null) {
            Log.e(TAG, "saveDrawingToGallery FAIL", new Exception());
            return false;
        }

        mDrawing.setDrawingUri(Uri.fromFile(file));
        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)));

        Log.d(TAG, "saveDrawingToGallery\npath " + file.getPath() +
                "\tgetExternalStorageDirectory: " + Environment.getExternalStorageDirectory() +
                "\turiFromFile: " + Uri.fromFile(file)
        );

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_editor, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export_drawing:
                boolean success = saveDrawingToGallery();
                String toastText =
                        success ? "Successfully saved drawing" : "Failed to save drawing!";
                mToast.setText(toastText);
                mToast.show();
                return true;
            case R.id.share_drawing:
                sendShareIntent();
                mToast.setText(getString(R.string.share_drawing));
                mToast.show();
                return true;

            case android.R.id.home: // Respond to the action bar's Up/Home button
                returnFromIntent();
                return true;

            case R.id.delete_drawing:
                deleteDrawing();
                mToast.setText(getString(R.string.delete_drawing));
                mToast.show();
                returnFromIntent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendShareIntent() {
        saveDrawingToGallery();
        Uri uri = mDrawing.getDrawingUri();
        Log.d(TAG, "sendShareIntent uri: " + uri);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/" + mDrawing.getFileFormat());

        // This flag clears the called app from the activity stack, so users arrive in the expected
        // place next time this application is restarted.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            //noinspection deprecation
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }
        shareIntent = Intent.createChooser(shareIntent, getString(R.string.share_drawing_to));
        startActivity(shareIntent);
    }

    private void deleteDrawing() {
        mDrawingManager.removeDrawing(mDrawing);
        mDrawing = null;
    }


}
