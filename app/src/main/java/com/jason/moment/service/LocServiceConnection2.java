package com.jason.moment.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jason.moment.StartNewActivity;
import com.jason.moment.StartRunActivity;
import com.jason.moment.util.Config;

public class LocServiceConnection2 implements ServiceConnection {
    // for StartRunActivity
    private StartRunActivity activity;
    public LocServiceConnection2(StartRunActivity tl) {
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
