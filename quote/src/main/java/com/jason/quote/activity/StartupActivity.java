package com.jason.quote.activity;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.jason.quote.ItemDetailHostActivity;
import com.jason.quote.R;
import com.jason.quote.activity.myrun.MainActivity;
import com.jason.quote.databinding.ActivityStartupBinding;
import com.jason.quote.util.ActivityStat;
import com.jason.quote.util.CloudUtil;
import com.jason.quote.util.Config;
import com.jason.quote.util.DateUtil;
import com.jason.quote.util.MP3;
import com.jason.quote.util.MyActivity;
import com.jason.quote.util.MyActivityUtil;
import com.jason.quote.util.PermissionUtil;
import com.jason.quote.util.db.MyActiviySummary;
import com.jason.quote.util.db.MyLoc;
import com.jason.quote.util.db.MyMedia;
import com.jason.quote.util.db.MyRun;

import java.io.File;
import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class StartupActivity extends AppCompatActivity implements View.OnClickListener{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private ActivityStartupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStartupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContent;

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.dummyButton.setOnTouchListener(mDelayHideTouchListener);

        // Startup
        Config.initialize(this);
        PermissionUtil.getInstance().setPermission(this);

        CloudUtil.getInstance().DownloadAll(this, Config._csv);
        CloudUtil.getInstance().DownloadAll(this, Config._img);
        CloudUtil.getInstance().DownloadAll(this, Config._mov);
        MyLoc.getInstance(this).onCreate();
        MyMedia.getInstance(this).createNew();
        MyActiviySummary.getInstance(this).createNew();
        MyRun.getInstance(this).createNew();
        rebuildActivitySummaries(this);
        ImportTodayActivity("Jason");



    }

    public void ImportTodayActivity(String runner_name) {
        String today = DateUtil.today();
        File f = new File(Config.CSV_SAVE_DIR, today + "_" + runner_name + Config._csv_ext);
        ArrayList<MyActivity> mal=null;
        if(f.exists()) {
            mal = MyActivityUtil.deserializeFromCSV(today + Config._csv_ext);
        }
        if(mal==null) {
            return;
        }
        MyLoc myloc=new MyLoc(this);
        myloc.deleteAll();
        for(int i=0;i<mal.size();i++) {
            MyActivity a = mal.get(i);
            myloc.ins(a.latitude,a.longitude,a.cr_date, a.cr_time);
        }
    }


    public void rebuildActivitySummaries(Context _ctx) {
        MyActiviySummary.getInstance(_ctx).createNew();
        File[] files = Config.CSV_SAVE_DIR.listFiles();
        for(int i=0;i<files.length;i++) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserialize(files[i]);
            if(mal==null) continue;
            if(mal.size()==0) continue;
            ActivityStat as = MyActivityUtil.getActivityStat(mal);
            if(as==null) continue;
            //String name = as.name;
            String name = files[i].getName();
            //if(name.length() <15 ) continue;

            double distanceKm = as.distanceKm;
            long durationInLong = as.durationInLong;
            double minperKm = as.minperKm;
            int calories = as.calories;

            if(minperKm == 0) files[i].delete();
            else {
                MyActiviySummary.getInstance(_ctx).ins(name, distanceKm, durationInLong, minperKm, calories);
                Log.d("----", " -- new Activity summary: " + as.toString());
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fullscreen_content:
            case R.id.dummy_button:
//                Intent intent = new Intent(this, FileActivity.class);
//                intent.putExtra("pos", 0);
//                intent.putExtra("filetype", Config._file_type_all);


                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pop_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return MyMenu.getInstance(this).onOptionsItemSelected(item);
    }


}