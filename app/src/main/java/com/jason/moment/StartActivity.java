package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.jason.moment.util.CalDistance;
import com.jason.moment.util.Config;
import com.jason.moment.util.StringUtil;
import com.jason.moment.util.db.MyLoc;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity {
    public String TAG = "StartActivity";

    public TextView tv_start_km;
    public TextView tv_start_time;
    public Date start_time;
    public long dist=0;
    public boolean quit=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        start_time = new Date();
        tv_start_km = (TextView)findViewById(R.id.tv_start_km);
        tv_start_time = (TextView)findViewById(R.id.tv_start_time);
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
        public void run() {
            long start = System.currentTimeMillis();
            StartActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Date d = new Date();
                    String elapsed = StringUtil.Duration(start_time,d);
                    tv_start_time.setText(elapsed);
                    dist+=1;
                    tv_start_km.setText("" + dist);

                }
            });
        } /* end of run() */
    } /* end of MyTimerTask */
}