package com.jason.moment.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.jason.moment.ConfigActivity;
import com.jason.moment.MyReportActivity;
import com.jason.moment.R;
import com.jason.moment.service.GPSLogger;
import com.jason.moment.service.GPSLoggerConnection;
import com.jason.moment.util.ActivityStat;
import com.jason.moment.util.AlertDialogUtil;
import com.jason.moment.util.C;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CaloryUtil;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.LocationUtil;
import com.jason.moment.util.MP3;

import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivity2;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.MyRunInfo;
import com.jason.moment.util.RunStat;
import com.jason.moment.util.StringUtil;
import com.jason.moment.util.db.MyRun;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class Run4 extends Run implements
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
                PopupMenu p = new PopupMenu(Run4.this, v);
                getMenuInflater().inflate(R.menu.start_menu, p.getMenu());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Resources r = getResources();
        String[] screen_layout = r.getStringArray(R.array.start_screen);
        String[] screen_layout_value = r.getStringArray(R.array.start_screen);

        int id = item.getItemId();
        switch (id) {
            case R.id.show_running_stat:
                AlertDialogUtil.getInstance().show_running_stat(_ctx, new RunStat(this, list,last_pk, getCurrentRunId()));
                return true;
            case R.id.showallmarkers:
                C.showallmarkers = !C.showallmarkers;
                return true;
            case R.id.toggleDashboard:
                dashboard = ! dashboard;
                LinearLayout ll01 = findViewById(R.id.start_dash_ll_01);
                LinearLayout ll02 = findViewById(R.id.start_dash_ll_02);
                LinearLayout ll03 = findViewById(R.id.start_dash_ll_03);
                LinearLayout ll04 = findViewById(R.id.start_dash_ll_04);
                LinearLayout ll05 = findViewById(R.id.start_dash_ll_05);
                if(dashboard) {
                    ll01.setVisibility(View.VISIBLE);
                    ll02.setVisibility(View.VISIBLE);
                    ll03.setVisibility(View.VISIBLE);
                    ll04.setVisibility(View.VISIBLE);
                    ll05.setVisibility(View.VISIBLE);
                }else {
                    ll01.setVisibility(View.GONE);
                    ll02.setVisibility(View.GONE);
                    ll03.setVisibility(View.GONE);
                    ll04.setVisibility(View.GONE);
                    ll05.setVisibility(View.GONE);
                }
                return true;
            case R.id.mp3Player:
                MP3.showPlayer(_ctx);
                return true;
            case R.id.stopMp3:
                MP3.stop(_ctx);
                return true;
            case R.id.start_layout_select:
                AlertDialog.Builder builder = new AlertDialog.Builder(Run4.this)
                        .setItems(screen_layout, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .setTitle("Choose a layout");
                AlertDialog mSportSelectDialog = builder.create();
                mSportSelectDialog.show();
                break;
            case R.id.imSetting:
                Log.d(TAG, "-- Setting Activities!");
                Intent configIntent = new Intent(Run4.this, ConfigActivity.class);
                startActivity(configIntent);
                break;
            case R.id.action_map:
                int i = 0;
                break;
            case R.id.record_video:
                recordVideo();
                break;
            case R.id.view_pics:
                showImages(0);
                break;
            case R.id.view_videos:
                showVideos(0);
                break;
        }
        return super.onOptionsItemSelected(item);
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

    void boardCastConfigChanged(long gpsLoggingInterval, long gpsLoggingMinDistance) {
        Intent intent = new Intent(Config.INTENT_CONFIG_CHANGE);
        intent.putExtra("gpsLoggingInterval", gpsLoggingInterval);
        intent.putExtra("gpsLoggingMinDistance", gpsLoggingMinDistance);
        sendBroadcast(intent);
        Log.e(TAG, "--INTENT_CONFIG_CHANGED message sent :");
        Log.e(TAG, "--gpsLoggingInterval:" + gpsLoggingInterval);
        Log.e(TAG, "--gpsLoggingMinDistance:" + gpsLoggingMinDistance);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this._ctx = this;
        // MyRun 테이블을 사용할 경우 set_use_db(true)
        set_use_db(true);
        set_use_broadcast(false);
        // 달리기 모드일 경우, 1초, 1미터로 셋팅함
        Config.initialize(getApplicationContext());
        Config.init_preference_value_running_default(getApplicationContext());

        MyRunInfo myRunInfo = MyRun.getInstance(_ctx).notFinishedRun();
        if(myRunInfo!=null) {
            // 마치지 못한 러닝이 있는 경우, 다시 시작하도록 한다.
            start_time = myRunInfo.cr_date;
            super.setCurrentRunId(myRunInfo.run_id);
            Toast.makeText(_ctx,"Restarting not saved run(" +  myRunInfo.run_id + ")", Toast.LENGTH_LONG).show();
        }else {
            start_time = new Date();
            super.setCurrentRunId(start_time.getTime());
        }

        gpsLoggerServiceIntent = new Intent(this, GPSLogger.class);
        gpsLoggerServiceIntent.putExtra(GPSLogger.RUN_ID, super.getCurrentRunId());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, GPSLogger.class)); // 서비스 시작
        } else {
            startService(new Intent(this, GPSLogger.class)); // 서비스 시작
        }
        gpsLoggerConnection = new GPSLoggerConnection(this); // 서비스 바인딩
        bindService(gpsLoggerServiceIntent, gpsLoggerConnection, 0);

        if (list == null) list = new ArrayList<>();

        super.onCreate(savedInstanceState);
        initialize_Mapview(savedInstanceState);
        activity_file_name = StringUtil.DateToString(new Date(), "yyyyMMdd_HHmmss");

        startMyTimer();
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
        mTask = new MyTimerTask();
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
            Run4.this.runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void run() {
                    if(paused) return;

                    Date d = new Date();
                    String elapsed = StringUtil.elapsedStr(start_time,d);
                    tv_start_time.setText(elapsed);

                    if (use_db) {
                        ArrayList<MyActivity2> l2 = MyRun.getInstance(_ctx).qry_from_last_pk(getCurrentRunId(), last_pk);
                        if(l2==null) return;
                        if(l2.size()==0) return;
                        if(!paused) showGPS();
                        last_pk = MyRun.getInstance(_ctx).get_last_pk(getCurrentRunId());
                        for(int i=0;i<l2.size();i++) list.add(new MyActivity(l2.get(i)));
                    }

                    if(resume) resume=false;
                    if(googleMap!=null && !paused)showActivities();
                    if(!paused) {
                        dist = MyActivityUtil.getTotalDistanceInDouble(list);
                        if(dist<1000) { /* 1KM 이하 */
                            String s1 = String.format("%.0f", dist);
                            tv_start_km.setText(s1);
                            tv_start_km_str.setText("Meters");
                        } else if(dist>1000) { /* 1KM 이상 */
                            String s1 = String.format("%.2f", dist/1000.0);
                            tv_start_km.setText(s1);
                            tv_start_km_str.setText("Kilometers");
                        } else if(dist >10000){ /* 10KM 이상*/
                            String s1 = String.format("%.3f", dist/1000.0);
                            tv_start_km.setText(s1);
                            tv_start_km_str.setText("Kilometers");
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
                        burntkCal = CaloryUtil.calculateEnergyExpenditure((float)dist / 1000f, durationInSeconds);
                        tv_start_calory.setText("" + String.format("%.1f", burntkCal));
                    }
                }
            });
        } /* end of run() */
    } /* end of MyTimerTask */
}
