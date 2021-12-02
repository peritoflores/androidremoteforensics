package com.peritoflores.androidremoteforensics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    static final String TAG = "DatabaseHelper";


    public static final String DATABASE_NAME = "locationhistory.db";
    public static final String TABLE_NAME = "location_history";
    public static final String COL1 = "time";
    public static final String COL2 = "latitude";
    public static final String COL3 = "longitude";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String stat = "create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TIME LONG, LATITUDE FLOAT,LONGITUDE FLOAT)";
        db.execSQL(stat);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getLocationHistory() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public boolean saveLocation(java.util.Date currenttime, Location loc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, currenttime.getTime());
        contentValues.put(COL2, loc.getLatitude());
        contentValues.put(COL3, loc.getLongitude());
        long insertedRows = db.insert(TABLE_NAME, null, contentValues);

        Log.d(TAG, "inserted values data into sqlite " + insertedRows);

        return (insertedRows != -1);
    }
}
