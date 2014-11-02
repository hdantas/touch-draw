package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by nuno on 1/11/14.
 */
public class DrawingManager {
    private static final String TAG = DrawingManager.class.getSimpleName();

    private static final String PREFS_FILE = "drawings";
    private static final String PREF_CURRENT_DRAWING_ID = "DrawingManager.currentDrawingId";

    private DrawingDatabaseHelper mHelper;
    private SharedPreferences mPrefs;
    private long mCurrentDrawingId;

    private static DrawingManager sDrawingManager;
    private Context mAppContext;

    // The private constructor forces users to use DrawingManager.get(Context
    private DrawingManager(Context appContext) {
        mAppContext = appContext;
        mHelper = new DrawingDatabaseHelper(mAppContext);
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
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
        mPrefs.edit().putLong(PREF_CURRENT_DRAWING_ID, mCurrentDrawingId).apply();
        Log.d(TAG, "startNewDrawing: id " + mCurrentDrawingId);
        return insertDrawing();
    }

    private Drawing insertDrawing() {
        Drawing drawing = new Drawing();
        drawing.setId(mHelper.insertDrawing(drawing));
        return drawing;
    }

    public DrawingDatabaseHelper.DrawingCursor queryDrawings() {
        return mHelper.queryDrawings();
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

    public Drawing getLastDrawing() {
        mCurrentDrawingId = mPrefs.getLong(PREF_CURRENT_DRAWING_ID, -1);
        Log.d(TAG, "getLastDrawing id: " + mCurrentDrawingId);
        return getDrawing(mCurrentDrawingId);
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
        mHelper.removeAllBoxes(mCurrentDrawingId);
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


