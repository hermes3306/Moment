package com.jason.moment.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.db.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.security.AccessController.getContext;

public class MyLoc {
    private static final String TAG = "MyLoc";
    private final Context ctx;
    private static MyLoc instance=null;
    public static MyLoc getInstance(Context ctx) {
        if(instance==null) instance = new MyLoc(ctx);
        return instance;
    }

    private static MyLocDbHelper dbHelper = null;
    private static SQLiteDatabase db = null;
    private static SQLiteDatabase dbr = null;

    public MyLoc(Context ctx) {
        this.ctx = ctx;
        if(dbHelper == null) dbHelper = new MyLocDbHelper(ctx);
        if(db == null) db = dbHelper.getWritableDatabase();
        if(dbr == null) dbr = dbHelper.getReadableDatabase();
    }

    public void onCreate() {
        dbHelper.onCreate(db);
    }

    public void createNew() {
        // MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // Gets the data repository in write mode
        // SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.createNew(db);
    }

    public void deleteAll() {
        // MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // Gets the data repository in write mode
        // SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.deleteAll(db);
    }

    public void ins(double lat, double lng) {
        // MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // Gets the data repository in write mode
        // SQLiteDatabase db = dbHelper.getWritableDatabase();

        Date today = new Date();
        SimpleDateFormat format1,format2;
        format1 = new SimpleDateFormat("yyyy/MM/dd");
        format2 = new SimpleDateFormat("HH:mm:ss");
        String dt = format1.format(today);
        String ti = format2.format(today);

        ContentValues values = new ContentValues();
        values.put(MyLocContract.LocEntry.COLUMN_NAME_LATITUDE, lat);
        values.put(MyLocContract.LocEntry.COLUMN_NAME_LONGITUDE, lng);
        values.put(MyLocContract.LocEntry.COLUMN_NAME_CRDATE, dt);
        values.put(MyLocContract.LocEntry.COLUMN_NAME_CRTIME, ti);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(MyLocContract.LocEntry.TABLE_NAME, null, values);
        //Log.d(TAG, "-- db.insert");
    }

    public void ins(double lat, double lng, String dt, String ti) {
        // MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // Gets the data repository in write mode
        // SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MyLocContract.LocEntry.COLUMN_NAME_LATITUDE, lat);
        values.put(MyLocContract.LocEntry.COLUMN_NAME_LONGITUDE, lng);
        values.put(MyLocContract.LocEntry.COLUMN_NAME_CRDATE, dt);
        values.put(MyLocContract.LocEntry.COLUMN_NAME_CRTIME, ti);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(MyLocContract.LocEntry.TABLE_NAME, null, values);
        //Log.d(TAG, "-- db.insert rowid(" + newRowId +")");
    }



    public void qry() {
        // MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // SQLiteDatabase dbr = dbHelper.getReadableDatabase();
                Cursor cursor = dbr.query(
                MyLocContract.LocEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry._ID));
            double lat = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LATITUDE));
            double lng = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LONGITUDE));
            String dt = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRDATE));
            String ti = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRTIME));
            itemIds.add(itemId);
            //Log.d(TAG, "-- " + itemId + ", " + lat + ", " + lng + ", " + dt + ", " + ti);
        }
        cursor.close();
    }

    // qry
    // selection        = " crdate == ?";
    // selectionArgs    = " 2021/05/01";
    // order_by         = " crtime desc";
    public ArrayList<MyActivity> Path2Activity(String selection,
                                               String[] selectionArgs,
                                               String order_by) {
        //Log.d(TAG, "--Path2Activity ()");
        //MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        //SQLiteDatabase dbr = dbHelper.getReadableDatabase();

        Cursor cursor = dbr.query(
                MyLocContract.LocEntry.TABLE_NAME,   // The table to query
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
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry._ID));
            double lat = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LATITUDE));
            double lng = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LONGITUDE));
            String dt = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRDATE));
            String ti = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRTIME));
            l.add(new MyActivity(lat, lng, dt, ti));
            //Log.d(TAG, "-- " + itemId + ", " + lat + ", " + lng + ", " + dt + ", " + ti);
        }
        //Log.d(TAG, "-- Total number of path:" + l.size());
        return l;
    }

    // qry
    // selection        = " crdate == ?";
    // selectionArgs    = " 2021/05/01";
    // order_by         = " crtime desc";
    public ArrayList<LatLng> Path(String selection,
                                  String[] selectionArgs,
                                  String order_by
                                  ) {
        //Log.d(TAG, "-- Path()");
        // MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // SQLiteDatabase dbr = dbHelper.getReadableDatabase();
        Cursor cursor = dbr.query(
                MyLocContract.LocEntry.TABLE_NAME,   // The table to query
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
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry._ID));
            double lat = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LATITUDE));
            double lng = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LONGITUDE));
            String dt = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRDATE));
            String ti = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRTIME));
            l.add(new LatLng(lat, lng));
            //Log.d(TAG, "-- " + itemId + ", " + lat + ", " + lng + ", " + dt + ", " + ti);
        }
        //Log.d(TAG, "-- Total number of path:" + l.size());
        return l;
    }

    public ArrayList<LatLng> path_of_today() {
        //Log.d(TAG, "-- path_of_today()");
        // MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // SQLiteDatabase dbr = dbHelper.getReadableDatabase();

        String selection = MyLocContract.LocEntry.COLUMN_NAME_CRDATE + " == ?";
        String order_by = MyLocContract.LocEntry.COLUMN_NAME_CRTIME + " ASC";
        String today = DateUtil.DateToString(new Date(), "yyyy/MM/dd");
        //Log.d(TAG, "-- Today is " + today);
        String[] selectionArgs = { today };
        return Path(selection, selectionArgs, order_by);
    }

    public ArrayList<MyActivity> getToodayActivities() {
        //Log.d(TAG, "-- todayActivity()");
        // MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // SQLiteDatabase dbr = dbHelper.getReadableDatabase();

        String selection = MyLocContract.LocEntry.COLUMN_NAME_CRDATE + " == ?";
        String order_by = MyLocContract.LocEntry.COLUMN_NAME_CRTIME + " ASC";
        String today = DateUtil.DateToString(new Date(), "yyyy/MM/dd");
        //Log.d(TAG, "-- Today is " + today);
        String[] selectionArgs = { today };
        return Path2Activity(selection, selectionArgs, order_by);
    }

    public MyActivity lastActivity() {
        //Log.d(TAG, "-- lastActivity()");
        // MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // SQLiteDatabase dbr = dbHelper.getReadableDatabase();

        //String order_by = " crdate DESC, crtime DESC";
        String order_by = " crdate DESC, crtime DESC limit 1";

        ArrayList<MyActivity> mal =  Path2Activity(null, null, order_by);
        //Log.d(TAG,"-- lastActivity called, the size of activities  " + mal.size());
        if(mal.size()>0) return mal.get(0);
        else return null;
    }

    public void drawPath(GoogleMap gmap) {
        ArrayList<LatLng> l = path_of_today();
        if(l==null) return;
        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.RED);
        Polyline line = gmap.addPolyline(plo);
        line.setWidth(20);
        line.setPoints(l);
    }

}
