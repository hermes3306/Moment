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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CaloryUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
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
    public TextView tv_start_cur;
    public TextView tv_start_calory;

    public Date start_time;
    public double dist=0;
    public boolean quit=false;

    public ArrayList list = null;
    public MyActivity first = null;
    public MyActivity last = null;
    public String filename;

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
        tv_start_cur = (TextView)findViewById(R.id.tv_start_cur);
        tv_start_calory = (TextView)findViewById(R.id.tv_start_calory);

        filename = StringUtil.DateToString(new Date(),"yyyyMMdd_HHmmss") + ".mnt";
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
                        MyActivityUtil.serialize(list, filename );
                        Toast.makeText(getApplicationContext(), "활동이 저장되었습니다!" + filename, Toast.LENGTH_SHORT).show();
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
        public Date last=null;
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
                        String s1 = String.format("%.1f", dist);
                        if(s1.length()>4) s1=String.format("%.0f", dist);
                        tv_start_km.setText(s1);
                        tv_start_km_str.setText("미터");
                    } else {
                        String s1 = String.format("%.2f", dist/1000.0);
                        if(s1.length()==4) s1=String.format("%.1f", dist);
                        if(s1.length()==5) s1=String.format("%.0f", dist);
                        tv_start_km.setText(s1);
                        tv_start_km_str.setText("킬로미터");
                    }

                    double  minpkm = MyActivityUtil.MinPerKm(list);
                    String tt1 = StringUtil.elapsedStr2((long) (minpkm*1000*60.0));
                    tv_start_avg.setText("" + tt1);

                    double  minp1km = MyActivityUtil.MinPer1Km(list);
                    String tt2 = StringUtil.elapsedStr2((long) (minp1km*1000*60.0));
                    tv_start_cur.setText("" + tt2);

                    float burntkCal;
                    int durationInSeconds = MyActivityUtil.durationInSeconds(list);
                    int stepsTaken = (int) (dist / Config._strideLengthInMeters);
                    burntkCal = CaloryUtil.calculateEnergyExpenditure(
                        Config._height,
                        Config._age,
                        Config._weight,
                        Config.GENDER_MALE,
                        durationInSeconds,
                        stepsTaken,
                        Config._strideLengthInMeters
                    );
                    tv_start_calory.setText("" + String.format("%.3f", burntkCal));
                    if(last==null) {
                        last = new Date();
                        MyActivityUtil.serialize(list, filename );
                    }else {
                        Date now = new Date();
                        if(DateUtil.isLongerThan1Min(last, now)) {
                            MyActivityUtil.serialize(list, filename );
                            last = now;
                        }
                    }

                }
            });
        } /* end of run() */
    } /* end of MyTimerTask */
}