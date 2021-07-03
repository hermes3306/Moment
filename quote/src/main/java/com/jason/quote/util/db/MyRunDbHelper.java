package com.jason.quote.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyRunDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Quote.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + MyRunContract.E.TAB_NAME + " (" +
                    MyRunContract.E._ID + " INTEGER PRIMARY KEY," +
                    MyRunContract.E.COL_RUN + " LONG," +
                    MyRunContract.E.COL_LAT + " TEXT," +
                    MyRunContract.E.COL_LON + " TEXT," +
                    MyRunContract.E.COL_ALT + " TEXT," +
                    MyRunContract.E.COL_DATE + " TEXT)";

    private static final String SQL_DELETE_ALL =
            "DELETE FROM " + MyRunContract.E.TAB_NAME;

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MyRunContract.E.TAB_NAME;

    public MyRunDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public void deleteAll(SQLiteDatabase db) {db.execSQL(SQL_DELETE_ALL);}
    public void createNew(SQLiteDatabase db) {db.execSQL(SQL_DELETE_ENTRIES); onCreate(db);}



}