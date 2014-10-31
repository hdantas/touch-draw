package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by nuno on 10/16/14.
 */
public class BoxDrawingView extends View {

    private static final String TAG = BoxDrawingView.class.getSimpleName();

    private static final String EXTRA_STATE = "extra_state";
    private static final String EXTRA_BOXES = "extra_boxes";

    private ArrayList<Box> mBoxes = new ArrayList<Box>();
    private Box mCurrentBox;
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;
    private DrawableShape mDrawableShape = DrawableShape.RECTANGLE;

    private Path path; // to draw triangles

    // Used when creating the view in code
    public BoxDrawingView(Context context) {
        this(context, null);
    }

    // Used when inflating the view from XML
    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBoxPaint = new Paint();
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getResources().getColor(DrawableColor.BACKGROUND_COLOR));

        path = new Path(); // Path is used to draw triangles

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Fill the background
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxes) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            switch (box.getShape()) {
                case RECTANGLE:
                    canvas.drawRect(left, top, right, bottom, box.getPaint());
                    break;

                case TRIANGLE:
                    path.reset();
                    path.moveTo(box.getOrigin().x, box.getOrigin().y);
                    path.lineTo(box.getCurrent().x, box.getCurrent().y);
                    path.lineTo(2 * box.getCurrent().x - box.getOrigin().x, box.getOrigin().y);
                    path.close();

                    canvas.drawPath(path, box.getPaint());
                    break;

                case CIRCLE:
                    float x_circle = box.getCurrent().x - box.getOrigin().x;
                    float y_circle = box.getCurrent().y - box.getOrigin().y;
                    float radius = (float) Math.sqrt(x_circle * x_circle + y_circle * y_circle);
                    canvas.drawCircle(box.getOrigin().x, box.getOrigin().y, radius, box.getPaint());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF curr = new PointF(event.getX(), event.getY());

//        Log.d(TAG, "Received event at x=" + curr.x + ", y=" + curr.y + ":");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Reset drawing state
                mCurrentBox = new Box(curr, mDrawableShape, new Paint(mBoxPaint));
                mBoxes.add(mCurrentBox);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(curr);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                mCurrentBox = null;
                break;

            case MotionEvent.ACTION_CANCEL:
                mCurrentBox = null;
                break;
        }
        return true;
    }

    public void setDrawableShape(DrawableShape drawableShape) {
        mDrawableShape = drawableShape;
        Log.d(TAG, "received shape: " + mDrawableShape);
    }

    public void setDrawableColor(int drawableColor, int alpha) {
        int alphaOffset = (0xFF - alpha) << 24;
        mBoxPaint.setColor(getResources().getColor(drawableColor) - alphaOffset);

        Log.d(TAG, String.format("received color: %s\tpaint: 0x%8s\talpha: %d",
                DrawableColor.toString(drawableColor),
                Integer.toHexString(mBoxPaint.getColor()).replace(' ', '0'),
                alpha));
    }

    public void clearBoxes() {
        mBoxes = new ArrayList<Box>();
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d(TAG, "onSaveInstance state");

        Parcelable savedState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_STATE, savedState);
        Log.d(TAG, "onSaveInstance savedState");
        bundle.putSerializable(EXTRA_BOXES, mBoxes);
        Log.d(TAG, "onSaveInstance mBoxes");

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.d(TAG, "onRestoreInstanceState");
        Parcelable restoredState = ((Bundle) state).getParcelable(EXTRA_STATE);
        Serializable serializable = ((Bundle) state).getSerializable(EXTRA_BOXES);

        if (serializable instanceof ArrayList) {
            mBoxes = (ArrayList<Box>) serializable;
        }

        super.onRestoreInstanceState(restoredState);
    }

}
