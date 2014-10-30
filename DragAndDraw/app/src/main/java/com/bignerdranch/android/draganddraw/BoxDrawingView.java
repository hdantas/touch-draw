package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by nuno on 10/16/14.
 */
public class BoxDrawingView extends View {
    private static final String TAG = BoxDrawingView.class.getSimpleName();

    private ArrayList<Box> mBoxes = new ArrayList<Box>();
    private Box mCurrentBox;
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;
    private DrawableShape mDrawableShape = DrawableShape.RECTANGLE;

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
                    // TODO implement drawing triangles
                    break;
                case CIRCLE: // TODO improve resolution circles, they look pixelated
                    double x = box.getCurrent().x - box.getOrigin().x;
                    double y = box.getCurrent().y - box.getOrigin().y;
                    double radius = Math.sqrt(x * x + y * y);
                    canvas.drawCircle(box.getOrigin().x, box.getOrigin().y, (float) radius, box.getPaint());
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
        int alphaOffset = ((0xFF * alpha) / 100) << 24; // (e.g. 0x99 := 0xFF * 0.6, 60% translucent (40% opaque)
        mBoxPaint.setColor(getResources().getColor(drawableColor) - alphaOffset);

        Log.d(TAG, String.format("received color: %s\tpaint: 0x%8s\talpha: %d",
                DrawableColor.toString(drawableColor),
                Integer.toHexString(mBoxPaint.getColor()).replace(' ', '0'),
                alpha));
    }
}
