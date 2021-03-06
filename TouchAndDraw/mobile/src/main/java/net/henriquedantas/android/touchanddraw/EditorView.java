package net.henriquedantas.android.touchanddraw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by hdantas on 10/16/14.
 * Custom View that allows drawing boxes. It handles touch events and interacts with the toolbar
 * that changes the attributes of the boxes (ie color, shape and translucency).
 */
public class EditorView extends View {

    private static final String TAG = EditorView.class.getSimpleName();

    private DrawingManager mDrawingManager;
    private ArrayList<Box> mBoxes = new ArrayList<>();
    private Box mCurrentBox;
    private final Paint mBoxPaint;
    private final Paint mBackgroundPaint;
    private DrawableShape mDrawableShape = DrawableShape.RECTANGLE;
    private final Toast mToast;
    private final Path mPath; // to draw triangles
    private LinearLayout mToolbar;
    private int[] mToolbarOriginalCoordinates;
    private final EditorFragment mDetailFragment;

    // Used when creating the view in code
    public EditorView(Context context) {
        this(context, null);
    }


    // Used when inflating the view from XML
    @SuppressLint("ShowToast")
    public EditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ActionBarActivity activity = (ActionBarActivity) context;
        mDetailFragment = (EditorFragment)
                activity.getSupportFragmentManager().findFragmentById(R.id.detailFragmentContainer);
        mBoxPaint = new Paint();
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getResources().getColor(DrawableColor.BACKGROUND_COLOR));
        mPath = new Path(); // Path is used to draw triangles
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
//        onDrawingUpdated();
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
                    Log.e(TAG, "Unrecognized shape " + box.getShape());

            }
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
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
                onDrawingUpdated();
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
            mToast.setText(getResources().getString(R.string.undo_no_boxes));
            mToast.show();
            return;
        }

        long boxId = mBoxes.size() - 1;
        mDrawingManager.removeBox(boxId);
        mBoxes.remove((int) boxId); // remove last box
        onDrawingUpdated();
        mToast.setText(String.format(getResources().getString(R.string.undo_box), boxId));
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

    public void setToolbar(LinearLayout toolbar) {
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
        // convert DP to PX
        float thresholdPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, threshold, getResources().getDisplayMetrics());

        int left = mToolbarOriginalCoordinates[0];
        int top = mToolbarOriginalCoordinates[1];
        int right = mToolbarOriginalCoordinates[2];
        int bottom = mToolbarOriginalCoordinates[3];

        if (top <= (touchY + thresholdPx)) {
            top = (int) (touchY + thresholdPx);
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

    public void onDrawingUpdated() {
        if (mDetailFragment != null) {
            // This is only necessary with 2 panes
            mDetailFragment.onDrawingUpdated();
        }
    }
}
