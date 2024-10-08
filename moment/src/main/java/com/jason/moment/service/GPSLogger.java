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

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.jason.moment.MyReportActivity;
import com.jason.moment.R;
import com.jason.moment.util.C;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.LocationUtil;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.db.MyLoc;
import com.jason.moment.util.db.MyRun;

import java.util.Date;

public class GPSLogger extends Service implements LocationListener {
    private static final String TAG = GPSLogger.class.getSimpleName();
    LocationListener _myLocationListener = null;
    Context _ctx = null;

    private boolean isTracking = true;
    private boolean isGpsEnabled = false;
    private boolean use_db = false;
    private boolean use_broadcast = false;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "Moment_Channel";

    private static Location lastLocation=null;
    private LocationManager lmgr;

    private long lastGPSTimestamp = 0;
    private long lastSAVETimestamp = 0;

    private long gpsLoggingInterval;
    private long gpsLoggingMinDistance;


    public static final String RUN_ID = "run_id";
    private long currentRunId = -1;
    public void setCurrentRunId(long run_id) {
        currentRunId = run_id;
    }

    public void set_use_db(boolean b) {
        use_db = b;
    }
    public void set_use_broadcast(boolean b) {use_broadcast = b; }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras;
            extras = intent.getExtras();

            if (Config.INTENT_START_TRACKING.equals(intent.getAction()) ) {
                startTracking();

            } else if (Config.INTENT_STOP_TRACKING.equals(intent.getAction()) ) {
                stopTrackingAndSave();

            } else if (Config.INTENT_START_RUNNING.equals(intent.getAction()) ) {
                if (extras != null) {
                    currentRunId = extras.getLong("currentRunId");
                    startRunning(currentRunId);
                }

            } else if (Config.INTENT_STOP_RUNNING.equals(intent.getAction()) ) {
                if (extras != null) {
                    currentRunId = extras.getLong("currentRunId");
                    stopRunning();
                }

            } else if (Config.INTENT_START_BROADCAST.equals(intent.getAction()) ) {
                startBroadcast();

            } else if (Config.INTENT_STOP_BROADCAST.equals(intent.getAction()) ) {
                stopBroadcast();

            } else if (Config.INTENT_CONFIG_CHANGE.equals(intent.getAction()) ) {
                Config.initialize(getApplicationContext());
                gpsLoggingInterval = intent.getLongExtra("gpsLoggingInterval",Config._loc_interval);
                gpsLoggingMinDistance = intent.getLongExtra("gpsLoggingMinDistance", (long)Config._loc_distance);
                if (ContextCompat.checkSelfPermission(_ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsLoggingInterval, gpsLoggingMinDistance, _myLocationListener);
                }
            }

        }
    };

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

    public class GPSLoggerBinder extends Binder {
        public GPSLogger getService() {
            return GPSLogger.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "-- GpsLogger Service onCreate()");
        _ctx = getApplicationContext();
        _myLocationListener = this;

        Log.d(TAG, "-- Config information read done!");
        Log.d(TAG, "-- gpsLoggingInterval: " + gpsLoggingInterval);
        Log.d(TAG, "-- gpsLoggingMinDistance: " + gpsLoggingMinDistance);

        // Register our broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.INTENT_START_TRACKING);
        filter.addAction(Config.INTENT_STOP_TRACKING);
        filter.addAction(Config.INTENT_START_RUNNING);
        filter.addAction(Config.INTENT_STOP_RUNNING);
        filter.addAction(Config.INTENT_CONFIG_CHANGE);
        registerReceiver(receiver, filter);
        Log.d(TAG, "-- registerReceiver done!");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 다른 Activity가 Pause하여 UnBind후 다시 StartService하면 실행됨.
        Log.d(TAG, "-- GpsLogger Service onStartCommand(-,"+flags+","+startId+")");
        createNotificationChannel();
        Log.d(TAG, "-- createNotificationChannel!");
        startForeground(NOTIFICATION_ID, getNotification());

        // Register ourselves for location updates
        Config.initialize(getApplicationContext());
        gpsLoggingInterval = Config._loc_interval;
        gpsLoggingMinDistance = (long)Config._loc_distance;

        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "-- LocationManager!");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsLoggingInterval, gpsLoggingMinDistance, this);
        }
        Log.d(TAG, "-- lmgr.requestLocationUpdates called with interval and distance!");

        return Service.START_STICKY;
    }

    @Override
    public void onLowMemory() {
        Log.e(TAG,"Android is low on memory!");
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "-- GpsLogger Service onDestroy()");
        if (isTracking) {
            // If we're currently tracking, save user data.
            stopTrackingAndSave();
            Log.e(TAG, "-- stopTrackingAndSave called!");
        }

        // Unregister listener
        if(lmgr!=null) lmgr.removeUpdates(this);
        Log.e(TAG, "-- lmgr.removeUpdates!");

        // Unregister broadcast receiver
        unregisterReceiver(receiver);
        Log.e(TAG, "-- receiver removed!");

        // Cancel any existing notification
        stopNotifyBackgroundService();
        Log.e(TAG, "-- stopNotifyBackgroundService!");
        super.onDestroy();
    }

    private void startTracking() {
        isTracking = true;
    }

    private void stopTrackingAndSave() {
        isTracking = false;
        this.stopSelf();
    }

    boolean isRunning = false;

    private void startBroadcast() {
        use_broadcast = true;
    }

    private void stopBroadcast() {
        use_broadcast = false;
    }

    private void startRunning(long currentRunId) {
        this.currentRunId = currentRunId;
        isRunning= true;
        MyRun.getInstance(_ctx).startRunning(currentRunId);

    }

    private void stopRunning() {
        MyRun.getInstance(_ctx).stopRunning(currentRunId);
        isRunning = false;
        this.currentRunId = -1;
    }

    private void recordRunning(Location loc) {

        MyRun.getInstance(_ctx).ins(currentRunId,
                loc.getLatitude(),
                loc.getLongitude(),
                loc.getAltitude());
    }

    public void setTracking(boolean b) {
        isTracking = b;
    }

    @Override
    public void onLocationChanged(Location location) {
        isGpsEnabled = true;

        // first of all we check if the time from the last used fix to the current fix is greater than the logging interval
        if((lastGPSTimestamp + gpsLoggingInterval) < System.currentTimeMillis()){
            lastGPSTimestamp = System.currentTimeMillis(); // save the time of this fix

            LocationUtil.getInstance().onLocationChanged(getApplicationContext(),location);

            if(use_broadcast) { // if there is listener than broadcast
                Intent intent = new Intent(Config.INTENT_LOCATION_CHANGED);
                intent.putExtra("location", location);
                sendBroadcast(intent);
            }

            if(isRunning & use_db) {
                if(lastLocation!= null) {
                    double dist = CalDistance.getInstance().calculateDistance(lastLocation,location);
                    if(dist < Config._loc_distance) return;
                    else recordRunning(location);
                }
                else recordRunning(location);
            }
            lastLocation = location;
        } else return;

        // 한시간 마다 한번씩 DBMS 내용을 파일로 저장함
        Date d = new Date();
        if(lastSAVETimestamp==0) {
            lastSAVETimestamp = d.getTime();
        } else {
            if( (d.getTime() - lastSAVETimestamp) > Config._ONE_HOUR) {
                saveTodayActivities();
                lastSAVETimestamp = d.getTime();
            }
        }
    }

    private String getActivity_file_name () {
        return DateUtil.today() + "_" + C.getRunnerName(getApplicationContext());
    }

    private void saveTodayActivities() {
        String file_name = getActivity_file_name();
        MyActivityUtil.serialize(MyLoc.getInstance(getApplication()).getToodayActivities(), file_name);
        CloudUtil.getInstance().Upload(file_name + Config._csv_ext);
    }

    /**
     * Builds the notification to display when tracking in background.
     */
    private Notification getNotification() {
        Intent myReportIntent = new Intent(this, MyReportActivity.class);
        myReportIntent.putExtra("activity_file_name", getActivity_file_name());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, myReportIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Moment")
                .setContentText("New Activity("+DateUtil.getActivityName(new Date())+")")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        return mBuilder.build();
    }

    private void createNotificationChannel() {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // FIXME: following two strings must be obtained from 'R.string' to support translations
            CharSequence name = "Moment";
            String description = "New Activity("+DateUtil.getActivityName(new Date())+")";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

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

    public boolean isTracking() {
        return isTracking;
    }
}