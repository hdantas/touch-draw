package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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

    private DrawingManager mDrawingManager;
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
                    break;

                default:
                    Log.e(TAG, "Unrecognized shape " + box.getShape(), new Exception());

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
                mDrawingManager.insertBox(mCurrentBox);
                Log.d(TAG, "onTouchEvent: add box! total: " + mDrawingManager.getBoxes().size());
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
//        Log.d(TAG, "setDrawableShape shape: " + mDrawableShape);
    }

    public void setDrawableColor(int drawableColor, int alpha) {
        int alphaOffset = (0xFF - alpha) << 24;
        mBoxPaint.setColor(getResources().getColor(drawableColor) - alphaOffset);

//        Log.d(TAG, String.format("recsetDrawableColor color: %s\tpaint: 0x%8s\talpha: %d",
//                DrawableColor.toString(drawableColor),
//                Integer.toHexString(mBoxPaint.getColor()).replace(' ', '0'),
//                alpha));
    }

    public void clearBoxes() {
        mBoxes.clear();
        mDrawingManager.removeAllBoxes();
        invalidate();
    }

    public void setDrawingManager(DrawingManager drawingManager) {
        mDrawingManager = drawingManager;
    }

    public void loadBoxes() {
        if (mDrawingManager != null) {
            mBoxes = mDrawingManager.getBoxes();
            Log.d(TAG, "loadBoxes: size " + mBoxes.size());
            invalidate();
        }
    }



}
