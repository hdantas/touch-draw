package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by nuno on 1/11/14.
 */
public class DrawingManager {
    private static final String TAG = DrawingManager.class.getSimpleName();

    private static final String PREF_CURRENT_DRAWING_ID = "DrawingManager.currentDrawingId";

    private DrawingDatabaseHelper mHelper;
    private long mCurrentDrawingId;

    private static DrawingManager sDrawingManager;
    private Context mAppContext;

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
        mHelper.updateDrawing(drawing);
        Log.d(TAG, "updateDrawing, updated drawing with id: " + drawing.getId());

    }

    public DrawingDatabaseHelper.DrawingCursor queryDrawings() {
        return mHelper.queryDrawings();
    }

    public ArrayList<Drawing> getAllDrawings() {
        ArrayList<Drawing> drawingArrayList = new ArrayList<Drawing>();
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
    public Drawing getDrawing(long id) {
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

    public void removeDrawing(long id) {
        mHelper.removeDrawing(id);
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

    public void removeAllBoxes () {
        removeAllBoxes(mCurrentDrawingId);
    }

    public void removeAllBoxes (long drawingId) {
        mHelper.removeAllBoxes(drawingId);
    }

    // Retrieve boxes of current drawing
    public ArrayList<Box> getBoxes() {
        Log.d(TAG, "getBoxes: id: " + mCurrentDrawingId + " size: " + getBoxes(mCurrentDrawingId).size());
        return getBoxes(mCurrentDrawingId);
    }

    // Retrieve boxes of an arbitrary drawing
    public ArrayList<Box> getBoxes(long drawingId) {
        ArrayList<Box> boxes = new ArrayList<Box>();

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


