package com.jason.moment.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jason.moment.MapsActivity;
import com.jason.moment.StartRunActivity;
import com.jason.moment.util.Config;

public class LocServiceConnection implements ServiceConnection {

    private StartRunActivity activity;
    public LocServiceConnection(StartRunActivity tl) {
        activity = tl;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        activity.setLocService( ((LocService.LocServiceBinder) iBinder).getService());
        if (!activity.getLocService().isRunning()) {
            Intent intent = new Intent(Config.INTENT_START_TRACKING);
            intent.putExtra("activity_file_name", activity.getActivity_file_name());
            activity.sendBroadcast(intent);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        activity.setLocService(null);
    }
}
