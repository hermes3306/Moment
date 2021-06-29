package com.jason.quote.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.jason.quote.util.AddressUtil;
import com.jason.quote.util.MyMediaInfo;
import com.jason.quote.util.SampleLoc;
import com.jason.quote.util.StringUtil;

import java.io.File;
import java.util.Date;

public class MyMedia {
    private static final String TAG = "MyLoc";
    private static MyMedia instance=null;
    private static Context _ctx = null;
    public static MyMedia getInstance(Context ctx) {
        _ctx = ctx;
        if(instance==null) instance = new MyMedia(ctx);
        return instance;
    }

    private static MyMediaDBHelper dbHelper = null;
    private static SQLiteDatabase db = null;
    private static SQLiteDatabase dbr = null;


    public MyMedia(Context ctx) {
        _ctx = ctx;
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
                mm_info.getPlace(),
                mm_info.getGrade(),
                mm_info.getAddress(),
                mm_info.getLatitude(),
                mm_info.getLongitude(),
                mm_info.getCr_datetime());
    }

    public void delete(long key) {
        db.execSQL("DELETE FROM " + MyMediaContract.MediaEntry.TABLE_NAME +
                " WHERE " + MyMediaContract.MediaEntry._ID + " = "  + key);
    }

    public void ins(File file, Location location) {
        MyMediaInfo mm_info = new MyMediaInfo();
        mm_info.setName(file.getName());

        if(location==null) {
            mm_info.setLatitude(SampleLoc.home.latitude);
            mm_info.setLongitude(SampleLoc.home.longitude);
        }else {
            mm_info.setLatitude(location.getLatitude());
            mm_info.setLongitude(location.getLongitude());
            String memo = AddressUtil.getAddressDong(_ctx,location.getLatitude(), location.getLongitude());
            mm_info.setMemo(memo);
        }
        Date d = new Date(file.lastModified());
        String cr = StringUtil.DateToString(d,"yyyy-MM-dd HH:mm:ss");
        mm_info.setCr_datetime(cr);
        mm_info.setMo_datetime(cr);
        mm_info.setKey(-1);
        ins(mm_info);
    }

    public void ins(long key, String name, String memo, String place, String grade, String address, double lat, double lng, String cr ) {
        Date today = new Date();
        String mo = StringUtil.DateToString(new Date(),"yyyy-MM-dd HH:mm:ss");
        if(cr == null) cr = mo;

        ContentValues values = new ContentValues();
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_NAME, name);
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_MEMO, memo);
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_PLACE, place);
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_GRADE, grade);
        values.put(MyMediaContract.MediaEntry.COLUMN_NAME_ADDRESS, address);
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
                    cursor.getColumnIndexOrThrow(MyMediaContract.MediaEntry.COLUMN_NAME_MEMO));
            String _place = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyMediaContract.MediaEntry.COLUMN_NAME_PLACE));
            String _address = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyMediaContract.MediaEntry.COLUMN_NAME_ADDRESS));
            String _grade = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyMediaContract.MediaEntry.COLUMN_NAME_GRADE));
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
            mmf.setPlace(_place);
            mmf.setGrade(_grade);
            mmf.setAddress(_address);
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
