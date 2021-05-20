package com.jason.moment.util;

import android.content.Context;
import android.util.Log;

import com.jason.moment.util.db.MyLoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class StartupBatch {
    private Context _ctx;
    public StartupBatch(Context ctx) {
        _ctx = ctx;
    }
    static boolean _executed = false;
    static String TAG = "StartupBatch";
    public void execute() {
        try{
            Log.d(TAG,"-- Startup Batch Started...");
            //if(genCVSfiles()) Log.d(TAG, "-- Success");
            //if(genMNTfiles()) Log.d(TAG, "-- Success");
            //deserializeTest();
            //genTodayDB4Sample();
            //deleteDB();
            geturl();
        }catch(Exception e) {
            Log.d(TAG,"-- Startup Batch Exception...");
            Log.d(TAG,"-- Err: " + e);
            e.printStackTrace();
        }finally{
            _executed = true;
            Log.d(TAG,"-- Startup Batch End...");
        }
        return;
    }

    public void geturl() throws Exception{
        String urlstr = "http://ezehub.club/moment/list.php?dir=upload&&ext=mnt";

        String fullString="";
        URL url = new URL(urlstr);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            fullString += line;
        }
        reader.close();
        Log.e(TAG, "-- geturl() " + fullString);
    }

    public boolean deleteDB() {
        MyLoc myloc=new MyLoc(_ctx);
        myloc.deleteAll();
        return true;
    }

    public boolean genTodayDB4Sample() {
        ArrayList<MyActivity> mal = MyActivityUtil.deserializeFromCSV("20210502_092412.csv");
        if(mal==null) {
            Log.d(TAG, "Sample cvs file does not exist!!");
            return false;
        }
        MyLoc myloc=new MyLoc(_ctx);
        myloc.deleteAll();
        for(int i=0;i<mal.size();i++) {
            MyActivity a = mal.get(i);
            myloc.ins(a.latitude,a.longitude,DateUtil.DateToString(new Date(),"yyyy/MM/dd"),a.cr_time);
        }
        return true;
    }

    public boolean genMNTfiles() {
        Log.d(TAG, "-- genMNTfiles call...");
        int original_setup = Config._default_ext;

        // setup
        Config._default_ext = Config._csv;
        MyActivityUtil.initialize();

        File flist[] = MyActivityUtil.getAllFiles();
        for (int i=0;i<flist.length;i++) {
            Log.e(TAG, "-- VVVV orig files " + i + " " + flist[i].getName());
        }
        for(int i=0;i<flist.length;i++) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserializeFromCSV(flist[i]);
            String mntfname = MyActivityUtil.renameExt(flist[i], "mnt");
            MyActivityUtil.serializeIntoMnt(mal, mntfname);
        }
        Config._default_ext = original_setup;
        MyActivityUtil.initialize();
        return true;
    }

    public boolean genCVSfiles() {
        Log.d(TAG, "-- genCVSfiles call...");
        int original_setup = Config._default_ext;

        // setup
        Config._default_ext = Config._ser;
        MyActivityUtil.initialize();

        File flist[] = MyActivityUtil.getAllFiles();
        for (int i=0;i<flist.length;i++) {
            Log.e(TAG, "-- VVVV orig files " + i + " " + flist[i].getName());
        }
        for(int i=0;i<flist.length;i++) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserializeActivityFromMnt(flist[i]);
            String csvfname = MyActivityUtil.renameExt(flist[i], "csv");
            MyActivityUtil.serializeIntoCSV(mal, csvfname);
        }
        Config._default_ext = original_setup;
        MyActivityUtil.initialize();
        return true;
    }

    public void deserializeTest() {
        ArrayList<MyActivity> mal = MyActivityUtil.deserializeFromCSV("20210515.csv");
        for(int i=0;i<mal.size();i++) Log.e(TAG, "-- " + mal.get(i).toString());
    }



}
