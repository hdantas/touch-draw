package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by hdantas on 1/11/14.
 * Controller class to allow easier access to drawing objects in the DB.
 */
public class DrawingManager {
    private static final String TAG = DrawingManager.class.getSimpleName();

    private final DrawingDatabaseHelper mHelper;
    private long mCurrentDrawingId;

    private static DrawingManager sDrawingManager;
    private final Context mAppContext;

    // The private constructor forces users to use DrawingManager.get(Context
    private DrawingManager(Context appContext) {
        mAppContext = appContext;
        mHelper = new DrawingDatabaseHelper(mAppContext);
        mCurrentDrawingId = -1;
    }

    public static DrawingManager get(Context c) {
        if (sDrawingManager == null) {
            // Use the application context to avoid leaking activities
            sDrawingManager = new DrawingManager(c.getApplicationContext());
        }
        return sDrawingManager;
    }

    public Drawing startNewDrawing() {
        // Insert a drawing into the db
        Drawing drawing = insertDrawing();
        mCurrentDrawingId = drawing.getId();
        Log.d(TAG, "startNewDrawing: id " + mCurrentDrawingId);
        return drawing;
    }

    private Drawing insertDrawing() {
        Drawing drawing = new Drawing();
        drawing.setId(mHelper.insertDrawing(drawing));
        return drawing;
    }

    public void updateDrawing(Drawing drawing) {
        // update Date
        drawing.setStartDate(new Date());
        mHelper.updateDrawing(drawing);
        Log.d(TAG, "updateDrawing, updated drawing with id: " + drawing.getId());

    }

    DrawingDatabaseHelper.DrawingCursor queryDrawings() {
        return mHelper.queryDrawings();
    }

    public ArrayList<Drawing> getAllDrawings() {
        ArrayList<Drawing> drawingArrayList = new ArrayList<>();
        DrawingDatabaseHelper.DrawingCursor cursor = queryDrawings();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            drawingArrayList.add(cursor.getDrawing());
            cursor.moveToNext();
        }
        Log.d(TAG, "getAllDrawings queried " + drawingArrayList.size() + " drawings.");
        return drawingArrayList;
    }

    public Drawing loadDrawing(long id) {
        mCurrentDrawingId = id;
        Log.d(TAG, "loadDrawing with id " + id);
        return getDrawing(id);
    }
    Drawing getDrawing(long id) {
        Drawing drawing = null;
        DrawingDatabaseHelper.DrawingCursor cursor = mHelper.queryDrawing(id);
        cursor.moveToFirst();
        // If you got a row, get a drawing
        if (!cursor.isAfterLast()) {
            drawing = cursor.getDrawing();
        }
        cursor.close();
        return drawing;
    }

    public void removeDrawing(Drawing drawing) {
        removeAllBoxes(drawing.getId());
        mAppContext.deleteFile(drawing.getFilename());
        mHelper.removeDrawing(drawing.getId());
    }

    public void insertBox(Box box) {
        if (mCurrentDrawingId != -1) {
            mHelper.insertBox(mCurrentDrawingId, box);
        } else {
            Log.e(TAG, "Box received with no associated drawing.", new Exception());
        }
    }

    public void insertBoxes (ArrayList<Box> boxes) {
        for (Box box : boxes) {
            insertBox(box);
        }
    }

    public void removeBox(long boxOrder) {
        mHelper.removeBox(boxOrder, mCurrentDrawingId);
    }

    public void removeBox(long boxOrder, long drawingId) {
        mHelper.removeBox(boxOrder, drawingId);
    }

    public void removeAllBoxes () {
        removeAllBoxes(mCurrentDrawingId);
    }

    void removeAllBoxes(long drawingId) {
        mHelper.removeAllBoxes(drawingId);
    }

    // Retrieve boxes of current drawing
    public ArrayList<Box> getBoxes() {
        Log.d(TAG, "getBoxes: id: " + mCurrentDrawingId + " size: " + getBoxes(mCurrentDrawingId).size());
        return getBoxes(mCurrentDrawingId);
    }

    // Retrieve boxes of an arbitrary drawing
    ArrayList<Box> getBoxes(long drawingId) {
        ArrayList<Box> boxes = new ArrayList<>();

        if (drawingId != -1) {
            DrawingDatabaseHelper.BoxCursor cursor = mHelper.queryBoxes(drawingId);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                boxes.add(cursor.getBox());
                cursor.moveToNext();
            }
            cursor.close();
        } else {
            Log.e(TAG, "Box requested with no associated drawing. ",  new Exception());
        }
        return boxes;
    }
}


