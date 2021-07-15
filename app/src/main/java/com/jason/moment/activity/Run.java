package com.jason.moment.activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.jason.moment.ConfigActivity;
import com.jason.moment.MyReportActivity;
import com.jason.moment.R;
import com.jason.moment.service.GPSLogger;
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
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.RunStat;
import com.jason.moment.util.StringUtil;
import com.jason.moment.util.db.MyActiviySummary;
import com.jason.moment.util.db.MyLoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Run extends AppCompatActivity{

    public ArrayList<String> pic_filenames = new ArrayList<>();
    public ArrayList<String> mov_filenames = new ArrayList<>();
    public ArrayList<String> media_filenames = new ArrayList<>();
    public Intent gpsLoggerServiceIntent = null;
    public ServiceConnection gpsLoggerConnection = null;
    public Date start_time;
    public double dist = 0;
    public boolean quit = false;
    public ArrayList<MyActivity> list = null;
    public final MyActivity first = null;
    public MyActivity last = null;
    MyActivity last_activity = null;
    Location new_location = null;
    String activity_file_name = null;
    public String getActivity_file_name() {
        return activity_file_name;
    }
    public String TAG = "Run";
    public GoogleMap googleMap = null;
    String currentMediaName;
    public static boolean paused = false;
    public boolean resume = false;

    private long currentRunId;
    public long getCurrentRunId() {
        return this.currentRunId;
    }
    public void setCurrentRunId(long id) {this.currentRunId = id;}

    public TextView tv_start_km;
    public TextView tv_start_km_str;
    public TextView tv_start_time;
    public TextView tv_start_avg;
    public TextView tv_start_cur;
    public TextView tv_start_calory;
    int _default_layout = R.layout.activity_run_common;
    public boolean viewStartActionBar = false;

    GPSLogger gpsLogger = null;
    boolean use_db = false;
    public boolean get_use_db() {return use_db;}
    public void set_use_db(boolean b) {use_db = b;}
    public Context _ctx = null;

    public void setGpsLogger(GPSLogger l) {
        this.gpsLogger = l;
    }
    public GPSLogger getGpsLogger() {
        return gpsLogger;
    }
    public String currentTrackId;
    public String getCurrentTrackId() {
        return this.currentTrackId;
    }
    public boolean dashboard = true;
    static long last_pk = -1;

    static Timer timer = new Timer();
    public void showGPS() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
        }
        ImageButton imb_wifi_off = findViewById(R.id.imbt_wifi_off);
        ImageButton imb_wifi_on = findViewById(R.id.imbt_wifi_on);
        imb_wifi_on.setVisibility(View.VISIBLE);
        imb_wifi_off.setVisibility(View.GONE);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Run.this.runOnUiThread(new Runnable() {
                    public void run() {
                        imb_wifi_on.setVisibility(View.GONE);
                        imb_wifi_off.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 500);
    }


    public void showVideos(int pos) {
        AlertDialogUtil.getInstance().showMedias(_ctx, mov_filenames, pos);
    }

    public void showImages(final int pos) {
        AlertDialogUtil.getInstance().showMedias(_ctx, pic_filenames, pos);
    }

    public void showVideo(VideoView vv, String fname) {
        File mediaFile = new File(Config.MOV_SAVE_DIR, fname);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);
        vv.setVideoURI(mediaUri);
        vv.start();
    }



    public void recordVideo() {
        currentMediaName = Config.getTmpVideoName();
        File mediaFile = new File(Config.MOV_SAVE_DIR, currentMediaName);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        startActivityForResult(intent, Config.PICK_FROM_VIDEO);
    }


    private void get_last_run_from_db() {
        long cur_pk = LocationUtil.getInstance().get_last_pk();
        if(last_pk != -1 && last_pk < cur_pk) {
            Toast.makeText(_ctx, "Last pk: "
                    + last_pk + "\nCurrent pk: "
                    + cur_pk + "\n" + (cur_pk-last_pk) +
                    " gaps", Toast.LENGTH_LONG).show();

            Log.e(TAG, "----- HERE ----------");
            Log.e(TAG, "----- HAVE TO PROCESS from last_pk ----------");
            Log.e(TAG, "----- paused_last_pk : " + last_pk );
            Log.e(TAG, "----- current_last_pk : " + LocationUtil.getInstance().get_last_pk() );

            ArrayList<MyActivity> t = MyLoc.getInstance(_ctx).getActivitiesFrom(last_pk);
            for(int i=0;i<t.size();i++) {
                Log.e(TAG,"----- " + t.get(i).toString());
                list.add(t.get(i));
            }
        }
    }

    @Override
    public void onPause() {
        paused = true;
        resume = false;

        last_pk = LocationUtil.getInstance().get_last_pk();
        super.onPause();
    }

    @Override
    public void onResume() {
        paused = false;
        resume = true;

        get_last_run_from_db();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Config.restore_preference_values_after_running(getApplicationContext());
        removeNotification(Config._notify_id);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Resources r = getResources();
        String[] screen_layout = r.getStringArray(R.array.start_screen);

        int id = item.getItemId();
        switch (id) {
            case R.id.show_running_stat:
                AlertDialogUtil.getInstance().show_running_stat(_ctx, new RunStat(this, list));
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
                AlertDialog.Builder builder = new AlertDialog.Builder(Run.this)
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
                Intent configIntent = new Intent(Run.this, ConfigActivity.class);
                startActivity(configIntent);
                break;
            case R.id.action_map:
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

    public void showActivities() {
        setHeadMessages();
        ArrayList<MyActivity> mal = list;
        if (mal == null) return;
        if (mal.size() == 0) return;
        ArrayList<Marker> _markers = new ArrayList<>();
        Display display = getWindowManager().getDefaultDisplay();
        MapUtil.DRAW(_ctx, googleMap, display, list);
    }

    public void initialize_views() {
        tv_start_km         = findViewById(R.id.tv_start_km);
        tv_start_km_str     = findViewById(R.id.tv_start_km_str);
        tv_start_time       = findViewById(R.id.tv_start_time);
        tv_start_avg        = findViewById(R.id.tv_start_avg);
        tv_start_cur        = findViewById(R.id.tv_start_cur);
        tv_start_calory         = findViewById(R.id.tv_start_calory);
    }

    public void process_new_location() {
        Date d = new Date();
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
            String elapsed = StringUtil.elapsedStr(start_time,d);
            tv_start_time.setText(elapsed);
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

    public void showImg(ImageView iv_pic, String fname) {
        File folder = Config.PIC_SAVE_DIR;
        File file = new File(folder, fname);
        String filepath = file.getAbsolutePath();

        Log.d(TAG, "--show:" + filepath);
        Log.d(TAG, "--filepath to show:" + filepath);
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        iv_pic.setImageBitmap(bitmap);
    }

    public void showMedias(final int pos) {
        if (media_filenames.size() < pos + 1) {
            Toast.makeText(_ctx, "No Medias!", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder alertadd = new AlertDialog.Builder(Run.this);
        LayoutInflater factory = LayoutInflater.from(Run.this);

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

    public void removeNotification(int _id) {
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
                        Config.restore_preference_values_after_running(getApplicationContext());
                        if(gpsLoggerConnection != null)  {
                            set_use_db(false);
                            unbindService(gpsLoggerConnection);
                            gpsLoggerConnection = null;
                        }

                        MyActivityUtil.serialize(list, media_filenames, activity_file_name );
                        CloudUtil.getInstance().Upload(activity_file_name + Config._csv_ext);

                        Toast.makeText(_ctx, " " + list.size() + " locations saved!", Toast.LENGTH_SHORT).show();

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

                            Intent myReportIntent = new Intent(Run.this, MyReportActivity.class);
                            myReportIntent.putExtra("activity_file_name", activity_file_name);
                            startActivity(myReportIntent);
                        }

                        Run.this.quit = true;
                        Run.this.finish();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Run.this.quit = false;
                    }
                });
        builder.show();
    }

}
