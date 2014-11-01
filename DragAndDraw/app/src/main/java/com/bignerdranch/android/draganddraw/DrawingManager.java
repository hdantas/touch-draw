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

    public DrawingManager(Context mAppContext) {
        mHelper = new DrawingDatabaseHelper(mAppContext);
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentDrawingId = mPrefs.getLong(PREF_CURRENT_DRAWING_ID, -1);
    }

    public Drawing startNewDrawing() {
        // Insert a drawing into the db
        return insertDrawing();
    }

    public void stopDrawing() {
        mCurrentDrawingId = -1;
        mPrefs.edit().remove(PREF_CURRENT_DRAWING_ID).apply();
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

    public void insertBox(Box box) {
        if (mCurrentDrawingId != -1) {
            mHelper.insertBox(mCurrentDrawingId, box);
        } else {
            Log.e(TAG, "Box received with no associated drawing.");
        }
    }

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
            Log.e(TAG, "Box received with no associated drawing.");
        }
        return boxes;
    }
}


