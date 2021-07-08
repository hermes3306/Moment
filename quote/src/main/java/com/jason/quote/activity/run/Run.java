package com.jason.quote.activity.run;

import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;


import com.jason.quote.R;
import com.jason.quote.service.GPSLogger;
import com.jason.quote.util.Config;
import com.jason.quote.util.MyActivity;
import com.jason.quote.util.MyActivityUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Run extends AppCompatActivity {
    GPSLogger gpsLogger = null;
    boolean use_db = false;
    public boolean get_use_db() {return use_db;}
    public void set_use_db(boolean b) {use_db = b;}

    public void setGpsLogger(GPSLogger l) {
        this.gpsLogger = l;
    }
    public GPSLogger getGpsLogger() {
        return gpsLogger;
    }
    private long currentRunId;
    public long getCurrentRunId() {
        return this.currentRunId;
    }
    public void setCurrentRunId(long id) {this.currentRunId = id;}
    public void setEnabledActionButtons(boolean b) {}

    public Intent gpsLoggerServiceIntent = null;
    public ServiceConnection gpsLoggerConnection = null;
    public static boolean activity_quit_normally = false;
    public ArrayList list = null;

    // Loc Service binding
    MyActivity last_activity = null;
    public ArrayList<String> pic_filenames = new ArrayList<>();
    public ArrayList<String> mov_filenames = new ArrayList<>();
    public ArrayList<String> media_filenames = new ArrayList<>();
    public String activity_file_name = null;

    public static boolean paused = false;
    public boolean resume = false;


    static Timer timer = new Timer();
    public void showGPS() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
        }
        ImageButton imb_wifi_off = (ImageButton) findViewById(R.id.imbt_wifi_off);
        ImageButton imb_wifi_on = (ImageButton) findViewById(R.id.imbt_wifi_on);
        imb_wifi_on.setVisibility(View.VISIBLE);
        imb_wifi_off.setVisibility(View.GONE);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                Run.this.runOnUiThread(new Runnable() {
                    public void run() {
                        imb_wifi_on.setVisibility(View.GONE);
                        imb_wifi_off.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 500);
    }

    @Override
    public void onPause() {
        paused = true;
        resume = false;
        if(!activity_quit_normally) {
            File lastRun = new File(Config.CSV_SAVE_DIR, Config.Unsaved_File_name);
            MyActivityUtil.serializeIntoCSV(list, media_filenames, lastRun);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        paused = false;
        resume = true;
        if(true) {
            File lastRun = new File(Config.CSV_SAVE_DIR, Config.Unsaved_File_name);
            if(lastRun.exists()) lastRun.delete();
        }
        super.onResume();
    }



}
