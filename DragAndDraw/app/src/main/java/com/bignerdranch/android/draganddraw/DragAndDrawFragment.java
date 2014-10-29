package com.bignerdranch.android.draganddraw;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

/**
 * Created by nuno on 10/16/14.
 */
public class DragAndDrawFragment extends Fragment {
    static final String TAG = DragAndDrawFragment.class.getSimpleName();

    RadioGroup mButtonShape;
    DrawableShape mShape = DrawableShape.RECTANGLE;
    DrawableColor mColor = DrawableColor.RED;
    BoxDrawingView mBoxView;
    at.markushi.ui.CircleButton mButtonRed, mButtonGreen, mButtonBlue;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drag_and_draw, container, false);

        mBoxView = (BoxDrawingView) v.findViewById(R.id.viewBox);

        mButtonRed = (at.markushi.ui.CircleButton) v.findViewById(R.id.buttonRed);
        mButtonRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColor = DrawableColor.RED;
                updateColor();
            }
        });

        mButtonGreen = (at.markushi.ui.CircleButton) v.findViewById(R.id.buttonGreen);
        mButtonGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColor = DrawableColor.GREEN;
                updateColor();
            }
        });

        mButtonBlue = (at.markushi.ui.CircleButton) v.findViewById(R.id.buttonBlue);
        mButtonBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColor = DrawableColor.BLUE;
                updateColor();
            }
        });
        updateColor();

        mButtonShape = (RadioGroup) v.findViewById(R.id.buttonShape);
        updateShape(mButtonShape.getCheckedRadioButtonId());
        mButtonShape.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateShape(checkedId);
            }
        });

        return v;
    }

    private void updateShape(int checkedId) {
        switch (checkedId) {
            case R.id.buttonRectangle:
                mShape = DrawableShape.RECTANGLE;
                break;
            case R.id.buttonTriangle:
                mShape = DrawableShape.TRIANGLE;
                break;
            case R.id.buttonCircle:
                mShape = DrawableShape.CIRCLE;
        }

        Log.d(TAG, "UpdatedShape checkedId: " + checkedId + " shape: " + mShape);
        if (mBoxView != null) {
            mBoxView.setDrawableShape(mShape);
        }
    }

    private void updateColor() {
        Log.d(TAG, "UpdatedColor color: " + mColor);
        if (mBoxView != null) {
            mBoxView.setDrawableColor(mColor);
        }
    }

}
