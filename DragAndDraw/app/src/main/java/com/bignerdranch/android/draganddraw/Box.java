package com.bignerdranch.android.draganddraw;

import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nuno on 10/16/14.
 */
public class Box implements Parcelable{

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

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
