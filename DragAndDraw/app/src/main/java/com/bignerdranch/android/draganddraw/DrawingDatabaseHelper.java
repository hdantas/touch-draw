package com.bignerdranch.android.draganddraw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Paint;
import android.graphics.PointF;

import java.util.Date;

/**
 * Created by nuno on 10/18/14.
 */
public class DrawingDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DrawingDatabaseHelper.class.getSimpleName();

    private static final String DB_NAME = "drawings.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_DRAWING = "drawing";
    private static final String COLUMN_DRAWING_ID = "_id";
    private static final String COLUMN_DRAWING_START_DATE = "start_date";

    private static final String TABLE_BOX = "box";
    private static final String COLUMN_BOX_ID = "drawing_id";
    private static final String COLUMN_BOX_ORIGIN_X = "origin_x";
    private static final String COLUMN_BOX_ORIGIN_Y = "origin_y";
    private static final String COLUMN_BOX_CURRENT_X = "current_x";
    private static final String COLUMN_BOX_CURRENT_Y = "current_y";
    private static final String COLUMN_BOX_COLOR = "color";
    private static final String COLUMN_BOX_SHAPE = "shape";

    public DrawingDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create the "drawing" table
        db.execSQL("create table " + TABLE_DRAWING + " (" +
                COLUMN_DRAWING_ID + " integer primary key autoincrement, " +
                COLUMN_DRAWING_START_DATE + " integer)");

        // Create the "box" table
        db.execSQL("create table " + TABLE_BOX + " (" +
                COLUMN_BOX_ID + " integer references " +
                        TABLE_DRAWING + "(" + COLUMN_DRAWING_ID + "), " +
                COLUMN_BOX_ORIGIN_X + " real, " +
                COLUMN_BOX_ORIGIN_Y + " real, " +
                COLUMN_BOX_CURRENT_X + " real, " +
                COLUMN_BOX_CURRENT_Y + " real, " +
                COLUMN_BOX_COLOR + " integer, " +
                COLUMN_BOX_SHAPE + " varchar(10))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implement schema changes and data massage here when upgrading
    }


    public long insertDrawing(Drawing drawing) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DRAWING_START_DATE, drawing.getStartDate().getTime());

        return getWritableDatabase().insert(TABLE_DRAWING, null, cv);
    }

    public long insertBox(long drawingId, Box box) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DRAWING_ID, drawingId);
        cv.put(COLUMN_BOX_ORIGIN_X, box.getOrigin().x);
        cv.put(COLUMN_BOX_ORIGIN_Y, box.getOrigin().y);
        cv.put(COLUMN_BOX_CURRENT_X, box.getCurrent().x);
        cv.put(COLUMN_BOX_CURRENT_Y, box.getCurrent().y);
        cv.put(COLUMN_BOX_COLOR, box.getPaint().getColor());
        cv.put(COLUMN_BOX_SHAPE, box.getShape().name());

        return getWritableDatabase().insert(TABLE_BOX, null, cv);
    }

    public DrawingCursor queryDrawings() {
        // Equivalent to "SELECT * FROM drawing ORDER BY start_time ASC"
        Cursor wrapped = getReadableDatabase().query(
                TABLE_DRAWING, // table
                null, // all columns
                null, // selection
                null, // selection args
                null, // group by
                null, // having
                COLUMN_DRAWING_ID + " asc" //order by
        );
        return new DrawingCursor(wrapped);
    }

    public DrawingCursor queryDrawing(long id) {
        Cursor wrapped = getReadableDatabase().query(
                TABLE_DRAWING,
                null, // All columns
                COLUMN_DRAWING_ID + " = ?", // Look for a drawingId
                new String[]{String.valueOf(id)}, //with this value
                null, // group by
                null, // having
                null, // order by
                "1"); // limit 1 row
        return new DrawingCursor(wrapped);
    }

    public BoxCursor queryBoxes(long id) {
        // Equivalent to "SELECT * FROM boxes WHERE drawing_id == id  ORDER BY start_time ASC"
        Cursor wrapped = getReadableDatabase().query(
                TABLE_BOX,
                null,  // All columns
                COLUMN_BOX_ID + " = ?", // Look for a boxId
                new String[]{String.valueOf(id)}, //with this value
                null, // group by
                null, // having
                COLUMN_DRAWING_ID + " asc" // order by
        );
        return new BoxCursor(wrapped);

    }


    /**
     * A convenience class to wrap a cursor that returns rows from the "drawing" table.
     * The {@link #getDrawing()} method will give you a Drawing instance representing
     * the current row
     */
    public static class DrawingCursor extends CursorWrapper {

        public DrawingCursor(Cursor c) {
            super(c);
        }

        /**
         * Returns a Drawing object configured for the current row,
         * or null if the current row is invalid.
         */
        public Drawing getDrawing() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            Drawing drawing = new Drawing();
            long drawingId = getLong(getColumnIndex(COLUMN_DRAWING_ID));
            drawing.setId(drawingId);
            long startDate = getLong(getColumnIndex(COLUMN_DRAWING_START_DATE));
            drawing.setStartDate(new Date(startDate));
            return drawing;
        }
    }

    public static class BoxCursor extends CursorWrapper {

        public BoxCursor(Cursor c) {
            super(c);
        }

        public Box getBox() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }

            PointF origin = new PointF(
                    getFloat(getColumnIndex(COLUMN_BOX_ORIGIN_X)),
                    getFloat(getColumnIndex(COLUMN_BOX_ORIGIN_Y))
            );
            PointF current = new PointF(
                    getFloat(getColumnIndex(COLUMN_BOX_CURRENT_X)),
                    getFloat(getColumnIndex(COLUMN_BOX_CURRENT_Y))
            );
            Paint paint = new Paint(getInt(getColumnIndex(COLUMN_BOX_COLOR)));
            DrawableShape shape = DrawableShape.valueOf(
                    getString(getColumnIndex(COLUMN_BOX_SHAPE))
            );
            return new Box(origin, current, shape, paint);
        }
    }
}
