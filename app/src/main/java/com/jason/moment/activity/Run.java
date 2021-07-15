package com.jason.moment.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.GoogleMap;
import com.jason.moment.ConfigActivity;
import com.jason.moment.R;
import com.jason.moment.service.GPSLogger;
import com.jason.moment.util.AlertDialogUtil;
import com.jason.moment.util.C;
import com.jason.moment.util.Config;
import com.jason.moment.util.MP3;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.RunStat;

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
    public ArrayList list = null;
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
    public static boolean activity_quit_normally = false;
    private long currentRunId;
    public long getCurrentRunId() {
        return this.currentRunId;
    }
    public void setCurrentRunId(long id) {this.currentRunId = id;}


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
    private String currentTrackId;
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
        ImageButton imb_wifi_off = (ImageButton) findViewById(R.id.imbt_wifi_off);
        ImageButton imb_wifi_on = (ImageButton) findViewById(R.id.imbt_wifi_on);
        imb_wifi_on.setVisibility(View.VISIBLE);
        imb_wifi_off.setVisibility(View.GONE);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                Run.this.runOnUiThread(new Runnable() {
                    public void run() {
                        imb_wifi_on.setVisibility(View.GONE);
                        imb_wifi_off.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 500);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Resources r = getResources();
        String[] screen_layout = r.getStringArray(R.array.start_screen);
        String[] screen_layout_value = r.getStringArray(R.array.start_screen);

        int id = item.getItemId();
        switch (id) {
            case R.id.showallmarkers:
                C.showallmarkers = !C.showallmarkers;
                break;
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

    public void showVideos(int pos) {
        AlertDialogUtil.getInstance().showMedias(_ctx, mov_filenames, pos);
    }

    private void showVideo(String fname) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(Run.this);
        LayoutInflater factory = LayoutInflater.from(Run.this);

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


    public void showImages(final int pos) {
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

    @Override
    public void onPause() {
        paused = true;
        resume = false;
        if(!activity_quit_normally && list != null) {
            if(list.size() > 5 ) { //최소 이상 정보가 있을 경우 저장
                File lastRun = new File(Config.CSV_SAVE_DIR, Config.Unsaved_File_name);
                MyActivityUtil.serializeIntoCSV(list, media_filenames, lastRun);
            }
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        paused = false;
        resume = true;
        if(true) {
            File lastRun = new File(Config.CSV_SAVE_DIR, Config.Unsaved_File_name);
            if(lastRun.exists()) lastRun.delete();
        }
        super.onResume();
    }

}
