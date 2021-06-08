package com.jason.moment.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.jason.moment.util.ActivitySummary;
import com.jason.moment.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyActiviySummary {
    private static final String TAG = "MyActiviySummary";
    private final Context ctx;
    private static MyActiviySummary instance=null;
    public static MyActiviySummary getInstance(Context ctx) {
        if(instance==null) instance = new MyActiviySummary(ctx);
        return instance;
    }
    private static ActStatDbHelper dbHelper = null;
    private static SQLiteDatabase db = null;
    private static SQLiteDatabase dbr = null;

    public MyActiviySummary(Context ctx) {
        this.ctx = ctx;
        if(dbHelper == null) dbHelper = new ActStatDbHelper(ctx);
        if(db == null) db = dbHelper.getWritableDatabase();
        if(dbr == null) dbr = dbHelper.getReadableDatabase();
    }

    public void createNew() {
        dbHelper.createNew(db);
    }

    public void deleteAll() {
        dbHelper.deleteAll(db);
    }

    public void ins(String name, double dist, long duration, double minpk, int cal) {
        ContentValues values = new ContentValues();
        values.put(ActStatContract.LocEntry.COLUMN_NAME_NAME,       name);
        values.put(ActStatContract.LocEntry.COLUMN_NAME_DIST,       dist);
        values.put(ActStatContract.LocEntry.COLUMN_NAME_DURATION,   duration);
        values.put(ActStatContract.LocEntry.COLUMN_NAME_MINPKM,     minpk);
        values.put(ActStatContract.LocEntry.COLUMN_NAME_CAL,        cal);
        long newRowId = db.insert(ActStatContract.LocEntry.TABLE_NAME, null, values);
        Log.d(TAG, "-- ActStat - db.insert with id:" + newRowId);
    }

    public ArrayList<ActivitySummary> query(String selection,
                                            String[] selectionArgs,
                                            String order_by) {
        Cursor cursor = null;
        try {
            cursor = dbr.query(
                    ActStatContract.LocEntry.TABLE_NAME,   // The table to query
                    null,                           // The array of columns to return (pass null to get all)
                    selection,                              // The columns for the WHERE clause
                    selectionArgs,                          // The values for the WHERE clause
                    null,                           // don't group the rows
                    null,                            // don't filter by row groups
                    order_by                                // The sort order
            );
        }catch(Exception e) {
            createNew();
            return null;
        }

        ArrayList<ActivitySummary> l = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(ActStatContract.LocEntry._ID));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ActStatContract.LocEntry.COLUMN_NAME_NAME));
            double dist = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(ActStatContract.LocEntry.COLUMN_NAME_DIST));
            long duration = cursor.getLong(
                    cursor.getColumnIndexOrThrow(ActStatContract.LocEntry.COLUMN_NAME_DURATION));
            double minpk = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(ActStatContract.LocEntry.COLUMN_NAME_MINPKM));
            int cal = cursor.getInt(
                    cursor.getColumnIndexOrThrow(ActStatContract.LocEntry.COLUMN_NAME_CAL));
            l.add(new ActivitySummary(name,dist,duration,minpk,cal));
        }
        return l;
    }

    public ArrayList<ActivitySummary> query_rank_speed() {
        String order_by = ActStatContract.LocEntry.COLUMN_NAME_MINPKM + " ASC";
        return query(null, null, order_by);
    }

    public int rank(double minpk) {
        ArrayList<ActivitySummary> asl = query_rank_speed();
        if (asl.size()==0) return 1;
        for(int i=0;i<asl.size();i++) {
            if(minpk < asl.get(i).minpk) return i+1;
        }
        return asl.size()+1;
    }

}
