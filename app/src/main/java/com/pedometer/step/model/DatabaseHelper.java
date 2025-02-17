package com.pedometer.step.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pedometer.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "steps";

    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_STEPS = "steps";
    private static final String COLUMN_GOAL = "goal";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_DATE + " TEXT PRIMARY KEY, "
                + COLUMN_STEPS + " INTEGER, "
                + COLUMN_GOAL + " INTEGER, "
                + COLUMN_CALORIES + " REAL, "
                + COLUMN_DISTANCE + " REAL, "
                + COLUMN_TIME + " INTEGER);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void saveStepData(int steps, int goal, double calories, double distance, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DATE, getCurrentDate());
        values.put(COLUMN_STEPS, steps);
        values.put(COLUMN_GOAL, goal);
        values.put(COLUMN_CALORIES, calories);
        values.put(COLUMN_DISTANCE, distance);
        values.put(COLUMN_TIME, time);

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public StepData getStepDataForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        StepData stepData = new StepData();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_STEPS, COLUMN_GOAL, COLUMN_CALORIES, COLUMN_DISTANCE, COLUMN_TIME},
                COLUMN_DATE + "=?",
                new String[]{date},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            stepData.steps = cursor.getInt(0);
            stepData.goal = cursor.getInt(1);
            stepData.calories = cursor.getDouble(2);
            stepData.distance = cursor.getDouble(3);
            stepData.time = cursor.getLong(4);
            cursor.close();
        }

        db.close();
        return stepData;
    }

    public StepData getTodayStepData() {
        return getStepDataForDate(getCurrentDate());
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static class StepData {
        public int steps = 0;
        public int goal = 6000;
        public double calories = 0;
        public double distance = 0;
        public long time = 0;
    }
}
