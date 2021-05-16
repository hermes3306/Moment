package com.jason.moment.util;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class StartupBatch {
    static boolean _executed = false;
    static String TAG = "StartupBatch";
    public static void execute() {
        try{
            Log.d(TAG,"-- Startup Batch Started...");
            //if(genCVSfiles()) Log.d(TAG, "-- Success");
            //if(genMNTfiles()) Log.d(TAG, "-- Success");
            //deserializeTest();
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

    public static boolean genMNTfiles() {
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

    public static boolean genCVSfiles() {
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

    public static void deserializeTest() {
        ArrayList<MyActivity> mal = MyActivityUtil.deserializeFromCSV("20210515.csv");
        for(int i=0;i<mal.size();i++) Log.e(TAG, "-- " + mal.get(i).toString());
    }



}
