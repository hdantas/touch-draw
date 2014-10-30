package com.bignerdranch.android.draganddraw;

import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Created by nuno on 10/16/14.
 */
public class Box {

    private PointF mOrigin;
    private PointF mCurrent;
    private DrawableShape mShape;
    private Paint mPaint;

    public Box(PointF origin, DrawableShape shape, Paint paint) {
        mOrigin = mCurrent = origin;
        mShape = shape;
        mPaint = paint;
        mPaint.setAntiAlias(true);
    }

    public PointF getCurrent() {
        return mCurrent;
    }

    public PointF getOrigin() {
        return mOrigin;
    }

    public void setCurrent(PointF current) {
        mCurrent = current;
    }

    public DrawableShape getShape() {
        return mShape;
    }

    public Paint getPaint() {
        return mPaint;
    }
}
