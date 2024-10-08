package com.jason.moment.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.jason.moment.R;
import com.jason.moment.service.GPSLogger;
import com.jason.moment.service.GPSLoggerConnection;
import com.jason.moment.util.AlertDialogUtil;
import com.jason.moment.util.C;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.RunStat;
import com.jason.moment.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/*
    Timer off
    GPSLogger 서비스를 받으면 리스트에 추가하는 단순 구조
 */

public class Run2 extends Run implements
        OnMapReadyCallback,
        View.OnClickListener {

    private void takePic() {
        currentMediaName = Config.getTmpPicName();
        File mediaFile = new File(Config.PIC_SAVE_DIR, currentMediaName);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        startActivityForResult(intent, Config.PICK_FROM_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "-- onActivityResult called!");
        if (resultCode != RESULT_OK) {
            Log.d(TAG, "-- RESULT_OK FALSE......!");
            return;
        }
        if (data == null) Log.d(TAG, "-- Intent data is null");
        //Bundle extras = data.getExtras();

        switch (requestCode) {
            case Config.PICK_FROM_CAMERA:
                Log.d(TAG, "-- PIC_FROM_CAMERA: ");
                //showImg(currentMediaName);
                CloudUtil.getInstance().Upload(currentMediaName);
                pic_filenames.add(currentMediaName);
                media_filenames.add(currentMediaName);
                break;
            case Config.PICK_FROM_VIDEO:
                Log.d(TAG, "-- PICK_FROM_VIDEO: ");
                //showVideo(currentMediaName);
                CloudUtil.getInstance().Upload(currentMediaName);
                mov_filenames.add(currentMediaName);
                media_filenames.add(currentMediaName);
                break;
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.name:
                AlertDialogUtil.getInstance().show_running_stat(_ctx, new RunStat(this, list, last_pk, getCurrentRunId()));
                break;
            case R.id.start_dash_ll_01:
            case R.id.start_dash_ll_02:
            case R.id.start_dash_ll_03:
            case R.id.start_dash_ll_04:
            case R.id.start_dash_ll_05:
                LinearLayout startActionBar = findViewById(R.id.startActionBar);
                LinearLayout action_menu_bar = findViewById(R.id.action_menu_bar);
                if (viewStartActionBar) {
                    startActionBar.setVisibility(View.VISIBLE);
                    action_menu_bar.setVisibility(View.VISIBLE);
                } else {
                    startActionBar.setVisibility(View.GONE);
                    action_menu_bar.setVisibility(View.GONE);
                }
                viewStartActionBar = !viewStartActionBar;
                break;
            case R.id.imb_start_media_view:
                showMedias(0);
                break;
            case R.id.iv_start_pause:
                alertQuitDialog();
                break;
            case R.id.imb_start_camera:
                takePic();
                break;
            case R.id.imb_start_movie:
                recordVideo();
                break;
            case R.id.tv_start_km:
                break;
            case R.id.broadcastNewStart:
                boardCastConfigChanged(1000, 1);
                break;
            case R.id.imb_start_list:
                //recordVideo();
                PopupMenu p = new PopupMenu(Run2.this, v);
                getMenuInflater().inflate(R.menu.menu_run4, p.getMenu());
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onOptionsItemSelected(item);
                    }
                });
                p.show();
                break;
        }
    }



    private void setHeadMessages() {
        TextView name = findViewById(R.id.name);
        TextView date_str = findViewById(R.id.date_str);
        Date d = new Date();
        name.setText(DateUtil.getActivityName(d));
        date_str.setText(DateUtil.getDateString(d));
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        C.getInstance().setGoogleMap(_ctx, googleMap);
        showActivities();
        initialize_views();
    }

    protected void initialize_Mapview(Bundle savedInstanceState) {
        setContentView(_default_layout);
        MapView mapView = findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        // mMap is null, when it created
        if (googleMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    /**
     * Receives Intent for new Location from GPS services
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(TAG, "-- Received intent " + intent.getAction());
            if (Config.INTENT_LOCATION_CHANGED.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Location location = (Location) extras.get("location");
                    Log.d(TAG, "-- New location received! ("+location.getLatitude() + "," + location.getLongitude()+")");
                    onLocationChanged(location);
                }
            }
        }
    };

    private void onLocationChanged(Location location) {
        new_location = location;
        if(!paused) showGPS();
        process_new_location();
    }

    void boardCastConfigChanged(long gpsLoggingInterval, long gpsLoggingMinDistance) {
        Intent intent = new Intent(Config.INTENT_CONFIG_CHANGE);
        intent.putExtra("gpsLoggingInterval", gpsLoggingInterval);
        intent.putExtra("gpsLoggingMinDistance", gpsLoggingMinDistance);
        sendBroadcast(intent);
        Log.e(TAG, "--INTENT_CONFIG_CHANGED message sent :");
        Log.e(TAG, "--gpsLoggingInterval:" + gpsLoggingInterval);
        Log.e(TAG, "--gpsLoggingMinDistance:" + gpsLoggingMinDistance);
    }

    // GPS Logger 관련 함수 들
    // 정리 필요함
    private String currentTrackId;
    GPSLogger gpsLogger = null;

    public String getCurrentTrackId() {
        return this.currentTrackId;
    }

    public void setGpsLogger(GPSLogger l) {
        this.gpsLogger = l;
    }

    public GPSLogger getGpsLogger() {
        return gpsLogger;
    }

    public void registerLocationChangedReceiver() {
        // Register our broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.INTENT_LOCATION_CHANGED);
        registerReceiver(receiver, filter);
        Log.d(TAG, "-- INTENT LOCATION CHANGED registerReceiver()");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this._ctx = this;
        set_use_db(false);
        set_use_broadcast(true);
        super.setCurrentRunId(-1);

        // 달리기 모드일 경우, 1초, 1미터로 셋팅함
        Config.initialize(getApplicationContext());
        start_time = new Date();

        registerLocationChangedReceiver();

        Config.init_preference_value_running_default(getApplicationContext());
        gpsLoggerServiceIntent = new Intent(this, GPSLogger.class);
        String today = DateUtil.today();
        currentTrackId = today;
        gpsLoggerServiceIntent.putExtra("activity_file_name", today);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, GPSLogger.class)); // 서비스 시작
        } else {
            startService(new Intent(this, GPSLogger.class)); // 서비스 시작
        }
        gpsLoggerConnection = new GPSLoggerConnection(this); // 서비스 바인딩
        bindService(gpsLoggerServiceIntent, gpsLoggerConnection, BIND_AUTO_CREATE);

        if (list == null) list = new ArrayList<>();

        super.onCreate(savedInstanceState);
        initialize_Mapview(savedInstanceState);
        activity_file_name = StringUtil.DateToString(new Date(), "yyyyMMdd_HHmmss");
        start_time = new Date();
        //startMyTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        stopMyTimer();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"-- onBackPressed.");
        alertQuitDialog();
    }

    static TimerTask mTask = null;
    Timer mTimer = null;
    private void startMyTimer() {
        mTask = new Run2.MyTimerTask();
        mTimer = new Timer();
        mTimer.schedule(mTask, 0, 1000);
    }

    private void stopMyTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
            mTask = null;
        }
    }

    // MyTimerTask can run even though the app run in background
    public class MyTimerTask extends TimerTask{
        public void run() {
            long start = System.currentTimeMillis();
            Run2.this.runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void run() {
                    process_new_location();
                }
            });
        } /* end of run() */
    } /* end of MyTimerTask */
}