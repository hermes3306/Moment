package com.jason.moment.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jason.moment.MapsActivity;
import com.jason.moment.StartRunActivity;
import com.jason.moment.util.Config;


public class GPSLoggerSvcCon4StartRun implements ServiceConnection {
    private final StartRunActivity activity;
    private String TAG = "GPSLoggerServiceConnection";

    public GPSLoggerSvcCon4StartRun(StartRunActivity tl) {
        activity = tl;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        //activity.setEnabledActionButtons(false);
        activity.setGpsLogger(null);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        activity.setGpsLogger( ((GPSLogger.GPSLoggerBinder) service).getService());
        // If not already tracking, start tracking
        if (!activity.getGpsLogger().isTracking()) {
            //activity.setEnabledActionButtons(false);
            activity.getGpsLogger().setTracking(true);
            Intent intent = new Intent(Config.INTENT_START_TRACKING);
            intent.putExtra("activity_file_name", activity.getCurrentTrackId());
            activity.sendBroadcast(intent);
        }
    }
}