package net.henriquedantas.android.touchanddraw;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by hdantas on 1/11/14.
 * Model class that encapsulates drawing objects. Each drawing is composed by a list of boxes.
 */
public class Drawing {

    private static final String fileFormat = "png";

    private long mId;
    private Date mStartDate; //when the drawing was initiate
    private ArrayList<Box> mBoxes;
    private String mFilename;
    private Uri mDrawingUri;

    public Drawing() {
        mId = -1;
        mBoxes = new ArrayList<>();
        mStartDate = new Date();
        mFilename = UUID.randomUUID().toString() + "" + fileFormat;

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

    public String getSimpleFilename() {
            return mFilename.replace("" + fileFormat, "");
    }

    public Uri getUri(Context context) {
        return Uri.fromFile(new File(context.getFilesDir(), mFilename));
    }

    public String getFileFormat() {
        return fileFormat.toUpperCase();
    }

    public Uri getDrawingUri() {
        return mDrawingUri;
    }

    public void setDrawingUri(Uri drawingUri) {
        mDrawingUri = drawingUri;
    }
}
