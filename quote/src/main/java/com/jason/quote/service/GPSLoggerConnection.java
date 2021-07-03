package com.jason.quote.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jason.quote.activity.run.Run;


public class GPSLoggerConnection implements ServiceConnection {

	private Run activity;
	public GPSLoggerConnection(Run tl) {
		activity = tl;
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		activity.setEnabledActionButtons(false);
		activity.setGpsLogger(null);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		activity.setGpsLogger( ((GPSLogger.GPSLoggerBinder) service).getService());
		// Update record status regarding of current tracking state
//		GpsStatusRecord gpsStatusRecord = (GpsStatusRecord) activity.findViewById(R.id.gpsStatus);
//		if (gpsStatusRecord != null) {
//			gpsStatusRecord.manageRecordingIndicator(activity.getGpsLogger().isTracking());
//		}

		if (!activity.getGpsLogger().isRunning()) {
			activity.setEnabledActionButtons(false);
			Intent intent = new Intent(GPS.INTENT_START_TRACKING);
			intent.putExtra(GPSLogger.RUN_ID, activity.getCurrentRunId());
			activity.sendBroadcast(intent);
		}
	}

}
