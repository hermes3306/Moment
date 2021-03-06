package com.jason.quote.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jason.quote.util.DateUtil;
import com.jason.quote.util.MyActivity;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyRun {
    private static final String TAG = "MyRun";
    private final Context ctx;
    private static MyRun instance=null;
    public static MyRun getInstance(Context ctx) {
        if(instance==null) instance = new MyRun(ctx);
        return instance;
    }

    private static MyRunDbHelper dbHelper = null;
    private static SQLiteDatabase db = null;
    private static SQLiteDatabase dbr = null;

    public MyRun(Context ctx) {
        this.ctx = ctx;
        if(dbHelper == null) dbHelper = new MyRunDbHelper(ctx);
        if(db == null) db = dbHelper.getWritableDatabase();
        if(dbr == null) dbr = dbHelper.getReadableDatabase();
    }

    public void stopRunning(long run_id) {
        db.execSQL("update myruninfo set status = false where run_id =" +
                String.format("%d", run_id));
    }

    public long startRunning(long run_id) {
        Date today = new Date();
        SimpleDateFormat format1;
        format1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String dt = format1.format(today);
        db.execSQL("create table if not exists myruninfo (_id integer primary key, run_id long, cr_date string, status boolean)");
        ContentValues values = new ContentValues();
        values.put("run_id", run_id);
        values.put("status", true);
        values.put("cr_date", new Date().toString());
        long newRowId = db.insert("myruninfo", null, values);
        return newRowId;
    }

    public void onCreate() {
        dbHelper.onCreate(db);
    }

    public void createNew() {
        // MyRunDbHelper dbHelper = new MyRunDbHelper(ctx);
        // Gets the data repository in write mode
        // SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.createNew(db);
    }

    public void deleteAll() {
        // MyRunDbHelper dbHelper = new MyRunDbHelper(ctx);
        // Gets the data repository in write mode
        // SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.deleteAll(db);
    }

    public void track(long currentRuId, Location location) {
        ins(currentRuId,location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());
    }

    public long ins(long currentRuId, double lat, double lng, double alt) {
        Date today = new Date();
        SimpleDateFormat format1,format2;
        format1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String dt = format1.format(today);

        ContentValues values = new ContentValues();
        values.put(MyRunContract.E.COL_RUN, currentRuId);
        values.put(MyRunContract.E.COL_LAT, lat);
        values.put(MyRunContract.E.COL_LON, lng);
        values.put(MyRunContract.E.COL_ALT, alt);
        values.put(MyRunContract.E.COL_DATE, dt);

        long newRowId = db.insert(MyRunContract.E.TAB_NAME, null, values);
        return newRowId;
    }

    public long ins(long runid, double lat, double lng, double alt, String dt) {
        ContentValues values = new ContentValues();
        values.put(MyRunContract.E.COL_RUN, runid);
        values.put(MyRunContract.E.COL_LAT, lat);
        values.put(MyRunContract.E.COL_LON, lng);
        values.put(MyRunContract.E.COL_ALT, alt);
        values.put(MyRunContract.E.COL_DATE, dt);
        long newRowId = db.insert(MyRunContract.E.TAB_NAME, null, values);

        Log.d(TAG, "---- ins()" + newRowId);
        return newRowId;
    }

    public ArrayList<MyActivity> qry1(String selection,
                                               String[] selectionArgs,
                                               String order_by) {
        Cursor cursor = dbr.query(
                MyRunContract.E.TAB_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                order_by               // The sort order
        );

        ArrayList<MyActivity> l = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MyRunContract.E._ID));
            long runId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_RUN));
            double lat = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_LAT));
            double lng = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_LON));
            double alt = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_ALT));
            String dt = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_DATE));
            l.add(new MyActivity(lat, lng, dt.substring(0,9), dt.substring(11)));
        }
        return l;
    }

    public ArrayList<LatLng> qry2(String selection,
                                  String[] selectionArgs,
                                  String order_by
    ) {
        Cursor cursor = dbr.query(
                MyRunContract.E.TAB_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                order_by               // The sort order
        );

        ArrayList<LatLng> l = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MyRunContract.E._ID));
            long runId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_RUN));
            double lat = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_LAT));
            double lng = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_LON));
            double alt = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_ALT));
            String dt = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyRunContract.E.COL_DATE));
            l.add(new LatLng(lat, lng));
        }
        return l;
    }

    public ArrayList<LatLng> qry_today1() {
        String selection = "SUBSTR( " + MyRunContract.E.COL_DATE + ", 1,10)  == ?";
        String order_by = MyRunContract.E.COL_DATE + " ASC";
        String today = DateUtil.DateToString(new Date(), "yyyy/MM/dd");
        String[] selectionArgs = { today };
        return qry2(selection, selectionArgs, order_by);
    }

    public ArrayList<MyActivity> qry_by_runid(long runid) {
        String selection = MyRunContract.E.COL_RUN + "  == ?";
        String order_by = MyRunContract.E.COL_DATE + " ASC";
        String[] selectionArgs = { String.format("%d",runid) };
        return qry1(selection, selectionArgs, order_by);
    }

    public ArrayList<MyActivity> qry_today2() {
        String selection = "SUBSTR( " + MyRunContract.E.COL_DATE + ", 1,10)  == ?";
        String order_by = MyRunContract.E.COL_DATE + " ASC";
        String today = DateUtil.DateToString(new Date(), "yyyy/MM/dd");
        String[] selectionArgs = { today };
        return qry1(selection, selectionArgs, order_by);
    }

    public ArrayList<MyActivity> qry_from_last_pk(long last_pk) {
        String selection = MyRunContract.E._ID  + " > ? ";
        String order_by = MyRunContract.E._ID + " ASC";
        String[] selectionArgs = { "" + last_pk };
        return qry1(selection, selectionArgs, order_by);
    }

    public MyActivity get_last_activity() {
        String order_by = " crdate DESC limit 1";
        ArrayList<MyActivity> mal =  qry1(null, null, order_by);
        if(mal.size()>0) return mal.get(0);
        else return null;
    }


}
