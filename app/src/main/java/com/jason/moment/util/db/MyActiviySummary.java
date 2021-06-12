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

    public void onCreate() {
        dbHelper.onCreate(db);
    }

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

    public String[] getStringRange_by_dist(double dist) {
        String[] selectionArgs = new String[] { "0", "1000" };
        if(dist <= 1)               selectionArgs = new String[] { "0", "1" };
        else if(dist <=5)           selectionArgs = new String[] { "1", "5" };
        else if(dist <=10)          selectionArgs = new String[] { "5", "10" };
        else if(dist <=15)          selectionArgs = new String[] { "10", "15" };
        else if(dist < 20.0975)     selectionArgs = new String[] { "15", "20.0975" };
        else if(dist <= 22)         selectionArgs = new String[] { "20.0975", "22" };
        else if(dist <=25)          selectionArgs = new String[] { "22", "25" };
        else if(dist <=30)          selectionArgs = new String[] { "25", "30" };
        else if(dist < 42.195)      selectionArgs = new String[] { "30", "42.195" };
        else if(dist < 43)          selectionArgs = new String[] { "42.195", "43" };
        else selectionArgs = new String[] { "43", "1000" };
        return selectionArgs;
    }

    public ArrayList<ActivitySummary> query_rank_speed_by_dist(double dist) {
        String[] selectionArgs = getStringRange_by_dist(dist);

        String order_by = ActStatContract.LocEntry.COLUMN_NAME_MINPKM + " ASC";
        String selection = ActStatContract.LocEntry.COLUMN_NAME_DIST + " > ? AND " +
                ActStatContract.LocEntry.COLUMN_NAME_DIST + " <= ? " ;

        Log.e(TAG, "-- selection:\t" + selection);
        Log.e(TAG, "-- selectionArgs:\t" + selectionArgs[0] + ", " + selectionArgs[1]);
        Log.e(TAG, "-- order_by:\t" + order_by);
        return query(selection, selectionArgs, order_by);
    }

    private void list() {

        String order_by = ActStatContract.LocEntry.COLUMN_NAME_MINPKM + " ASC";
        ArrayList<ActivitySummary> asl = query(null, null, order_by);
        for(int i=0;i<asl.size();i++) {
            Log.e(TAG, "-- # : " + i );
            Log.d(TAG, "--name:\t" + asl.get(i).name);
            Log.d(TAG, "--minpk:\t" + asl.get(i).minpk);
            Log.d(TAG, "--dist:\t" + asl.get(i).dist);
            Log.d(TAG, "--duration:\t" + asl.get(i).duration);
        }
    }

    private void list_by_dist(double dist) {
        String[] selectionArgs = new String[] { "0", "100000" };
        if(dist <= 1)               selectionArgs = new String[] { "0", "1" };
        else if(dist <=5)           selectionArgs = new String[] { "1", "5" };
        else if(dist <=10)          selectionArgs = new String[] { "5", "10" };
        else if(dist <=15)          selectionArgs = new String[] { "10", "15" };
        else if(dist < 20.0975)     selectionArgs = new String[] { "15", "20.0975" };
        else if(dist <= 22)         selectionArgs = new String[] { "20.0975", "22" };
        else if(dist <=25)          selectionArgs = new String[] { "22", "25" };
        else if(dist <=30)          selectionArgs = new String[] { "25", "30" };
        else if(dist < 42.195)      selectionArgs = new String[] { "30", "42.195" };
        else if(dist < 43)          selectionArgs = new String[] { "42.195", "43" };
        else selectionArgs = new String[] { "43", "100000" };

        String order_by = ActStatContract.LocEntry.COLUMN_NAME_MINPKM + " ASC";
        String selection = ActStatContract.LocEntry.COLUMN_NAME_DIST + " > ? AND " +
                ActStatContract.LocEntry.COLUMN_NAME_DIST + " <= ? " ;

        Log.e(TAG, "-- selection:\t" + selection);
        Log.e(TAG, "-- selectionArgs:\t" + selectionArgs);
        Log.e(TAG, "-- order_by:\t" + order_by);
        ArrayList<ActivitySummary> asl = query(selection, selectionArgs, order_by);

        for(int i=0;i<asl.size();i++) {
            Log.e(TAG, "-- # : " + i );
            Log.d(TAG, "--name:\t" + asl.get(i).name);
            Log.d(TAG, "--minpk:\t" + asl.get(i).minpk);
            Log.d(TAG, "--dist:\t" + asl.get(i).dist);
            Log.d(TAG, "--duration:\t" + asl.get(i).duration);
        }
    }

    public int rank(double minpk) {
        list();
        ArrayList<ActivitySummary> asl = query_rank_speed();
        if(asl==null) return -1;
        if (asl.size()==0) return 1;
        for(int i=0;i<asl.size();i++) {
            if(minpk <= asl.get(i).minpk) return i+1;
        }
        return asl.size()+1;
    }

    public int rank(double minpk, double dist) {
        ArrayList<ActivitySummary> asl = query_rank_speed_by_dist(dist);
        if(asl==null) return -1;
        if (asl.size()==0) return 1;
        for(int i=0;i<asl.size();i++) {
            if(minpk <= asl.get(i).minpk) return i+1;
        }
        return asl.size()+1;
    }

}
