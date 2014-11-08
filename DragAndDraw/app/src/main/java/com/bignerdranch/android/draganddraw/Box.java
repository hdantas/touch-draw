package com.bignerdranch.android.draganddraw;

import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Created by nuno on 10/16/14.
 */
public class Box{

    private long mId;
    private int mOrder; // order of the box in the drawing
    private PointF mOrigin;
    private PointF mCurrent;
    private DrawableShape mShape;
    private Paint mPaint;

    public Box(int order, PointF origin, DrawableShape shape, Paint paint) {
        this(order, origin, origin, shape, paint);
    }

    public Box(int order, PointF origin, PointF current, DrawableShape shape, Paint paint) {
        mId = -1;
        mOrder = order;
        mOrigin = origin;
        mCurrent = current;
        mShape = shape;
        mPaint = paint;
        mPaint.setAntiAlias(true);
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
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

    public int getOrder() {
        return mOrder;
    }
}
