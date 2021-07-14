package com.jason.moment.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.jason.moment.activity.Run;
import com.jason.moment.util.Config;

public class GPSLoggerConnection implements ServiceConnection {

    private final Run activity;
    private String TAG = "GPSLoggerConnection";

    public GPSLoggerConnection(Run tl) {
        activity = tl;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        //activity.setEnabledActionButtons(false);
        activity.getGpsLogger().set_use_db(activity.get_use_db());
        activity.setGpsLogger(null);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        activity.setGpsLogger( ((GPSLogger.GPSLoggerBinder) service).getService());
        // If not already tracking, start tracking
        if (!activity.getGpsLogger().isTracking()) {
            //activity.setEnabledActionButtons(false);
            activity.getGpsLogger().setTracking(true);
            activity.getGpsLogger().set_use_db(activity.get_use_db());

            Intent intent = new Intent(Config.INTENT_START_TRACKING);
            intent.putExtra("activity_file_name", activity.getCurrentTrackId());
            activity.sendBroadcast(intent);
        }
    }
}