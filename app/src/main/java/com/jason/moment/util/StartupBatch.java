package com.jason.moment.util;

import android.content.Context;
import android.util.Log;

import com.jason.moment.util.db.MyActiviySummary;
import com.jason.moment.util.db.MyLoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class StartupBatch {
    private final Context _ctx;
    public StartupBatch(Context ctx) {
        _ctx = ctx;
    }
    static boolean _executed = false;
    static String TAG = "StartupBatch";
    public void execute() {
        try{
            Log.d(TAG,"-- Startup Batch Started...");
            initDatabase(_ctx);
            //if(genCVSfiles()) Log.d(TAG, "-- Success");
            //if(genMNTfiles()) Log.d(TAG, "-- Success");
            //deserializeTest();
            //genTodayDB4Sample();
            //deleteDB();
            //geturl();
            //clearShortRunActivities(_ctx);
            //clearRunActivitiesStartsWith(_ctx,"S");
            //renameMediaFiles(_ctx);
            //query_rank_speed(_ctx);
            //rebuildActivitySummaries(_ctx);
            //uploadAll(_ctx);
            //downAll(_ctx);
            //ImportTodayActivity();

        }catch(Exception e) {
            Log.d(TAG,"-- Startup Batch Exception...");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.d(TAG,"-- Err: " + exceptionAsString);
        }finally{
            _executed = true;
            Log.d(TAG,"-- Startup Batch End...");
        }
        return;
    }

    public void uploadAll(Context _ctx) {
        CloudUtil.getInstance().UploadAll(_ctx, Config._csv);
        CloudUtil.getInstance().UploadAll(_ctx, Config._mov);
        CloudUtil.getInstance().UploadAll(_ctx, Config._img);
    }

    public void initDatabase(Context _ctx){
        MyLoc.getInstance(_ctx).onCreate();
        MyActiviySummary.getInstance(_ctx).createNew();
        clearShortRunActivities(_ctx);
        rebuildActivitySummaries(_ctx);
    }

    public void downAll(Context _ctx) {
        CloudUtil.getInstance().DownloadAll(_ctx,Config._csv);
        CloudUtil.getInstance().DownloadAll(_ctx,Config._mov);
        CloudUtil.getInstance().DownloadAll(_ctx,Config._img);
        CloudUtil.getInstance().DownloadMP3(_ctx);
        MyLoc.getInstance(_ctx).createNew(); // DB를 초기화
        rebuildActivitySummaries(_ctx);
        ImportTodayActivity();
    }

    public void query_rank_speed(Context _ctx) {
        ArrayList<ActivitySummary> mas =
                MyActiviySummary.getInstance(_ctx).query_rank_speed();
        for(int i=0;i<mas.size();i++) Log.d(TAG, "--" + mas.get(i).toString() );
    }

    public void ImportTodayActivity() {
        String today = DateUtil.today();
        File f = new File(Config.CSV_SAVE_DIR, today + Config._csv_ext);
        ArrayList<MyActivity> mal=null;
        if(f.exists()) {
            mal = MyActivityUtil.deserializeFromCSV(today + Config._csv_ext);
        }
        if(mal==null) {
            return;
        }
        MyLoc myloc=new MyLoc(_ctx);
        myloc.deleteAll();
        for(int i=0;i<mal.size();i++) {
            MyActivity a = mal.get(i);
            myloc.ins(a.latitude,a.longitude,a.cr_date, a.cr_time);
        }
    }

    public void rebuildActivitySummaries(Context _ctx) {
        MyActiviySummary.getInstance(_ctx).createNew();
        File[] files = Config.CSV_SAVE_DIR.listFiles();
        for(int i=0;i<files.length;i++) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserialize(files[i]);
            ActivityStat as = MyActivityUtil.getActivityStat(mal);
            //String name = as.name;
            String name = files[i].getName();
            //if(name.length() <15 ) continue;

            double distanceKm = as.distanceKm;
            long durationInLong = as.durationInLong;
            double minperKm = as.minperKm;
            int calories = as.calories;

            if(minperKm == 0) files[i].delete();
            else {
                MyActiviySummary.getInstance(_ctx).ins(name, distanceKm, durationInLong, minperKm, calories);
                Log.d(TAG, " -- new Activity summary: " + as.toString());
            }
        }
    }

    public void renameMediaFiles(Context _ctx) {
        File[] files = Config.PIC_SAVE_DIR.listFiles();
        for(int i=0;i<files.length;i++) {
            String org = files[i].getName();
            String tar = org.substring(4);
            if(org.startsWith("IMG")) files[i].renameTo(new File(Config.PIC_SAVE_DIR, tar));
        }

        files = Config.MOV_SAVE_DIR.listFiles();
        for(int i=0;i<files.length;i++) {
            String org = files[i].getName();
            String tar = org.substring(6);
            if(org.startsWith("MOV")) files[i].renameTo(new File(Config.MOV_SAVE_DIR, tar));
        }
    }

    public void  clearShortRunActivities(Context _ctx) {

        File[] files = Config.CSV_SAVE_DIR.listFiles();
        for(int i=0;i<files.length;i++) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserialize(files[i]);
            ActivityStat as = ActivityStat.getActivityStat(mal);

            if(as.distanceKm <= 0.1 || mal.size() < 10 ) {
                files[i].delete();
                Log.d(TAG,"-- file["+files[i].getName()+"] deleted!");
            }
        }

        files = Config.MNT_SAVE_DIR.listFiles();
        for(int i=0;i<files.length;i++) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserialize(files[i]);
            if(mal.size() < 10) {
                files[i].delete();
                Log.d(TAG,"-- file["+files[i].getName()+"] deleted!");
            }
        }
    }

    public void  clearRunActivitiesStartsWith(Context _ctx, String StartsWith) {
        File[] files = Config.CSV_SAVE_DIR.listFiles();
        for(int i=0;i<files.length;i++) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserialize(files[i]);
            if(files[i].getName().startsWith(StartsWith)) {
                files[i].delete();
                Log.d(TAG, "-- file[" + files[i].getName() + "] deleted!");
            }
        }

        files = Config.MNT_SAVE_DIR.listFiles();
        for(int i=0;i<files.length;i++) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserialize(files[i]);
            if(files[i].getName().startsWith(StartsWith)) {
                files[i].delete();
                Log.d(TAG, "-- file[" + files[i].getName() + "] deleted!");
            }
        }
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

        File[] flist = MyActivityUtil.getAllFiles();
//        for (int i=0;i<flist.length;i++) {
//            Log.e(TAG, "-- VVVV orig files " + i + " " + flist[i].getName());
//        }
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

        File[] flist = MyActivityUtil.getAllFiles();
//        for (int i=0;i<flist.length;i++) {
//            Log.e(TAG, "-- VVVV orig files " + i + " " + flist[i].getName());
//        }
        for(int i=0;i<flist.length;i++) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserializeFromMnt(flist[i]);
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
