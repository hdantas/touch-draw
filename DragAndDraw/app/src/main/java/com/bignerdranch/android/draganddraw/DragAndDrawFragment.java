package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;

/**
 * Created by nuno on 10/16/14.
 */
public class DragAndDrawFragment extends Fragment {
    private static final String TAG = DragAndDrawFragment.class.getSimpleName();

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

    @Override
    public void onResume() {
        mShaker.resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mShaker.pause();
        super.onPause();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDrawingManager = DrawingManager.get(getActivity());
        mDrawing = mDrawingManager.getLastDrawing();
        if (mDrawing == null) { // if there was no last drawing
            mDrawing = mDrawingManager.startNewDrawing();
            Log.d(TAG, "onCreate: Created new Drawing with id " + mDrawing.getId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drag_and_draw, container, false);

        mBoxView = (BoxDrawingView) v.findViewById(R.id.viewBox);
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
                vibe.vibrate(300);
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
        LayerDrawable ld = (LayerDrawable) seekBar.getProgressDrawable();

        int transformedColor = (newColor & 0x00FFFFFF) | (newAlpha << 24);
//        Log.d(TAG, String.format("transformedColor: 0x%8s  |  alpha: 0x%2s",
//                        Integer.toHexString(transformedColor).replace(' ', '0'),
//                        Integer.toHexString(newAlpha).replace(' ', '0'))
//        );

        ColorFilter filter = new LightingColorFilter(0, transformedColor);
        ld.setColorFilter(filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            seekBar.getThumb().setColorFilter(filter);
        }

        seekBar.setAlpha(((float) newAlpha) / 255);
    }


    @Override
    public void onStop() {
        Log.d(TAG, "onStop: saved " + mDrawingManager.getBoxes().size());
        super.onStop();
    }
}
