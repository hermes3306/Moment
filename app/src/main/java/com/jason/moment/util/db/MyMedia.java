package com.jason.moment.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jason.moment.util.DateUtil;
import com.jason.moment.util.MyMediaInfo;
import com.jason.moment.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyMedia {
    private static final String TAG = "MyLoc";
    private static MyMedia instance=null;
    Context ctx = null;
    public static MyMedia getInstance(Context ctx) {
        if(instance==null) instance = new MyMedia(ctx);
        return instance;
    }

    private static MyMediaDBHelper dbHelper = null;
    private static SQLiteDatabase db = null;
    private static SQLiteDatabase dbr = null;

    public MyMedia(Context ctx) {
        this.ctx = ctx;
        if(dbHelper == null) dbHelper = new MyMediaDBHelper(ctx);
        if(db == null) db = dbHelper.getWritableDatabase();
        if(dbr == null) dbr = dbHelper.getReadableDatabase();
    }

    public void onCreate() {
        dbHelper.onCreate(db);
    }
    public void createNew() { dbHelper.createNew(db); }
    public void deleteAll() { dbHelper.deleteAll(db); }

    public void ins(MyMediaInfo mm_info) {
        ins(mm_info.getKey(),
                mm_info.getName(),
                mm_info.getMemo(),
                mm_info.getLatitude(),
                mm_info.getLongitude(),
                mm_info.getCr_datetime());
    }

    public void delete(long key) {
        db.execSQL("DELETE FROM " + MyMediaContract.MediaEntry.TABLE_NAME +
                " WHERE " + MyMediaContract.MediaEntry._ID + " = "  + key);
    }

    public void ins(long key, String name, String memo, double lat, double lng, String cr ) {
        Date today = new Date();
        String mo = StringUtil.DateToString(new Date(),"yyyy-MM-dd HH:mm:ss");
        if(cr == null) cr = mo;

        ContentValues values = new ContentValues();
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_NAME, name);
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_LATITUDE, lat);
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_LONGITUDE, lng);
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_CR_DATETIME, cr);
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_MO_DATETIME, mo);

        if(key!=-1) delete(key);
        long newRowId = db.insert(MyMediaContract.MediaEntry.TABLE_NAME, null, values);
    }

    public MyMediaInfo qry(String name) {
        String selection = MyMediaContract.MediaEntry.COLUMN_NAME_NAME + " == ?";
        String[] selectionArgs = { name };

        Cursor cursor = dbr.query(
                MyMediaContract.MediaEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        MyMediaInfo mmf = new MyMediaInfo();

        if(cursor.moveToNext()) {
            MyMediaInfo mif = new MyMediaInfo();
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MyMediaContract.MediaEntry._ID));
            String _name = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyMediaContract.MediaEntry.COLUMN_NAME_NAME));
            String _memo = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyMediaContract.MediaEntry.COLUMN_NAME_NAME));
            double lat = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LATITUDE));
            double lng = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MyLocContract.LocEntry.COLUMN_NAME_LONGITUDE));
            String _cr = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyMediaContract.MediaEntry.COLUMN_NAME_CR_DATETIME));
            String _mo = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyMediaContract.MediaEntry.COLUMN_NAME_MO_DATETIME));

            mmf.setKey(itemId);
            mmf.setName(_name);
            mmf.setMemo(_memo);
            mmf.setLatitude(lat);
            mmf.setLongitude(lng);
            mmf.setCr_datetime(_cr);
            mmf.setMo_datetime(_mo);
            cursor.close();
            return mmf;
        }else {
            cursor.close();
            return null;
        }
    }


}
