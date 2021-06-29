package com.jason.quote.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyLocDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Moment.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + MyLocContract.LocEntry.TABLE_NAME + " (" +
                    MyLocContract.LocEntry._ID + " INTEGER PRIMARY KEY," +
                    MyLocContract.LocEntry.COLUMN_NAME_LATITUDE + " TEXT," +
                    MyLocContract.LocEntry.COLUMN_NAME_LONGITUDE + " TEXT," +
                    MyLocContract.LocEntry.COLUMN_NAME_CRDATE + " TEXT," +
                    MyLocContract.LocEntry.COLUMN_NAME_CRTIME + " TEXT)";

    private static final String SQL_DELETE_ALL =
            "DELETE FROM " + MyLocContract.LocEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MyLocContract.LocEntry.TABLE_NAME;

    public MyLocDbHelper(Context context) {
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