package com.jason.moment.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.jason.moment.MapsActivity;
import com.jason.moment.util.Config;

public class GPSLoggerServiceConnection implements ServiceConnection {

    private final MapsActivity activity;
    private static final String TAG = "GPSLoggerServiceConnection";

    public GPSLoggerServiceConnection(MapsActivity tl) {
        activity = tl;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        activity.setGpsLogger(null);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        activity.setGpsLogger(((GPSLogger.GPSLoggerBinder) service).getService());
        // If not already tracking, start tracking
        if (activity.getGpsLogger() != null && !activity.getGpsLogger().isTracking()) {
            activity.getGpsLogger().setTracking(true);
            Intent intent = new Intent(Config.INTENT_START_TRACKING);
            String trackId = activity.getCurrentTrackId();
            if (trackId != null) {
                intent.putExtra("activity_file_name", trackId);
            } else {
                Log.w(TAG, "Current track ID is null");
            }
            activity.sendBroadcast(intent);
        }
    }
}