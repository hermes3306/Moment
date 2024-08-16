package com.jason.moment.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

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
        // it doesn't work well
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "-- onServiceConnected");

        activity.setGpsLogger( ((GPSLogger.GPSLoggerBinder) service).getService());
        activity.getGpsLogger().set_use_db(activity.get_use_db());
        activity.getGpsLogger().set_use_broadcast(activity.get_use_broadcast());
        activity.getGpsLogger().setCurrentRunId(activity.getCurrentRunId());

        if(activity.get_use_db()) {
            Intent intent = new Intent(Config.INTENT_START_RUNNING);
            intent.putExtra("currentRunId", activity.getCurrentRunId());
            activity.sendBroadcast(intent);
        }

        if(activity.get_use_broadcast()) {
            Intent intent = new Intent(Config.INTENT_START_BROADCAST);
            activity.sendBroadcast(intent);
        }

        if (!activity.getGpsLogger().isTracking()) {
            activity.getGpsLogger().setTracking(true);

            Intent intent = new Intent(Config.INTENT_START_TRACKING);
            intent.putExtra("activity_file_name", activity.getCurrentTrackId());
            activity.sendBroadcast(intent);

        }

    }
}