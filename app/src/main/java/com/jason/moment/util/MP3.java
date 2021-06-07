package com.jason.moment.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.core.content.FileProvider;

import com.jason.moment.R;
import com.jason.moment.StartNewActivity;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MP3 {
    static String TAG = "MP3";
    static File[] playlist1 = null;
    static MediaPlayer mediaPlayer = null;
    static Timer timer = null;
    static int pos=0;
    static boolean played = false;
    static TextView tv = null;
    static Context context;

    public static int current() {
        return pos;
    }

    public static void showPlayer(Context context) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);
        if(!played) playRN(context,pos);

        View view1 = factory.inflate(R.layout.layout_mp3view, null);
        ImageView iv = view1.findViewById(R.id.dialog_imageview);
        tv = view1.findViewById(R.id.view_title);

        playlist1 = list();
        if(playlist1==null) {
            Toast.makeText(context,"No musics!",Toast.LENGTH_SHORT).show();
            return;
        }

        if(playlist1.length==0 ) {
            Toast.makeText(context,"No musics!",Toast.LENGTH_SHORT).show();
            return;
        }
        //tv.setText("" + (pos+1) + "/" + playlist1.length);
        tv.setText(playlist1[pos].getName());
        alertadd.setView(view1);
        if(playlist1.length > pos+1 ) {
            alertadd.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    playRN(context, pos+1);
                    showPlayer(context);
                }
            });
        }

        if(0 < pos) {
            alertadd.setNegativeButton("Prev", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    playRN(context, pos-1);
                    showPlayer(context);
                }
            });
        }

        if(playlist1.length > 0) {
            alertadd.setNeutralButton("Play All", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    playNext(context);
                }
            });
        }
        alertadd.show();
    }

    public static File[] list() {
        if (playlist1 == null) {
            playlist1 = Config.MP3_SAVE_DIR.listFiles();
            Log.d(TAG, "-- # of MP3:" + playlist1.length);
        }
        return playlist1;
    }

    public static void stop(Context context) {
        if(timer!=null) timer.cancel();
        timer=null;
        mediaPlayer.stop();
        mediaPlayer=null;
        played=false;
    }

    public static void playRN(Context context, int new_pos) {
        pos = new_pos;
        if(timer!=null) timer.cancel();
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            return;
        }

        if (playlist1 != null) {
            playlist1 = Config.MP3_SAVE_DIR.listFiles();
            Log.d(TAG, "-- # of MP3:" + playlist1.length);
        } else {
            Toast.makeText(context,"No mp3 files", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer.reset();
        Uri myUri = FileProvider.getUriForFile(context,
                "com.jason.moment.file_provider", playlist1[pos]);
        Log.d(TAG,"-- MP3 players will play:" + playlist1[pos].getName());

        try {
            mediaPlayer.setDataSource(context, myUri);
            mediaPlayer.prepare();
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        mediaPlayer.start();
        played = true;
    }

    public static void play(Context context, int new_pos) {
        pos = new_pos;
        if(timer==null) timer = new Timer();
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        if (playlist1 == null) {
            playlist1 = Config.MP3_SAVE_DIR.listFiles();
            Log.d(TAG, "-- # of MP3:" + playlist1.length);
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mediaPlayer.reset();
                Uri myUri = FileProvider.getUriForFile(context,
                        "com.jason.moment.file_provider", playlist1[pos]);
                Log.d(TAG,"-- MP3 players will play:" + playlist1[pos].getName());

                try {
                    mediaPlayer.setDataSource(context, myUri);
                    mediaPlayer.prepare();
                }catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e(TAG,"Err:" + sw.toString());
                }
                mediaPlayer.start();

                played = true;
                if (playlist1.length > pos+1) {
                    playNext(context);
                }
            }
        },mediaPlayer.getDuration()+100);
    }

    public static void playNext(Context context) {
        play(context, ++pos);
    }

    public static void playPrev(Context context) {
        play(context, --pos);
    }

    // below is for just TEST
    public static void play(Context context, File file) {
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        Uri myUri = FileProvider.getUriForFile(context,
                "com.jason.moment.file_provider", file);
        Log.d(TAG,"-- MP3 players will play:" + file.getName());

        try {
            mediaPlayer.stop();
            mediaPlayer.setDataSource(context, myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
    }

    public static void play(Context context, String filename) {
        File f = new File(Config.MP3_SAVE_DIR, filename);
        play(context, f);
    }

    public static void test2(Context ctx) {
        String url = "http://ezehub.club/moment/mp3/A%20Time%20For%20Us.mp3"; // your URL here
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.start();
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
    }

}
