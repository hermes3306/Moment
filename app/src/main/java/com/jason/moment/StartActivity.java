package com.jason.moment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

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

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class StartActivity extends AppCompatActivity implements View.OnClickListener{
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_start_pause:
                alertQuitDialog();
                break;
            case R.id.imb_start_camera:
                int i=10;
                break;
            case R.id.imb_start_env:
                int j;
                break;
        }
    }

    private class GPSListener implements LocationListener {
        public GPSListener(String gpsProvider) {
        }

        private Location lastloc = null;
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.d(TAG,"-- onLocationChanged! [StartActivity] " + location.getLatitude() + "," + location.getLongitude());
            Date d = new Date();
            if(list==null) {
                list = new ArrayList<MyActivity>();
            }
            double dist;
            if(lastloc==null) {
                dist = 0;
                last = new MyActivity(location.getLatitude(), location.getLongitude(),d);
                list.add(last);
                lastloc = location;
            }else {
                dist = CalDistance.dist(lastloc.getLatitude(), lastloc.getLongitude(), location.getLatitude(), location.getLongitude());
                if(dist > Config._minLocChange) {
                    last = new MyActivity(location.getLatitude(), location.getLongitude(),d);
                    list.add(last);
                    lastloc = location;
                }
            }
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
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        Config._enable_network_provider = sharedPreferences.getBoolean("NetworkProvider", Config._enable_network_provider);
        String _loc_interval = sharedPreferences.getString("interval", "");
        String _loc_distance = sharedPreferences.getString("distance", "");

        Config._loc_interval = parseInt(_loc_interval);
        Config._loc_distance = parseFloat(_loc_distance);

        String t = "Loc_interval:"+ Config._loc_interval / 1000 + " sec\n" +
                "Loc_distance:" + Config._loc_distance + " meter\n" +
                "Network provider: " + Config._enable_network_provider;
        Toast.makeText(getApplicationContext(),t, Toast.LENGTH_LONG).show();


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
        if(Config._enable_network_provider) {
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

        filename = StringUtil.DateToString(new Date(),"yyyyMMdd_HHmmss");
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
                    dist = MyActivityUtil.getTotalDistanceInDouble(list);

                    long t2 = System.currentTimeMillis();
                    if(dist<1000) {
                        String s1 = String.format("%.2f", dist);
                        if(s1.length()>4) s1=String.format("%.1f", dist);
                        tv_start_km.setText(s1);
                        tv_start_km_str.setText("미터");
                    } else {
                        String s1 = String.format("%.2f", dist/1000.0);
                        if(s1.length()>4) s1=String.format("%.1f", dist/1000.0);
                        tv_start_km.setText(s1);
                        tv_start_km_str.setText("킬로미터");
                    }

                    double  minpkm = MyActivityUtil.getMinPerKm(list);
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