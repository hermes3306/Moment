package com.jason.moment.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.jason.moment.MapsActivity;
import com.jason.moment.util.Config;

/**
 * Handles the bind to the GPS Logger service
 *
 * @author Nicolas Guillaumin
 *
 */
public class GPSLoggerServiceConnection implements ServiceConnection {

    /**
     * Reference to TrackLogger activity
     */
    private final MapsActivity activity;
    private String TAG = "GPSLoggerServiceConnection";

    public GPSLoggerServiceConnection(MapsActivity tl) {
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
            //activity.getGpsLogger().setTracking(true);
            Intent intent = new Intent(Config.INTENT_START_TRACKING);
            intent.putExtra("activity_file_name", activity.getCurrentTrackId());
            activity.sendBroadcast(intent);
        }
    }

    public void boardCastConfigChanged(long gpsLoggingInterval, long gpsLoggingMinDistance ) {
        Intent intent = new Intent(Config.INTENT_CONFIG_CHANGE);
        intent.putExtra("gpsLoggingInterval", gpsLoggingInterval);
        intent.putExtra("gpsLoggingMinDistance", gpsLoggingMinDistance);
        activity.sendBroadcast(intent);
        Log.e(TAG, "--INTENT_CONFIG_CHANGED message sent :");
        Log.e(TAG, "--gpsLoggingInterval:" + gpsLoggingInterval);
        Log.e(TAG, "--gpsLoggingMinDistance:" +  gpsLoggingMinDistance);
    }

}