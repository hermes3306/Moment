package com.jason.quote.service;

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

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.jason.quote.R;
import com.jason.quote.activity.NotificationActivity;
import com.jason.quote.util.db.MyRun;


/**
 * GPS logging service.
 * 
 * @author Nicolas Guillaumin
 *
 */
public class GPSLogger extends Service implements LocationListener {

	private static final String TAG = GPSLogger.class.getSimpleName();
	public static final String RUN_ID = "run_id";

	/**
	 * Data helper.
	 */
	private MyRun dataHelper;

	/**
	 * Are we currently tracking ?
	 */
	private boolean isRunning = false;
	
	/**
	 * Is GPS enabled ?
	 */
	private boolean isGpsEnabled = false;

	/**
	 * Use barometer yes/no ?
	 */
	private boolean use_barometer = false;
	
	/**
	 * System notification id.
	 */
	private static final int NOTIFICATION_ID = 1;
	private static String CHANNEL_ID = "GPSLogger_Channel";
	
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
	private long currentRunId = -1;

	/**
	 * the timestamp of the last GPS fix we used
	 */
	private long lastGPSTimestamp = 0;
	
	/**
	 * the interval (in ms) to log GPS fixes defined in the preferences
	 */
	private long gpsLoggingInterval;
	private long gpsLoggingMinDistance;

	/**
	 * Receives Intent for way point tracking, and stop/start logging.
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "Received intent " + intent.getAction());
			
			if (GPS.INTENT_START_TRACKING.equals(intent.getAction()) ) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					Long trackId = extras.getLong(GPSLogger.RUN_ID);
					startTracking(trackId);
				}
			} else if (GPS.INTENT_STOP_TRACKING.equals(intent.getAction()) ) {
				stopTrackingAndSave();
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
		if (! isRunning ) {
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
		Log.v(TAG, "Service onCreate()");
		dataHelper = new MyRun(this);

		//read the logging interval from preferences
		gpsLoggingInterval = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString(
				GPS.Preferences.KEY_GPS_LOGGING_INTERVAL, GPS.Preferences.VAL_GPS_LOGGING_INTERVAL)) * 1000;
		gpsLoggingMinDistance = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString(
				GPS.Preferences.KEY_GPS_LOGGING_MIN_DISTANCE, GPS.Preferences.VAL_GPS_LOGGING_MIN_DISTANCE));
		use_barometer =  PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getBoolean(
				GPS.Preferences.KEY_USE_BAROMETER, GPS.Preferences.VAL_USE_BAROMETER);

		// Register our broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(GPS.INTENT_START_TRACKING);
		filter.addAction(GPS.INTENT_STOP_TRACKING);
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
	private void startTracking(long trackId) {
		currentRunId = trackId;
		Log.v(TAG, "Starting track logging for track #" + trackId);
		// Refresh notification with correct Track ID
		NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nmgr.notify(NOTIFICATION_ID, getNotification());
		isRunning = true;
	}

	/**
	 * Stops GPS Logging
	 */
	private void stopTrackingAndSave() {
		isRunning = false;
		dataHelper.stopRunning(currentRunId);
		currentRunId = -1;
		this.stopSelf();
	}

	@Override
	public void onLocationChanged(Location location) {		
		// We're receiving location, so GPS is enabled
		isGpsEnabled = true;
		
		// first of all we check if the time from the last used fix to the current fix is greater than the logging interval
		if((lastGPSTimestamp + gpsLoggingInterval) < System.currentTimeMillis()){
			lastGPSTimestamp = System.currentTimeMillis(); // save the time of this fix
		
			lastLocation = location;
			
			if (isRunning) {
				dataHelper.track(currentRunId, location);
			}
		}
	}

	/**
	 * Builds the notification to display when tracking in background.
	 */
    private Notification getNotification() {
		Intent startTrackLogger = new Intent(this, NotificationActivity.class);
		startTrackLogger.putExtra("currentRunId", currentRunId);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startTrackLogger, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_notifications_black_24dp)
				.setContentTitle(getResources().getString(R.string.notification_title).replace("{0}", (currentRunId > -1) ? Long.toString(currentRunId) : "?"))
				.setContentText(getResources().getString(R.string.notification_text))
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
			CharSequence name = "GPS Logger";
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
	 * Setter for isRunning
	 * @return true if we're currently tracking, otherwise false.
	 */
	public boolean isRunning() {
		return isRunning;
	}

}
