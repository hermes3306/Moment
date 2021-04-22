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
import com.jason.moment.util.db.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.security.AccessController.getContext;

public class MyLoc {
    private static String TAG = "MyLoc";
    private Context ctx;
    public MyLoc(Context ctx) {
        this.ctx = ctx;
    }

    public void createNew() {
        MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.createNew(db);
    }

    public void deleteAll() {
        MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.deleteAll(db);
    }

    public void ins(double lat, double lng) {
        MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Date today = new Date();
        SimpleDateFormat format1,format2;
        format1 = new SimpleDateFormat("yyyy/MM/dd");
        format2 = new SimpleDateFormat("HH:mm:ss");
        String dt = format1.format(today);
        String ti = format2.format(today);

        ContentValues values = new ContentValues();
        values.put(MyLocContract.LocEntry.COLUMN_NAME_LATITUDE, lat);
        values.put(MyLocContract.LocEntry.COLUMN_NAME_LONGITIDE, lng);
        values.put(MyLocContract.LocEntry.COLUMN_NAME_CRDATE, dt);
        values.put(MyLocContract.LocEntry.COLUMN_NAME_CRTIME, ti);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(MyLocContract.LocEntry.TABLE_NAME, null, values);
        Log.d(TAG, "-- db.insert");
        qry();
    }

    public void qry() {
        MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
//        String[] projection = {
//                BaseColumns._ID,
//                MyLocContract.LocEntry.COLUMN_NAME_LATITUDE,
//                MyLocContract.LocEntry.COLUMN_NAME_LONGITIDE
//        };

//        String selection = MyLocContract.LocEntry.COLUMN_NAME_LATITUDE + " != ?";
//        String[] selectionArgs = { "-1" };
//        String sortOrder =
//                MyLocContract.LocEntry.COLUMN_NAME_LATITUDE + " DESC";

//        Cursor cursor = db.query(
//                MyLocContract.LocEntry.TABLE_NAME,   // The table to query
//                projection,             // The array of columns to return (pass null to get all)
//                selection,              // The columns for the WHERE clause
//                selectionArgs,          // The values for the WHERE clause
//                null,                   // don't group the rows
//                null,                   // don't filter by row groups
//                sortOrder               // The sort order
//        );

                Cursor cursor = db.query(
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
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LONGITIDE));
            String dt = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRDATE));
            String ti = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRTIME));

            itemIds.add(itemId);
            Log.d(TAG, "-- " + itemId + ", " + lat + ", " + lng + ", " + dt + ", " + ti);
        }
        cursor.close();
    }

    public ArrayList<LatLng> todayPath() {
        Log.d(TAG, "-- todayPath()");
        MyLocDbHelper dbHelper = new MyLocDbHelper(ctx);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = MyLocContract.LocEntry.COLUMN_NAME_CRDATE + " == ?";
        String order_by = MyLocContract.LocEntry.COLUMN_NAME_CRTIME + " DESC";
        String today = DateUtil.DateToString(new Date(), "yyyy/MM/dd");
        Log.d(TAG, "-- Today is " + today);
        String[] selectionArgs = { today };

        Cursor cursor = db.query(
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
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LONGITIDE));
            String dt = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRDATE));
            String ti = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_CRTIME));
            l.add(new LatLng(lat, lng));
            Log.d(TAG, "-- " + itemId + ", " + lat + ", " + lng + ", " + dt + ", " + ti);
        }
        Log.d(TAG, "-- Total number of locations of today:" + l.size());
        return l;
    }

    public void drawPath(GoogleMap gmap) {
        ArrayList<LatLng> l = todayPath();
        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.RED);
        Polyline line = gmap.addPolyline(plo);
        line.setWidth(20);
        line.setPoints(l);
    }

}
