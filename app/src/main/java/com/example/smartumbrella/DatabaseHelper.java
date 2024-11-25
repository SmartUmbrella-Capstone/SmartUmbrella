package com.example.smartumbrella;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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

    // UserSetting 데이터 가져오기
    public Cursor getUserSettings() {
        SQLiteDatabase db = this.getReadableDatabase();
        // 가장 최근 데이터 1건 가져오기
        return db.query(
                TABLE_USER_SETTING,       // 테이블 이름
                null,                     // 모든 컬럼 선택
                null,                     // WHERE 조건 없음
                null,                     // WHERE 조건 값 없음
                null,                     // GROUP BY 없음
                null,                     // HAVING 없음
                "id DESC",                // id 기준 내림차순 정렬
                "1"                       // 결과 1건만 반환
        );
    }

    public void saveUserSettings(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.insertWithOnConflict(TABLE_USER_SETTING, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // 거리 설정 가져오기
    public int getDistanceSetting() {
        int distance = 3;  // 기본값을 3미터로 설정
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT distance FROM " + TABLE_USER_SETTING + " ORDER BY id DESC LIMIT 1", null);

        if (cursor != null && cursor.moveToFirst()) {
            distance = cursor.getInt(cursor.getColumnIndexOrThrow("distance"));
            Log.d("DB", "데이터베이스에서 가져온 거리 설정 값: " + distance);
        } else {
            Log.d("DB", "설정 값이 없거나 데이터베이스에서 값을 가져오는 데 실패했습니다.");
        }
        if (cursor != null) {
            cursor.close();
        }
        return distance;
    }

    // LocationLog 데이터 가져오기
    public Cursor getLocationLogs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_LOCATION_LOG, null, null, null, null, null, "id DESC", "10");
    }

    public void insertLocationLog(String timestamp, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("timestamp", timestamp);
        values.put("latitude", latitude);
        values.put("longitude", longitude);

        db.insert(TABLE_LOCATION_LOG, null, values);
        db.close();
    }
}