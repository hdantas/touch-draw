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

        // The actual color will be chosen later
        mBoxPaint = new Paint();

        // Paint the background off-white
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getResources().getColor(R.color.white));

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
                    // TODO
                    break;
                case CIRCLE:
                    float x = box.getCurrent().x - box.getOrigin().x;
                    float y = box.getCurrent().y - box.getOrigin().y;
                    float radius = (float) Math.sqrt(x * x + y * y);
                    canvas.drawCircle(box.getOrigin().x, box.getOrigin().y, radius, box.getPaint());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF curr = new PointF(event.getX(), event.getY());

        Log.i(TAG, "Received event at x=" + curr.x + ", y=" + curr.y + ":");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, " ACTION_DOWN");
                // Reset drawing state
                mCurrentBox = new Box(curr, mDrawableShape, new Paint(mBoxPaint));
                mBoxes.add(mCurrentBox);
                break;

            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, " ACTION_MOVE");
                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(curr);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                Log.i(TAG, " ACTION_UP");
                mCurrentBox = null;
                break;

            case MotionEvent.ACTION_CANCEL:
                Log.i(TAG, " ACTION_CANCEL");
                mCurrentBox = null;
                break;
        }

        return true;
    }

    public void setDrawableShape(DrawableShape drawableShape) {
        mDrawableShape = drawableShape;
        Log.d(TAG, "received shape: " + mDrawableShape);
    }

    public void setDrawableColor(DrawableColor drawableColor) {
        int alpha = 0x99 << 24; // (0x99 := 0xFF * 0.6, 60% translucent (40% opaque)

        switch (drawableColor) {
            case RED:
                mBoxPaint.setColor(getResources().getColor(R.color.red) - alpha);
                break;
            case GREEN:
                mBoxPaint.setColor(getResources().getColor(R.color.green) - alpha);
                break;
            case BLUE:
                mBoxPaint.setColor(getResources().getColor(R.color.blue) - alpha);
        }
        Log.d(TAG, String.format("received color: %s\tpaint: 0x%8s",
                        drawableColor.toString(),
                        Integer.toHexString(mBoxPaint.getColor()).replace(' ', '0')));
    }
}
