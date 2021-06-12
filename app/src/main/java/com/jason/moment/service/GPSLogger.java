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
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.jason.moment.MapsActivity;
import com.jason.moment.MyReportActivity;
import com.jason.moment.R;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.LocationUtil;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.db.MyLoc;

import java.util.Date;


public class GPSLogger extends Service implements LocationListener {

    private static final String TAG = GPSLogger.class.getSimpleName();
    LocationListener _myLocationListener = null;
    Context _ctx = null;

    /**
     * Data helper.
     */
    //private DataHelper dataHelper;

    /**
     * Are we currently tracking ?
     */
    private boolean isTracking = false;

    /**
     * Is GPS enabled ?
     */
    private boolean isGpsEnabled = false;

    /**
     * Use barometer yes/no ?
     */
    private final boolean use_barometer = false;

    /**
     * System notification id.
     */
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "Moment_Channel";

    /**
     * Last known location
     */
    private Location lastLocation;

    /**
     * LocationManager
     */
    private LocationManager lmgr;

    /**
     * Current Track ID
     */
    private String activity_file_name = "-1";

    /**
     * the timestamp of the last GPS fix we used
     */
    private long lastGPSTimestamp = 0;

    private long lastSAVETimestamp = 0;

    /**
     * the interval (in ms) to log GPS fixes defined in the preferences
     */
    private long gpsLoggingInterval;
    private long gpsLoggingMinDistance;

    /**
     * Receives Intent for way point tracking, and stop/start logging.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "-- Received intent " + intent.getAction());

            if (Config.INTENT_TRACK_WP.equals(intent.getAction())) {
                // Track a way point
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    // because of the gps logging interval our last fix could be very old
                    // so we'll request the last known location from the gps provider
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        lastLocation = lmgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null) {
                        }
                    }
                }
            } else if (Config.INTENT_START_TRACKING.equals(intent.getAction()) ) {
                Log.d(TAG, "-- INTENT_START_TRACKING");
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    activity_file_name = extras.getString("activity_file_name");
                    Log.d(TAG, "-- activity_file_name" + activity_file_name);
                    startTracking(activity_file_name);
                } else {
                    Log.d(TAG, "-- activity_file_name" + null);
                }
            } else if (Config.INTENT_STOP_TRACKING.equals(intent.getAction()) ) {
                stopTrackingAndSave();
            } else if (Config.INTENT_CONFIG_CHANGE.equals(intent.getAction()) ) {
                Log.e(TAG, "-- GPSLogger get message of CONFIG_CHANGE");

                Config.initialize(getApplicationContext());
                gpsLoggingInterval = intent.getLongExtra("gpsLoggingInterval",Config._loc_interval);
                gpsLoggingMinDistance = intent.getLongExtra("gpsLoggingMinDistance", (long)Config._loc_distance);
                if (ContextCompat.checkSelfPermission(_ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Register ourselves for location updates
                    lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsLoggingInterval, gpsLoggingMinDistance, _myLocationListener);
                    Log.e(TAG, "-- gpsLoggingInterval:" + gpsLoggingInterval);
                    Log.e(TAG, "-- gpsLoggingMinDistance:" + gpsLoggingMinDistance);
                }
            }
        }
    };

    /**
     * Binder for service interaction
     */
    private final IBinder binder = new GPSLoggerBinder();

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
        if (! isTracking ) {
            Log.v(TAG, "Service self-stopping");
            stopSelf();
        }

        // We don't want onRebind() to be called, so return false.
        return false;
    }

    /**
     * Bind interface for service interaction
     */
    public class GPSLoggerBinder extends Binder {

        /**
         * Called by the activity when binding.
         * Returns itself.
         * @return the GPS Logger service
         */
        public GPSLogger getService() {
            return GPSLogger.this;
        }
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "-- Service onCreate()");
        _ctx = getApplicationContext();
        _myLocationListener = this;
        //dataHelper = new DataHelper(this);

        Config.initialize(getApplicationContext());
        gpsLoggingInterval = Config._loc_interval;
        gpsLoggingMinDistance = (long)Config._loc_distance;

        // Register our broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.INTENT_START_TRACKING);
        filter.addAction(Config.INTENT_STOP_TRACKING);
        filter.addAction(Config.INTENT_CONFIG_CHANGE);
        registerReceiver(receiver, filter);

        // Register ourselves for location updates
        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsLoggingInterval, gpsLoggingMinDistance, this);
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "-- Service onStartCommand(-,"+flags+","+startId+")");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Service onDestroy()");
        if (isTracking) {
            // If we're currently tracking, save user data.
            stopTrackingAndSave();
        }

        // Unregister listener
        lmgr.removeUpdates(this);

        // Unregister broadcast receiver
        unregisterReceiver(receiver);

        // Cancel any existing notification
        stopNotifyBackgroundService();
        super.onDestroy();
    }

    /**
     * Start GPS tracking.
     */
    private void startTracking(String trackId) {
        activity_file_name = trackId;
        Log.d(TAG, "-- Starting track logging for track #" + trackId);
        // Refresh notification with correct Track ID
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.notify(NOTIFICATION_ID, getNotification());
        isTracking = true;
    }

    /**
     * Stops GPS Logging
     */
    private void stopTrackingAndSave() {
        isTracking = false;
        activity_file_name = "-1";
        this.stopSelf();
    }

    public void setTracking(boolean b) {
        isTracking = b;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(Config._sharedPreferenceChanged) {
            Config.init_preference_values(getApplicationContext());
            gpsLoggingInterval = Config._loc_interval;
            gpsLoggingMinDistance = (long)Config._loc_distance;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsLoggingInterval, gpsLoggingMinDistance, this);
            }
            Log.d(TAG,"-- Shared Preference changed! reload ok!");
            Config._sharedPreferenceChanged = false;
        }

        // on background run this will write data to database
        Log.d("GPSLogger", "--- New Loc:" + location);
        // We're receiving location, so GPS is enabled
        isGpsEnabled = true;

        // first of all we check if the time from the last used fix to the current fix is greater than the logging interval
        if((lastGPSTimestamp + gpsLoggingInterval) < System.currentTimeMillis()){
            lastGPSTimestamp = System.currentTimeMillis(); // save the time of this fix
            lastLocation = location;
            if (isTracking) {
                LocationUtil.getInstance().onLocationChanged(getApplicationContext(),location);
                Log.d(TAG, "-- Location Changed Intent Broadcasting to MapActivity...");
                Intent intent = new Intent(Config.INTENT_LOCATION_CHANGED);
                intent.putExtra("location", location);
                sendBroadcast(intent);
            }
        }

        // 한시간 마다 한번씩 DBMS 내용을 파일로 저장함
        Date d = new Date();
        if(lastSAVETimestamp==0) {
            saveTodayActivities();
            lastSAVETimestamp = d.getTime();
        } else {
            if( (d.getTime() - lastSAVETimestamp) > Config._ONE_HOUR) {
                saveTodayActivities();
                lastSAVETimestamp = d.getTime();
            }
        }
    }

    private void saveTodayActivities() {
        String file_name = DateUtil.today();
        MyActivityUtil.serialize(MyLoc.getInstance(getApplication()).getToodayActivities(), file_name);
        CloudUtil.getInstance().Upload(file_name + Config._csv_ext);
    }

    /**
     * Builds the notification to display when tracking in background.
     */
    private Notification getNotification() {
        Intent myReportIntent = new Intent(this, MyReportActivity.class);
        myReportIntent.putExtra("activity_file_name", activity_file_name);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myReportIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Moment")
                .setContentText("New Activity("+activity_file_name+")")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        return mBuilder.build();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // FIXME: following two strings must be obtained from 'R.string' to support translations
            CharSequence name = "Moment";
            String description = "New Activity("+activity_file_name+")";
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
    public boolean isTracking() {
        return isTracking;
    }

}