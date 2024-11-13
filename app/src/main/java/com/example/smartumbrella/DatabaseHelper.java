package com.example.smartumbrella;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserSettings.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "settings";

    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_UPDATE_ALERT = "updateAlert";
    public static final String COLUMN_PRIORITY_ALERT = "priorityAlert";
    public static final String COLUMN_LOCATION_LOG = "locationLog";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_VOLUME = "volume";
    public static final String COLUMN_INTERVAL = "interval";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_UPDATE_ALERT + " INTEGER, " +
                COLUMN_PRIORITY_ALERT + " INTEGER, " +
                COLUMN_LOCATION_LOG + " INTEGER, " +
                COLUMN_DISTANCE + " INTEGER, " +
                COLUMN_VOLUME + " INTEGER, " +
                COLUMN_INTERVAL + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Save settings to the database
    public void saveSettings(boolean updateAlert, boolean priorityAlert, boolean locationLog, int distance, int volume, String interval) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UPDATE_ALERT, updateAlert ? 1 : 0);
        values.put(COLUMN_PRIORITY_ALERT, priorityAlert ? 1 : 0);
        values.put(COLUMN_LOCATION_LOG, locationLog ? 1 : 0);
        values.put(COLUMN_DISTANCE, distance);
        values.put(COLUMN_VOLUME, volume);
        values.put(COLUMN_INTERVAL, interval);

        // Insert or update row
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Retrieve the latest settings from the database
    public Cursor getSettings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1", null);
    }
}
