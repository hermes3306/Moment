package com.jason.quote.activity.run;

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
import com.jason.quote.R;
import com.jason.quote.activity.MyReportActivity;
import com.jason.quote.activity.SettingsActivity;
import com.jason.quote.service.GPSLogger;
import com.jason.quote.service.GPSLoggerConnection;
import com.jason.quote.util.ActivityStat;
import com.jason.quote.util.AlertDialogUtil;
import com.jason.quote.util.C;
import com.jason.quote.util.CalDistance;
import com.jason.quote.util.CaloryUtil;
import com.jason.quote.util.CloudUtil;
import com.jason.quote.util.Config;
import com.jason.quote.util.DateUtil;
import com.jason.quote.util.LocationUtil;
import com.jason.quote.util.MP3;
import com.jason.quote.util.MapUtil;
import com.jason.quote.util.MyActivity;
import com.jason.quote.util.MyActivityUtil;
import com.jason.quote.util.StringUtil;
import com.jason.quote.util.db.MyActiviySummary;
import com.jason.quote.util.db.MyRun;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class Run3 extends Run implements
        OnMapReadyCallback,
        View.OnClickListener {

    ImageButton imb_wifi_off;
    ImageButton imb_wifi_on;

    String TAG = "Run1";
    Context _ctx = null;
    int _default_layout = R.layout.activity_run_common;

    private GoogleMap googleMap = null;
    private TextView tv_start_km;
    private TextView tv_start_km_str;
    private TextView tv_start_time;
    private TextView tv_start_avg;
    private TextView tv_start_cur;
    private TextView tv_start_calory;

    private Date start_time;
    private double dist = 0;
    private MyActivity last = null;
    boolean quit=false;

    public String getActivity_file_name() {
        return activity_file_name;
    }

    // 사진 촬영 기능
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentMediaName;
    Uri currentFileUri;

    private void recordVideo() {
        currentMediaName = Config.getTmpVideoName();
        File mediaFile = new File(Config.MOV_SAVE_DIR, currentMediaName);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        startActivityForResult(intent, Config.PICK_FROM_VIDEO);
    }

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

    public void showImg(ImageView iv_pic, String fname) {
        File folder = Config.PIC_SAVE_DIR;
        File file = new File(folder, fname);
        String filepath = file.getAbsolutePath();

        Log.d(TAG, "--show:" + filepath);
        Log.d(TAG, "--filepath to show:" + filepath);
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        int mDegree = 90;
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        iv_pic.setImageBitmap(bitmap);
    }

    private void showImg(String fname) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(Run3.this);
        LayoutInflater factory = LayoutInflater.from(Run3.this);

        /// View를 inflate하면 해당 View내의 객체를 접근하려면 해당  view.findViewById를 호출 해야 함
        final View view = factory.inflate(R.layout.layout_imageview, null);
        ImageView iv = view.findViewById(R.id.dialog_imageview);
        showImg(iv, fname);
        alertadd.setView(view);
        alertadd.setNeutralButton("Upload!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {
                CloudUtil.getInstance().Upload(currentMediaName);
            }
        });
        alertadd.show();
    }

    private void showMedias(final int pos) {
        if (media_filenames.size() < pos + 1) {
            Toast.makeText(_ctx, "No Medias!", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder alertadd = new AlertDialog.Builder(Run3.this);
        LayoutInflater factory = LayoutInflater.from(Run3.this);

        /// View를 inflate하면 해당 View내의 객체를 접근하려면 해당  view.findViewById를 호출 해야 함
        if (media_filenames.get(pos).endsWith(Config._pic_ext)) {
            View view1 = factory.inflate(R.layout.layout_imageview, null);
            ImageView iv = view1.findViewById(R.id.dialog_imageview);
            TextView tv = view1.findViewById(R.id.view_title);
            tv.setText("" + (pos + 1) + "/" + media_filenames.size());
            showImg(iv, media_filenames.get(pos));
            alertadd.setView(view1);
        } else {
            View view2 = factory.inflate(R.layout.layout_videoview, null);
            VideoView vv = view2.findViewById(R.id.dialog_video_view);
            TextView tv2 = view2.findViewById(R.id.view_title);
            tv2.setText("" + (pos + 1) + "/" + media_filenames.size());
            showVideo(vv, media_filenames.get(pos));
            alertadd.setView(view2);
        }

        if (media_filenames.size() > pos + 1) {
            alertadd.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    showMedias(pos + 1);
                }
            });
        }

        if (0 < pos) {
            alertadd.setNegativeButton("Prev", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    showMedias(pos - 1);
                }
            });
        }
        alertadd.show();
    }

    private void showImages(final int pos) {
        AlertDialogUtil.getInstance().showMedias(_ctx, pic_filenames, pos);
    }

    public void showVideo(VideoView vv, String fname) {
        MediaController m;
        m = new MediaController(this);

        File mediaFile = new File(Config.MOV_SAVE_DIR, fname);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);
        vv.setVideoURI(mediaUri);
        vv.start();
    }

    private void showVideos(int pos) {
        AlertDialogUtil.getInstance().showMedias(_ctx, mov_filenames, pos);
    }

    private void showVideo(String fname) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(Run3.this);
        LayoutInflater factory = LayoutInflater.from(Run3.this);

        /// View를 inflate하면 해당 View내의 객체를 접근하려면 해당  view.findViewById를 호출 해야 함
        final View view = factory.inflate(R.layout.layout_videoview, null);
        VideoView vv = view.findViewById(R.id.dialog_video_view);
        showVideo(vv, fname);
        alertadd.setView(view);
        alertadd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {
            }
        });
        alertadd.show();
    }

    private boolean viewStartActionBar = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                PopupMenu p = new PopupMenu(Run3.this, v);
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
            case R.id.mp3Player:
                MP3.showPlayer(_ctx);
                return true;
            case R.id.stopMp3:
                MP3.stop(_ctx);
                return true;
            case R.id.start_layout_select:
                AlertDialog.Builder builder = new AlertDialog.Builder(Run3.this)
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
                Intent configIntent = new Intent(Run3.this, SettingsActivity.class);
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

    private void showActivities() {
        setHeadMessages();
        ArrayList<MyActivity> mal = list;
        MyActivity lastActivity = null;
        if (mal == null) return;
        if (mal.size() == 0) return;
        ArrayList<Marker> _markers = new ArrayList<>();
        Display display = getWindowManager().getDefaultDisplay();
        MapUtil.DRAW(_ctx, googleMap, display, list);
    }

    private void initialize_views() {
        tv_start_km = (TextView) findViewById(R.id.tv_start_km);
        tv_start_km_str = (TextView) findViewById(R.id.tv_start_km_str);
        tv_start_time = (TextView) findViewById(R.id.tv_start_time);
        tv_start_avg = (TextView) findViewById(R.id.tv_start_avg);
        tv_start_cur = (TextView) findViewById(R.id.tv_start_cur);
        tv_start_calory = (TextView) findViewById(R.id.tv_start_calory);
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
        set_use_db(true);

        // 달리기 모드일 경우, 1초, 1미터로 셋팅함
        Config.initialize(getApplicationContext());

        File lastRun = new File(Config.CSV_SAVE_DIR, Config.Unsaved_File_name);
        if (lastRun.exists()) {
            Log.e(TAG, "-- Restarting Running with last data....");
            Toast.makeText(_ctx, "Unsaved run!!!", Toast.LENGTH_SHORT).show();
            list = MyActivityUtil.deserializeFromCSV(lastRun);
            lastRun.delete();
            if (list.size() > 0) {
                last_activity = (MyActivity) list.get(list.size() - 1);
                start_time = StringUtil.StringToDate((MyActivity)list.get(0));
            } else {
                start_time = new Date();
            }
            Toast.makeText(_ctx, Config.Unsaved_File_name + " converted into current running!!!", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "-- Normal Running....");
            start_time = new Date();
        }

        Config.init_preference_value_running_default(getApplicationContext());
        gpsLoggerServiceIntent = new Intent(this, GPSLogger.class);

        super.setCurrentRunId(new Date().getTime());
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


    static long last_pk = -1;
    static long start_pk = -1;

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        get_last_run_from_db();
        super.onResume();
    }

    public void get_last_run_from_db() {

        if(last_pk != -1 && last_pk < LocationUtil.getInstance().get_last_pk()) {
            Log.e(TAG, "----- HERE ----------");
            Log.e(TAG, "----- HAVE TO PROCESS from last_pk ----------");
            Log.e(TAG, "----- paused_last_pk : " + last_pk );
            Log.e(TAG, "----- current_last_pk : " + LocationUtil.getInstance().get_last_pk() );

            ArrayList<MyActivity> t = MyRun.getInstance(_ctx).qry_from_last_pk(last_pk);
            for(int i=0;i<t.size();i++) {
                Log.e(TAG,"----- " + t.get(i).toString());
                list.add(t.get(i));
            }
        }
    }

    @Override
    public void onDestroy() {
        stopMyTimer();

        if(!activity_quit_normally) {
            File lastRun = new File(Config.CSV_SAVE_DIR, Config.Unsaved_File_name);
            MyActivityUtil.serializeIntoCSV(list, media_filenames, lastRun );
            Config.restore_preference_values_after_running(getApplicationContext());
            Toast.makeText(_ctx,"Running activity saved into " + Config.Unsaved_File_name + " !!", Toast.LENGTH_SHORT).show();
        } else {
            set_use_db(false);
            if (gpsLoggerConnection != null) unbindService(gpsLoggerConnection);
        }
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
                            set_use_db(false);
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

                            Intent myReportIntent = new Intent(Run3.this, MyReportActivity.class);
                            myReportIntent.putExtra("activity_file_name", activity_file_name);
                            startActivity(myReportIntent);
                        }

                        Run3.this.quit = true;
                        Run3.this.finish();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Run3.this.quit = false;
                    }
                });
        builder.show();
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
            Run3.this.runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void run() {
                    if(paused) return;

                    Date d = new Date();
                    String elapsed = StringUtil.elapsedStr(start_time,d);
                    tv_start_time.setText(elapsed);

                    new_location = LocationUtil.getInstance().last_location();
                    Location location = new_location;

                    if(location == null) return;

                    if(resume) {
                        showActivities();
                        resume = false;
                    }

                    if(last_activity==null) {
                        dist = 0;
                        last = new MyActivity(location.getLatitude(), location.getLongitude(),d);
                        list.add(last);
                        last_activity = last;
                    }else {
                        dist = CalDistance.dist(last_activity.getLatitude(), last_activity.getLongitude(), location.getLatitude(), location.getLongitude());
                        if(dist > Config._loc_distance) {
                            last = new MyActivity(location.getLatitude(), location.getLongitude(),d);
                            list.add(last);
                            last_activity = last;
                            if(googleMap != null && ! paused) showActivities();
                        }
                    }

                    //Log.e(TAG, "-- Timer!");

                    if(!paused) {
                        long t1 = System.currentTimeMillis();
                        dist = MyActivityUtil.getTotalDistanceInDouble(list);
                        long t2 = System.currentTimeMillis();
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
