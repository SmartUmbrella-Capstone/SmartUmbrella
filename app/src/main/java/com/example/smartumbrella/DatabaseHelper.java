package com.example.smartumbrella;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartUmbrella.db";
    private static final int DATABASE_VERSION = 3; // 버전 업데이트

    // UserSetting 테이블
    private static final String TABLE_USER_SETTING = "UserSetting";
    private static final String CREATE_TABLE_USER_SETTING = "CREATE TABLE " + TABLE_USER_SETTING + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "update_alert INTEGER, " +
            "priority_alert INTEGER, " +
            "distance INTEGER, " +
            "volume INTEGER DEFAULT 50, " + // 기본값 포함
            "interval TEXT, " +
            "location_enable INTEGER DEFAULT 0" +
            ");";

    // LocationLog 테이블
    private static final String TABLE_LOCATION_LOG = "LocationLog";
    private static final String CREATE_TABLE_LOCATION_LOG = "CREATE TABLE " + TABLE_LOCATION_LOG + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "timestamp TEXT NOT NULL, " +
            "latitude REAL NOT NULL, " +
            "longitude REAL NOT NULL" +
            ");";

    // BatteryStatus 테이블
    private static final String TABLE_BATTERY_STATUS = "BatteryStatus";
    private static final String CREATE_TABLE_BATTERY_STATUS = "CREATE TABLE " + TABLE_BATTERY_STATUS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "timestamp TEXT NOT NULL, " +
            "battery_level INTEGER NOT NULL" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER_SETTING);
        db.execSQL(CREATE_TABLE_LOCATION_LOG);
        db.execSQL(CREATE_TABLE_BATTERY_STATUS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 기존 테이블에 새 컬럼 추가
            db.execSQL("ALTER TABLE UserSetting ADD COLUMN volume INTEGER DEFAULT 50;");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE UserSetting ADD COLUMN location_enable INTEGER DEFAULT 0;");
        }
    }

    // DatabaseHelper.java
    public Cursor getUserSettings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("UserSetting", null, null, null, null, null, null);
    }

    public void saveUserSettings(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.insertWithOnConflict("UserSetting", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
}
