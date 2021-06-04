package com.jason.moment.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.jason.moment.MapsActivity;
import com.jason.moment.R;
import com.jason.moment.StartRunActivity;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;

import java.util.ArrayList;
import java.util.Date;

public class LocService extends Service implements LocationListener {
    /**
     * Last known location
     */
    private Location lastLocation=null;
    public Location getLastLocation() {return lastLocation;}

    /**
     * LocationManager
     */
    private LocationManager lmgr1,lmgr2;

    /**
     * Is GPS enabled ?
     */
    private boolean isGpsEnabled = false;

    private long lastGPSTimestamp = 0;

    /**
     * the interval (in ms) to log GPS fixes defined in the preferences
     */
    private long gpsLoggingInterval;
    private long gpsLoggingMinDistance;

    /**
     * System notification id.
     */
    private static final int NOTIFICATION_ID = 12345;
    private static String CHANNEL_ID = "LocService_Channel";
    private String activity_file_name;
    private ArrayList<MyActivity> list = null;
    private MyActivity last = null;

    private boolean isRunning = false;
    private static final String TAG = LocService.class.getSimpleName();



    /**
     * Receives Intent for way point tracking, and stop/start logging.
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Received intent " + intent.getAction());

            if (Config.INTENT_START_TRACKING.equals(intent.getAction()) ) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    activity_file_name = extras.getString("activity_file_name");
                    Log.d(TAG, "-- onReceive to START_TRACKING:" + activity_file_name);
                    startRuning(activity_file_name);
                }
            } else if (Config.INTENT_STOP_TRACKING.equals(intent.getAction()) ) {
                stopRunningAndSave();
            }
        }
    };

    private Notification getNotification() {
        Intent startRunActivityIntent = new Intent(this, StartRunActivity.class);
        startRunActivityIntent.putExtra("activity_file_name", activity_file_name);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startRunActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("New Activity Started!")
                .setContentText("Activity Name: " + activity_file_name)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        return mBuilder.build();
    }


    /**
     * Start GPS tracking.
     */
    private void startRuning(String activity_file_name) {
        Log.d(TAG, "-- Starting Running logging for track #" + activity_file_name);
        // Refresh notification with correct Track ID
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.notify(NOTIFICATION_ID, getNotification());
        isRunning = true;
    }

    /**
     * Stops GPS Logging
     */
    private void stopRunningAndSave() {
        Log.d(TAG, "-- stopRunningAndSave # S" + activity_file_name);
        isRunning = false;
        CloudUtil cu = new CloudUtil();
        cu.Upload(getApplicationContext(), "S" + activity_file_name);
        MyActivityUtil.serialize(list, "S" + activity_file_name);
        this.stopSelf();
    }

    /**
     * Bind interface for service interaction
     */
    public class LocServiceBinder extends Binder {

        /**
         * Called by the activity when binding.
         * Returns itself.
         * @return the LocService
         */
        public LocService getService() {
            return LocService.this;
        }
    }

    /**
     * Binder for service interaction
     */
    private final IBinder binder = new LocService.LocServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Service onBind()");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "Service onUnbind()");
        // If we aren't currently tracking we can
        // stop ourselves
        if (! isRunning ) {
            Log.v(TAG, "Service self-stopping");
            stopSelf();
        }

        // We don't want onRebind() to be called, so return false.
        return false;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "-- LocService onCreate()");
        Log.d(TAG, "-- Location update interval:" + Config._loc_interval);
        Log.d(TAG, "-- Location update distance:" + Config._loc_distance);

        // Register our broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.INTENT_START_TRACKING);
        filter.addAction(Config.INTENT_STOP_TRACKING);
        registerReceiver(receiver, filter);

        // Register ourselves for location updates
        lmgr1 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lmgr2 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        gpsLoggingInterval = Config._loc_interval;
        gpsLoggingMinDistance = (long)Config._loc_distance;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lmgr1.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config._loc_interval, Config._loc_distance, this);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lmgr2.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config._loc_interval, Config._loc_distance, this);
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service onStartCommand(-,"+flags+","+startId+")");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Service onDestroy()");
        if (isRunning) {
            // If we're currently tracking, save user data.
            stopRunningAndSave();
        }

        // Unregister listener
        lmgr1.removeUpdates(this);
        lmgr2.removeUpdates(this);

        // Unregister broadcast receiver
        unregisterReceiver(receiver);

        // Cancel any existing notification
        stopNotifyBackgroundService();
        super.onDestroy();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // FIXME: following two strings must be obtained from 'R.string' to support translations
            CharSequence name = "LocService";
            String description = "Display when tracking in Background";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Stops notifying the user that we're tracking in the background
     */
    private void stopNotifyBackgroundService() {
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        isGpsEnabled = true;
        if(list==null) {
            list = new ArrayList<MyActivity>();
        }

        Log.d(TAG, "-- New Noc:" + location);
        if((lastGPSTimestamp + gpsLoggingInterval) < System.currentTimeMillis()){
            lastGPSTimestamp = System.currentTimeMillis(); // save the time of this fix
            Date d = new Date();
            if (isRunning) {
                double dist;
                if(lastLocation==null) {
                    dist = 0;
                    last = new MyActivity(location.getLatitude(), location.getLongitude(),d);
                    list.add(last);
                }else {
                    dist = CalDistance.dist(lastLocation.getLatitude(), lastLocation.getLongitude(), location.getLatitude(), location.getLongitude());
                    if(dist > Config._minLocChange) {
                        last = new MyActivity(location.getLatitude(), location.getLongitude(),d);
                        list.add(last);
                    }
                }
            }
            lastLocation = location;
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        isGpsEnabled = false;
    }

    @Override
    public void onProviderEnabled(String provider) {
        isGpsEnabled = true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Not interested in provider status
    }

    /**
     * Getter for gpsEnabled
     * @return true if GPS is enabled, otherwise false.
     */
    public boolean isGpsEnabled() {
        return isGpsEnabled;
    }

    /**
     * Setter for isTracking
     * @return true if we're currently tracking, otherwise false.
     */
    public boolean isRunning() {
        return isRunning;
    }

}
