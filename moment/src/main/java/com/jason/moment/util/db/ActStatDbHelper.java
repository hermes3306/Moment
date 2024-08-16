package com.jason.moment.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ActStatDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Moment.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + ActStatContract.LocEntry.TABLE_NAME + " (" +
                    ActStatContract.LocEntry._ID + " INTEGER PRIMARY KEY," +
                    ActStatContract.LocEntry.COLUMN_NAME_NAME + " TEXT," +
                    ActStatContract.LocEntry.COLUMN_NAME_DIST + " REAL," +
                    ActStatContract.LocEntry.COLUMN_NAME_DURATION + " INTEGER," +
                    ActStatContract.LocEntry.COLUMN_NAME_MINPKM + " REAL," +
                    ActStatContract.LocEntry.COLUMN_NAME_CAL + " INTEGER)";

    private static final String SQL_DELETE_ALL =
            "DELETE FROM " + ActStatContract.LocEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ActStatContract.LocEntry.TABLE_NAME;

    public ActStatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL("create index if not exists minperkm_inx on actstat(minpkm)");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("drop index minperkm_inx");
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public void deleteAll(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ALL);
    }
    public void createNew(SQLiteDatabase db) {

        try {
            db.execSQL("drop index if exists minperkm_inx");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e("ActStatDbHelper", "-- ERR:" + sw.toString());
        }

        try {
            db.execSQL(SQL_DELETE_ENTRIES);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e("ActStatDbHelper", "-- ERR:" + sw.toString());
        }

        try {
            onCreate(db);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e("ActStatDbHelper", "-- ERR:" + sw.toString());
        }
    }

}
