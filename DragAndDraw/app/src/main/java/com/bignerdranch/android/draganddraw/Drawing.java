package com.bignerdranch.android.draganddraw;

import java.util.Date;
import java.util.ArrayList;

/**
 * Created by nuno on 1/11/14.
 */
public class Drawing {

    private long mId;
    private Date mStartDate; //when the drawing was initiate
    private ArrayList<Box> mBoxes;

    public Drawing() {
        mId = -1;
        mBoxes = new ArrayList<Box>();
        mStartDate = new Date();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public ArrayList<Box> getBoxes() {
        return mBoxes;
    }

    public void setBoxes(ArrayList<Box> boxes) {
        mBoxes = boxes;
    }
}
