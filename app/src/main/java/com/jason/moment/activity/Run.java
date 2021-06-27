package com.jason.moment.activity;

import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.jason.moment.R;
import com.jason.moment.service.GPSLogger;

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
    private String currentTrackId;
    public String getCurrentTrackId() {
        return this.currentTrackId;
    }

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
}
