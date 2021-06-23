package com.jason.moment;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.TextViewCompat;
import androidx.preference.PreferenceManager;

import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CaloryUtil;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.NotificationUtil;
import com.jason.moment.util.StringUtil;

import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class StartActivity extends AppCompatActivity implements View.OnClickListener{
    public static String TAG = "StartActivity";

    public ArrayList<String> pic_filenames = new ArrayList<>();
    public ArrayList<String> mov_filenames = new ArrayList<>();

    int[] start_layout = {
            R.layout.activity_start_style3,
            R.layout.activity_start_style1,
            R.layout.activity_start_style2
    };

    public static Context _ctx;
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
    public String activity_file_name;

    public LocationManager mLocManager = null;

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
        if(resultCode != RESULT_OK) {
            Log.d(TAG, "-- RESULT_OK FALSE......!");
            return;
        }
        if(data==null) Log.d(TAG, "-- Intent data is null");
        //Bundle extras = data.getExtras();

        switch(requestCode) {
            case Config.PICK_FROM_CAMERA:
                Log.d(TAG, "-- PIC_FROM_CAMERA: ");
                //showImg(currentMediaName);
                CloudUtil.getInstance().Upload(currentMediaName);
                break;
            case Config.PICK_FROM_VIDEO:
                Log.d(TAG, "-- PICK_FROM_VIDEO: ");
                showVideo(currentMediaName);
                CloudUtil.getInstance().Upload(currentMediaName);
                break;
        }
    }

    public void showImg(ImageView iv_pic, String fname) {
        File folder= Config.PIC_SAVE_DIR;
        File file = new File(folder,fname);
        String filepath = file.getAbsolutePath();

        Log.d(TAG,"--show:"+filepath);
        Log.d(TAG, "--filepath to show:" + filepath);
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        int mDegree = 90;
        bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(),matrix,true);
        iv_pic.setImageBitmap(bitmap);
    }

    private void showImg(String fname) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(StartActivity.this);
        LayoutInflater factory = LayoutInflater.from(StartActivity.this);

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

    public void showVideo(VideoView vv, String fname) {
        MediaController m;
        m = new MediaController(this);

        File mediaFile = new File(Config.MOV_SAVE_DIR, currentMediaName);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);
        vv.setVideoURI(mediaUri);
        vv.start();
    }

    private void showVideo(String fname) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(StartActivity.this);
        LayoutInflater factory = LayoutInflater.from(StartActivity.this);

        /// View를 inflate하면 해당 View내의 객체를 접근하려면 해당  view.findViewById를 호출 해야 함
        final View view = factory.inflate(R.layout.layout_videoview, null);
        VideoView vv = view.findViewById(R.id.dialog_video_view);
        showVideo(vv, fname);
        alertadd.setView(view);
        alertadd.setPositiveButton("Upload!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {

            }
        });
        alertadd.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_start_pause:
                alertQuitDialog();
                break;
            case R.id.imb_start_camera:
                takePic();
                break;
            case R.id.tv_start_km:
                break;
            case R.id.imb_start_list:
                //recordVideo();
                PopupMenu p = new PopupMenu(StartActivity.this, v);
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
        switch(id) {
            case R.id.start_layout_select:
                AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this )
                        .setItems(screen_layout, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                initializeContentViews(start_layout[i]);
                                //Toast.makeText(getApplicationContext(),screen_layout[i], Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setTitle("Choose a layout");
//                    .setPositiveButton("OK",null)
//                    .setNegativeButton("Cancel",null);
                AlertDialog mSportSelectDialog = builder.create();
                mSportSelectDialog.show();
                break;
            case R.id.imSetting:
                Log.d(TAG,"-- Setting Activities!");
                Intent configIntent = new Intent(StartActivity.this, ConfigActivity.class);
                configIntent.putExtra("1", 1);
                startActivityForResult(configIntent, Config.CALL_SETTING_ACTIVITY);
                break;
            case R.id.action_map:
                int i=0;
                break;
            case R.id.record_video:
                recordVideo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private class GPSListener implements LocationListener {
        public GPSListener(String gpsProvider) {
        }

        private Location lastloc = null;
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.d(TAG,"-- onLocationChanged! [" + location.getProvider() + "]" +location.getLatitude() + "," + location.getLongitude());
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
                if(dist > Config._loc_distance) {
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

        try {
            Config._loc_interval = parseInt(_loc_interval);
            Config._loc_distance = parseFloat(_loc_distance);
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        String t = "Loc_interval:"+ Config._loc_interval / 1000 + " sec\n" +
                "Loc_distance:" + Config._loc_distance + " meter\n" +
                "Network provider: " + Config._enable_network_provider;
        //Toast.makeText(getApplicationContext(),t, Toast.LENGTH_LONG).show();
        Log.d(TAG,t);

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
        } catch (java.lang.SecurityException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        } catch (IllegalArgumentException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        if(Config._enable_network_provider) {
            try {
                mLocManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        Config._loc_interval,
                        Config._loc_distance,
                        mLocationListeners[1]
                );
            } catch (java.lang.SecurityException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG,"Err:" + sw.toString());
            } catch (IllegalArgumentException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG,"Err:" + sw.toString());
            }
        }
    }

    private void initializeContentViews(int layout) {
        setContentView(layout);
        tv_start_km = (TextView)findViewById(R.id.tv_start_km);
        tv_start_km_str = (TextView)findViewById(R.id.tv_start_km_str);
        tv_start_time = (TextView)findViewById(R.id.tv_start_time);
        tv_start_avg = (TextView)findViewById(R.id.tv_start_avg);
        tv_start_cur = (TextView)findViewById(R.id.tv_start_cur);
        tv_start_calory = (TextView)findViewById(R.id.tv_start_calory);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _ctx = this;
        Config.initialize(getApplicationContext());

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_start);

        int inx = Config.getIntPreference(getApplicationContext(),"start_screen");
        //setContentView();
        initializeContentViews(start_layout[inx]);

        initializeLocationManager();
        activity_file_name = StringUtil.DateToString(new Date(),"yyyyMMdd_HHmmss");
        startMyTimer();
        start_time = new Date();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "-- onPause.");
        MyActivityUtil.serialize(list, activity_file_name );
        NotificationUtil.notify_new_activity(_ctx, activity_file_name);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "-- onResume.");
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

    public void alertQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("활동을 중지하시겠습니까?");
        builder.setMessage("활동을 정말 중지하시겠습니까?");
        builder.setPositiveButton("중지",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MyActivityUtil.serialize(list, activity_file_name );
                        Toast.makeText(getApplicationContext(), "JASON's 활동이 저장되었습니다!" + activity_file_name, Toast.LENGTH_SHORT).show();

                        String detail = "총운동 거리:" + tv_start_km.getText();
                        detail+= "\n총운동 시간:" + tv_start_time.getText();
                        detail+= "\n평균 분/Km:" + tv_start_avg.getText();
                        detail+= "\n소모칼로리:" + tv_start_calory.getText();
                        notificationQuit(Config._notify_id,Config._notify_ticker,
                                "활동이 저장되었습니다.", detail);
                        deleteLocationManager();

                        Intent myReportIntent = new Intent(StartActivity.this, MyReportActivity.class);
                        myReportIntent.putExtra("activity_file_name", activity_file_name);
                        startActivity(myReportIntent);

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
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void run() {
                    Date d = new Date();
                    String elapsed = StringUtil.elapsedStr(start_time,d);
                    tv_start_time.setText(elapsed);
                    long t1 = System.currentTimeMillis();
                    dist = MyActivityUtil.getTotalDistanceInDouble(list);
                    long t2 = System.currentTimeMillis();
                    if(dist<1000) { /* 1KM 이하 */
                        String s1 = String.format("%.0f", dist);
                        tv_start_km.setText(s1);
                        tv_start_km_str.setText("Meters");
                    } else if(dist>1000) { /* 1KM 이상 */
                        String s1 = String.format("%.1f", dist/1000.0);
                        tv_start_km.setText(s1);
                        tv_start_km_str.setText("Kilometers");
                    } else if(dist >10000){ /* 10KM 이상*/
                        String s1 = String.format("%.1f", dist/1000.0);
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
                    if(last==null) {
                        last = new Date();
                        MyActivityUtil.serialize(list, activity_file_name );
                    }else {
                        Date now = new Date();
                        if(DateUtil.isLongerThan1Min(last, now)) {
                            MyActivityUtil.serialize(list, activity_file_name );
                            last = now;
                        }
                    }
                }
            });
        } /* end of run() */
    } /* end of MyTimerTask */
}