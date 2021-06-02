package com.jason.moment.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MP3 {
    static String TAG = "MP3";
    static File[] playlist1 = null;
    static MediaPlayer mediaPlayer = null;
    static Timer timer = null;
    static int pos=0;

    public static int current() {
        return pos;
    }

    public static File[] list() {
        if (playlist1 == null) {
            playlist1 = Config.MP3_SAVE_DIR.listFiles();
            Log.d(TAG, "-- # of MP3:" + playlist1.length);
        }
        return playlist1;
    }

    public static void play_all(Context context) {
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        if (playlist1 == null) {
            playlist1 = Config.MP3_SAVE_DIR.listFiles();
            Log.d(TAG, "-- # of MP3:" + playlist1.length);
        }

        Uri myUri = FileProvider.getUriForFile(context,
                "com.jason.moment.file_provider", playlist1[pos]);
        Log.d(TAG,"-- MP3 players will play:" + playlist1[pos].getName());

        try {
            mediaPlayer.setDataSource(context, myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            timer = new Timer();
            if(pos<playlist1.length) playNext(context);
        }catch(Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }

    public static void play(Context context, int new_pos) {
        pos = new_pos;
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
                    e.printStackTrace();
                    Log.d(TAG, e.toString());
                }
                mediaPlayer.start();
                if (playlist1.length > pos+1) {
                    playNext(context);
                }
            }
        },mediaPlayer.getDuration()+100);
    }

    public static void playNext(Context context) {
        play(context, ++pos);
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
            mediaPlayer.setDataSource(context, myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
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
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }

}
