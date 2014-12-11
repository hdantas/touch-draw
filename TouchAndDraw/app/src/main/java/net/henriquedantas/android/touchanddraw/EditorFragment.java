package net.henriquedantas.android.touchanddraw;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import net.henriquedantas.android.utils.BitmapUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by hdantas on 10/16/14.
 * Controller class that describes the fragment containing the drawings' editor
 */
public class EditorFragment extends Fragment {
    private static final String TAG = EditorFragment.class.getSimpleName();

    private ColorfulRadioGroup mButtonShape;
    private ToggleButtonGroupTableLayout mButtonColor;
    private ImageView mRectangleImageView;
    private ImageView mTriangleImageView;
    private ImageView mCircleImageView;
    private int mColor;
    private int mAlpha;
    private EditorView mEditorView;
    private SeekBar mAlphaBar;
    private ShakeListener mShaker;
    private Drawing mDrawing;
    private DrawingManager mDrawingManager;
    private Toast mToast;

    private View mEditorLayout;
    private View mEditorLayoutPlaceholder;

    private Callbacks mCallBacks;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onDrawingUpdated(long drawingId);
        void onDrawingFinished();
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

    public void onDrawingUpdated() {
        saveDrawing();
        mCallBacks.onDrawingUpdated(mDrawing.getId());
    }

    @Override
    public void onResume() {
        if (mShaker != null) {
            mShaker.resume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: saved " + mDrawingManager.getBoxes().size());
        if (mShaker != null) {
            mShaker.pause();
        }
        saveDrawing();
        super.onPause();
    }

    public void goBack() {
        // The user has pushed the back button
        Log.i(TAG, "goBack");
        mCallBacks.onDrawingFinished();
    }

    public static EditorFragment newInstance() {
        return newInstance(-1);
    }

    public static EditorFragment newInstance(long drawingId) {
        Bundle args = new Bundle();
        args.putLong(EditorActivity.EXTRA_DRAWING_ID, drawingId);

        EditorFragment editorFragment = new EditorFragment();
        editorFragment.setArguments(args);

        return editorFragment;
    }

    @SuppressLint("ShowToast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        long drawingId = getArguments().getLong(EditorActivity.EXTRA_DRAWING_ID, -1L);

        mDrawingManager = DrawingManager.get(getActivity());
        mDrawing = mDrawingManager.loadDrawing(drawingId);
        if (mDrawing != null) {
            Log.d(TAG, "onCreate: Loaded Drawing with id " + mDrawing.getId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_editor, container, false);

        mEditorView = (EditorView) v.findViewById(R.id.editorView);
        mEditorView.setDrawingManager(mDrawingManager);
        mEditorView.setToolbar((LinearLayout) v.findViewById(R.id.toolbarEditor));

        mEditorLayout = v.findViewById(R.id.editorLayout);
        mEditorLayoutPlaceholder = v.findViewById(R.id.editorLayoutPlaceholder);

        if (mDrawing == null) {
            mEditorLayout.setVisibility(View.INVISIBLE);
            mEditorLayoutPlaceholder.setVisibility(View.VISIBLE);
        } else {
            mEditorLayout.setVisibility(View.VISIBLE);
            mEditorLayoutPlaceholder.setVisibility(View.INVISIBLE);
            mEditorView.loadBoxes();
        }

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

        mRectangleImageView = (ImageView) v.findViewById(R.id.imageRectangle);
        mTriangleImageView = (ImageView) v.findViewById(R.id.imageTriangle);
        mCircleImageView = (ImageView) v.findViewById(R.id.imageCircle);

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
        try {
            mShaker = new ShakeListener(getActivity());
            mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
                public void onShake() {
                    vibe.vibrate(getResources().getInteger(R.integer.vibration_millis));
                    mEditorView.undoLastBox();
                }
            });
        } catch (UnsupportedOperationException e) {
            Log.e(TAG, "Accelerometer not supported");
        }

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

        if (mEditorView != null) {
            mEditorView.setDrawableShape(shape);
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

        if (mEditorView != null) {
            mEditorView.setDrawableColor(mColor, mAlpha);
        }

        setImageViewColor(mRectangleImageView, getResources().getColor(mColor), mAlpha);
        setImageViewColor(mTriangleImageView, getResources().getColor(mColor), mAlpha);
        setImageViewColor(mCircleImageView, getResources().getColor(mColor), mAlpha);
        setSeekBarColor(mAlphaBar, getResources().getColor(mColor), mAlpha);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mButtonShape.setButtonsColor(getResources().getColor(mColor));
        }


    }

    void setImageViewColor(ImageView imageView, int newColor, int alpha) {
        int transformedColor = (newColor & 0x00FFFFFF) | (alpha << 24);
        ColorFilter filter = new LightingColorFilter(0, transformedColor);
//        imageView.setColorFilter(filter);
        imageView.getDrawable().setColorFilter(filter);
        imageView.invalidate();
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

        seekBar.invalidate();
    }

    private boolean saveDrawing() {
        boolean success = true;

        // if mDrawing is null it has been deleted
        if (mDrawing != null) {
            success = saveThumbnail();
            mDrawingManager.updateDrawing(mDrawing);
        }
        return success;
    }

    private boolean saveThumbnail() {

        int thumbnail_width = mEditorView.getWidth();
        int thumbnail_height = mEditorView.getHeight();
        mEditorView.setDrawingCacheEnabled(true);
        mEditorView.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(mEditorView.getDrawingCache(true));
        mEditorView.setDrawingCacheEnabled(false);

        Log.d(TAG, "saveThumbnail\n" +
                "\tDrawingId " + mDrawing.getId() +
                "\twidth/height: " + thumbnail_width + "/" + thumbnail_height +
                "\tfilename: " + mDrawing.getFilename());


        /* Write bitmap to file using format defined in Drawing */
        Bitmap.CompressFormat format = Bitmap.CompressFormat.valueOf(mDrawing.getFileFormat());
        File file = BitmapUtils.saveBitmapToPrivateInternalStorage
                (getActivity(), mDrawing.getFilename(), bitmap, format);

        return file != null;
    }

    private boolean saveDrawingToGallery() {

        if (mDrawing == null) {
            Toast.makeText(getActivity(), getString(R.string.no_drawing_to_save),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        mEditorView.setDrawingCacheEnabled(true);
        mEditorView.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(mEditorView.getDrawingCache(true));
        mEditorView.setDrawingCacheEnabled(false);
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
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.item_save_drawing).setVisible(mDrawing != null);
        menu.findItem(R.id.item_share_drawing).setVisible(mDrawing != null);
        menu.findItem(R.id.item_delete_drawing).setVisible(mDrawing != null);
        menu.findItem(R.id.item_undo_drawing).setVisible(mDrawing != null);

        Log.d(TAG, "onPrepareOptionsMenu:\n" +
                "visibility: " + menu.hasVisibleItems() +
                "\tmDrawing != null: " + (mDrawing != null));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_save_drawing:
                boolean success = saveDrawingToGallery();
                String successString = getString(R.string.drawing_saved_succeeded);
                String failureString = getString(R.string.drawing_saved_failed);
                mToast.setText(success ? successString : failureString);
                mToast.show();
                return true;

            case R.id.item_share_drawing:
                sendShareIntent();
                return true;

            case android.R.id.home: // Respond to the action bar's Up/Home button
                mCallBacks.onDrawingFinished();
                return true;

            case R.id.item_delete_drawing:
                deleteDrawing();
                mCallBacks.onDrawingFinished();
                return true;

            case R.id.item_undo_drawing:
                undoDrawing();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendShareIntent() {

        if (mDrawing == null) {
            mToast.setText(R.string.no_drawing_to_share);
            mToast.show();
            return;
        }

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

    public void deleteDrawing() {
        // create toast text before deleting drawing to preserve drawing id
        String toastText;
        if (mDrawing != null) {
            toastText = String.format(getString(R.string.drawing_deleted), mDrawing.getId());
            mDrawingManager.removeDrawing(mDrawing);
            mDrawing = null;
        } else {
            toastText = getString(R.string.no_drawing_to_delete);
        }

        mToast.setText(toastText);
        mToast.show();
    }

    private void undoDrawing() {
        if (mDrawing != null) {
            mEditorView.undoLastBox();
        } else {
            String toastText = getString(R.string.no_drawing_to_undo);
            mToast.setText(toastText);
            mToast.show();
        }

    }
}
