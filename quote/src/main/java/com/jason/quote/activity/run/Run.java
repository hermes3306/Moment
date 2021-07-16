package com.jason.quote.activity.run;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.jason.quote.R;
import com.jason.quote.service.GPSLogger;
import com.jason.quote.util.Config;
import com.jason.quote.util.LocationUtil;
import com.jason.quote.util.MyActivity;
import com.jason.quote.util.MyActivityUtil;
import com.jason.quote.util.db.MyLoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Run extends AppCompatActivity {
    public Context _ctx = null;
    public String TAG = "Run";
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
    public Location new_location;
    public static long last_pk = -1;

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

        last_pk = LocationUtil.getInstance().get_last_pk();
        super.onPause();
    }

    @Override
    public void onResume() {
        paused = false;
        resume = true;

        get_last_run_from_db();
        super.onResume();
    }

    private void get_last_run_from_db() {
        long cur_pk = LocationUtil.getInstance().get_last_pk();
        if(last_pk != -1 && last_pk < cur_pk) {
            Toast.makeText(_ctx, "Last pk: "
                    + last_pk + "\nCurrent pk: "
                    + cur_pk + "\n" + (cur_pk-last_pk) +
                    " gaps", Toast.LENGTH_LONG).show();

            Log.e(TAG, "----- HERE ----------");
            Log.e(TAG, "----- HAVE TO PROCESS from last_pk ----------");
            Log.e(TAG, "----- paused_last_pk : " + last_pk );
            Log.e(TAG, "----- current_last_pk : " + LocationUtil.getInstance().get_last_pk() );

            ArrayList<MyActivity> t = MyLoc.getInstance(_ctx).getActivitiesFrom(last_pk);
            for(int i=0;i<t.size();i++) {
                Log.e(TAG,"----- " + t.get(i).toString());
                list.add(t.get(i));
            }
        }
    }




}
