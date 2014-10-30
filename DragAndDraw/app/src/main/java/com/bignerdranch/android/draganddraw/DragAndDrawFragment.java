package com.bignerdranch.android.draganddraw;

import android.os.Bundle;
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
    static final String TAG = DragAndDrawFragment.class.getSimpleName();

    RadioGroup mButtonShape;
    ToggleButtonGroupTableLayout mButtonColor;
    DrawableShape mShape;
    int mColor;
    int mAlpha;
    BoxDrawingView mBoxView;
    CircleRadioButton mButtonRed, mButtonGreen, mButtonBlue, mButtonOrange, mButtonYellow, mButtonPurple;
    SeekBar mAlphaBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drag_and_draw, container, false);

        mBoxView = (BoxDrawingView) v.findViewById(R.id.viewBox);

        mButtonRed = (CircleRadioButton) v.findViewById(R.id.buttonRed);
        mButtonGreen = (CircleRadioButton) v.findViewById(R.id.buttonGreen);
        mButtonBlue = (CircleRadioButton) v.findViewById(R.id.buttonBlue);
        mButtonOrange = (CircleRadioButton) v.findViewById(R.id.buttonOrange);
        mButtonYellow = (CircleRadioButton) v.findViewById(R.id.buttonYellow);
        mButtonPurple = (CircleRadioButton) v.findViewById(R.id.buttonPurple);

        mButtonColor = (ToggleButtonGroupTableLayout) v.findViewById(R.id.buttonColor);
        int checkedButtonId = mButtonColor.getCheckedRadioButtonId();
        if (checkedButtonId != -1) {
            Log.d(TAG, "onCreateView: checkedRadioButtonId " +
                    getResources().getResourceEntryName(checkedButtonId));
        }
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
                mAlpha = 100 - progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateColor();
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

        Log.d(TAG, "UpdatedShape checkedId: " + checkedId + " shape: " + mShape);
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
            Log.d(TAG, "UpdatedColor color: " + mColor + " alpha " + mAlpha);
            mBoxView.setDrawableColor(mColor, mAlpha);
        }
    }

}
