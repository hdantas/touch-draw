package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

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
    private Toast mToast;
    private Path mPath; // to draw triangles
    private Toolbar mToolbar;
    private boolean mCanToolbarSlideUp = false;
    private int[] mToolbarOriginalCoordinates;

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
        mPath = new Path(); // Path is used to draw triangles
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);

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
                    mPath.reset();
                    mPath.moveTo(box.getOrigin().x, box.getOrigin().y);
                    mPath.lineTo(box.getCurrent().x, box.getCurrent().y);
                    mPath.lineTo(2 * box.getCurrent().x - box.getOrigin().x, box.getOrigin().y);
                    mPath.close();

                    canvas.drawPath(mPath, box.getPaint());
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

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Reset drawing state
                mCurrentBox = new Box(mBoxes.size(), curr, mDrawableShape, new Paint(mBoxPaint));
                mBoxes.add(mCurrentBox);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(curr);
                    invalidate();
                }
                updateToolbarPosition(event.getY());
                break;

            case MotionEvent.ACTION_UP:
                mDrawingManager.insertBox(mCurrentBox);
                Log.d(TAG, "onTouchEvent: add box! total: " + mDrawingManager.getBoxes().size());
                mCurrentBox = null;
                resetToolbarPosition();
                break;

            case MotionEvent.ACTION_CANCEL:
                mCurrentBox = null;
                resetToolbarPosition();
                break;
        }
        return true;
    }

    public void setDrawableShape(DrawableShape drawableShape) {
        mDrawableShape = drawableShape;
    }

    public void setDrawableColor(int drawableColor, int alpha) {
        int alphaOffset = (0xFF - alpha) << 24;
        mBoxPaint.setColor(getResources().getColor(drawableColor) - alphaOffset);
    }

    public void undoLastBox() {
        if (mBoxes.size() < 1) {
            mToast.setText("There are no boxes");
            mToast.show();
            return;
        }

        long boxId = mBoxes.size() - 1;
        mDrawingManager.removeBox(boxId);
        mBoxes.remove((int) boxId); // remove last box
        mToast.setText("Removed box " + boxId);
        mToast.show();
        invalidate();
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

    public void setToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
    }


    private void updateToolbarPosition(float touchY) {

        if (mToolbarOriginalCoordinates == null) {
            mToolbarOriginalCoordinates = new int[]{
                    mToolbar.getLeft(),
                    mToolbar.getTop(),
                    mToolbar.getRight(),
                    mToolbar.getBottom()
            };
        }

        int threshold = 30;

        int left = mToolbarOriginalCoordinates[0];
        int top = mToolbarOriginalCoordinates[1];
        int right = mToolbarOriginalCoordinates[2];
        int bottom = mToolbarOriginalCoordinates[3];


        if (top <= (touchY + threshold)) {
            top = (int) (touchY + threshold);
            mToolbar.layout(left, top, right, bottom);
        }

    }

    private void resetToolbarPosition() {

        if (mToolbarOriginalCoordinates == null) {
            mToolbarOriginalCoordinates = new int[]{
                    mToolbar.getLeft(),
                    mToolbar.getTop(),
                    mToolbar.getRight(),
                    mToolbar.getBottom()
            };

        }

        if (mToolbarOriginalCoordinates[1] < mToolbar.getTop()) {
            Log.d(TAG, "resetToolbarPosition");

            float originalHeight = mToolbarOriginalCoordinates[3] - mToolbarOriginalCoordinates[1];
            float deltaY = originalHeight - mToolbar.getHeight();
            final Animation animation = new TranslateAnimation(0.0f, 0.0f, deltaY, 0.0f);
            animation.setInterpolator(getContext(), android.R.anim.accelerate_decelerate_interpolator);
            animation.setDuration(250);
            mToolbar.startAnimation(animation);

            // Necessary to restore the original height of toolbar, otherwise it will be clipped
            mToolbar.layout(
                    mToolbarOriginalCoordinates[0],
                    mToolbarOriginalCoordinates[1],
                    mToolbarOriginalCoordinates[2],
                    mToolbarOriginalCoordinates[3]
            );
        }
    }
}
