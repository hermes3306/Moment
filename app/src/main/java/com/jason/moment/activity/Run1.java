package com.jason.moment.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
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
import com.jason.moment.util.MP3;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.RunStat;
import com.jason.moment.util.StringUtil;
import com.jason.moment.util.db.MyActiviySummary;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Run1 extends Run implements
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
                PopupMenu p = new PopupMenu(Run1.this, v);
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

    public void registerLocationChangedReceiver() {
        // Register our broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.INTENT_LOCATION_CHANGED);
        registerReceiver(receiver, filter);
        Log.d(TAG, "-- INTENT LOCATION CHANGED registerReceiver()");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _ctx = this;
        set_use_db(false);
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
        unregisterReceiver(receiver);
        stopMyTimer();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"-- onBackPressed.");
        alertQuitDialog();
    }

    public void notificationSimpleQuit(int _id, String title, String detail) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(title);
        builder.setContentText(detail);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }
        notificationManager.notify(_id, builder.build());
    }

    private void removeNotification(int _id) {
        // Notification 제거
        NotificationManagerCompat.from(this).cancel(_id);
    }

    public void notificationQuit(int _id, String ticker, String title, String detail) {
        Intent intent = new Intent(_ctx, MyReportActivity.class);

        intent.putExtra("activity_file_name", activity_file_name);
        //intent.putExtra("activity_file_name", "20210502_092412");
        PendingIntent contentIntent = PendingIntent.getActivity(_ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(_ctx,"default");
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(detail)
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");
        NotificationManager notificationManager = (NotificationManager) _ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }
        notificationManager.notify(_id, b.build());
    }

    void deleteIfExistsUnsaved() {
        File f = new File(Config.JSN_SAVE_DIR, Config.Unsaved_File_name);
        if(f.exists()) f.delete();
    }

    static boolean activity_quit_normally = false;
    public void alertQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("활동을 중지하시겠습니까?");
        builder.setMessage("활동을 정말 중지하시겠습니까?");
        builder.setPositiveButton("중지",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity_quit_normally = true;
                        Config.restore_preference_values_after_running(getApplicationContext());
                        deleteIfExistsUnsaved();
                        if(gpsLoggerConnection != null)  {
                            unbindService(gpsLoggerConnection);
                            gpsLoggerConnection = null;
                        }
                        Log.d(TAG,"-- Service gpsLogger unbound...");

                        MyActivityUtil.serialize(list, media_filenames, activity_file_name );
                        CloudUtil.getInstance().Upload(activity_file_name + Config._csv_ext);
                        ActivityStat as = ActivityStat.getActivityStat(list);
                        if(as !=null) {
                            MyActiviySummary.getInstance(_ctx).ins(activity_file_name,as.distanceKm,as.durationInLong,as.minperKm,as.calories);
                            Log.d(TAG,"-- Activity Stat inserted successfully !!!!");
                            if(Config._default_ext==Config._csv)
                                CloudUtil.getInstance().Upload(activity_file_name + Config._csv_ext);
                            else
                                CloudUtil.getInstance().Upload(activity_file_name + Config._mnt_ext);
                        }

                        if(as != null) {
                            Toast.makeText(getApplicationContext(), "JASON's 활동이 저장되었습니다!" + activity_file_name, Toast.LENGTH_SHORT).show();
                            String detail = "총운동 거리:" + tv_start_km.getText();
                            detail += "\n총운동 시간:" + tv_start_time.getText();
                            detail += "\n평균 분/Km:" + tv_start_avg.getText();
                            detail += "\n소모칼로리:" + tv_start_calory.getText();
                            notificationQuit(Config._notify_id, Config._notify_ticker,
                                    "활동이 저장되었습니다.", detail);

                            Intent myReportIntent = new Intent(Run1.this, MyReportActivity.class);
                            myReportIntent.putExtra("activity_file_name", activity_file_name);
                            startActivity(myReportIntent);
                        }

                        Run1.this.quit = true;
                        Run1.this.finish();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Run1.this.quit = false;
                    }
                });
        builder.show();
    }

    static TimerTask mTask = null;
    Timer mTimer = null;
    private void startMyTimer() {
        mTask = new Run1.MyTimerTask();
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
            Run1.this.runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void run() {
                    process_new_location();
                }
            });
        } /* end of run() */
    } /* end of MyTimerTask */




}