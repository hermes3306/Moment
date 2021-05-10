package com.jason.moment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.Config;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity {
    public String TAG = "StartActivity";

    public TextView tv_start_km;
    public TextView tv_start_km_str;
    public TextView tv_start_time;
    public TextView tv_start_avg;

    public Date start_time;
    public double dist=0;
    public boolean quit=false;

    public ArrayList list = null;
    public MyActivity first = null;
    public MyActivity last = null;

    public LocationManager mLocManager = null;

    private class GPSListener implements LocationListener {
        public GPSListener(String gpsProvider) {
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.d(TAG,"-- onLocationChanged! [StartActivity] " + location.getLatitude() + "," + location.getLongitude());
            Date d = new Date();
            if(list==null) {
                list = new ArrayList<MyActivity>();
            }

            last = new MyActivity(location.getLatitude(), location.getLongitude(),d);
            list.add(last);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }
    }

    StartActivity.GPSListener[] mLocationListeners = new StartActivity.GPSListener[] {
            new StartActivity.GPSListener(LocationManager.GPS_PROVIDER),
            new StartActivity.GPSListener(LocationManager.NETWORK_PROVIDER)
    };

    private void deleteLocationManager() {
        if(mLocManager!=null) {
            mLocManager.removeUpdates(mLocationListeners[0]);
            mLocManager.removeUpdates(mLocationListeners[1]);
            mLocManager = null;
        }
    }

    private void initializeLocationManager() {
        if (mLocManager == null) {
            mLocManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            mLocManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    Config._loc_interval,
                    Config._loc_distance,
                    mLocationListeners[0]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        try {
            mLocManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    Config._loc_interval,
                    Config._loc_distance,
                    mLocationListeners[1]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initializeLocationManager();

        start_time = new Date();
        tv_start_km = (TextView)findViewById(R.id.tv_start_km);
        tv_start_km_str = (TextView)findViewById(R.id.tv_start_km_str);
        tv_start_time = (TextView)findViewById(R.id.tv_start_time);
        tv_start_avg = (TextView)findViewById(R.id.tv_start_avg);

        startMyTimer();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"-- onBackPressed.");
        alertQuitDialog();
    }

    public void alertQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("활동을 중지하시겠습니까?");
        builder.setMessage("활동을 정말 중지하시겠습니까?");
        builder.setPositiveButton("중지",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteLocationManager();
                        StartActivity.this.quit = true;
                        StartActivity.this.finish();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StartActivity.this.quit = false;
                    }
                });
        builder.show();
    }

    private void startMyTimer() {
        TimerTask mTask = new StartActivity.MyTimerTask();
        Timer mTimer = new Timer();
        mTimer.schedule(mTask, 0, 1000);
    }

    // MyTimerTask can run even though the app run in background
    public class MyTimerTask extends java.util.TimerTask{
        public void run() {
            long start = System.currentTimeMillis();
            StartActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Date d = new Date();
                    String elapsed = StringUtil.elapsedStr(start_time,d);
                    tv_start_time.setText(elapsed);
                    long t1 = System.currentTimeMillis();
                    dist = MyActivityUtil.getTotalDistanceDouble(list);
                    long t2 = System.currentTimeMillis();
                    Log.d(TAG, "-- Time to check all distance: " + (t2-t1));
                    if(dist<1000) {
                        tv_start_km.setText(String.format("%.0f", dist));
                        tv_start_km_str.setText("미터");
                    } else {
                        tv_start_km.setText(String.format("%.1f", dist / 1000.0));
                        tv_start_km_str.setText("킬로미터");
                    }

                    long sec = (d.getTime()-start_time.getTime())/1000;//초
                    Log.d(TAG,"-- min:" + sec);
                    double km = (double)dist/1000; //km
                    Log.d(TAG,"-- km:" + km);
                    double secpkm;
                    if(km==0) {
                        secpkm = 0;
                    }else {
                        secpkm = (double)sec/km;
                    }
                    Log.d(TAG,"-- min/km:" + secpkm);

                    String tt = StringUtil.elapsedStr((long)secpkm);
                    tv_start_avg.setText("" + tt);
                }
            });
        } /* end of run() */
    } /* end of MyTimerTask */
}