package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by nuno on 1/11/14.
 */
public class Drawing {

    private long mId;
    private Date mStartDate; //when the drawing was initiate
    private ArrayList<Box> mBoxes;
    private String mFilename;

    public Drawing() {
        mId = -1;
        mBoxes = new ArrayList<Box>();
        mStartDate = new Date();
        mFilename = UUID.randomUUID().toString() + ".png";

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

    public String getFilename() {
        return mFilename;
    }

    public void setFilename(String filename) {
        mFilename = filename;
    }

    public Uri getUri(Context context) {
        return Uri.fromFile(new File(context.getFilesDir(), mFilename));
    }


}
