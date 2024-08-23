package com.jason.moment.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.core.content.FileProvider;

import com.jason.moment.util.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MP3Service extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "MP3Service";
    private final IBinder binder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private List<File> playlist;
    private int currentPosition = 0;
    private boolean isPlaying = false;

    public class LocalBinder extends Binder {
        public MP3Service getService() {
            return MP3Service.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        loadPlaylist();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void loadPlaylist() {
        playlist = new ArrayList<>();
        File[] files = Config.MP3_SAVE_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".mp3")) {
                    playlist.add(file);
                }
            }
        }
    }

    public void playNext() {
        if (playlist.isEmpty()) {
            return;
        }

        currentPosition = (currentPosition + 1) % playlist.size();
        playCurrentSong();
    }

    public void playPrevious() {
        if (playlist.isEmpty()) {
            return;
        }

        currentPosition = (currentPosition - 1 + playlist.size()) % playlist.size();
        playCurrentSong();
    }

    public void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        isPlaying = false;
    }

    public boolean hasNext() {
        return currentPosition < playlist.size() - 1;
    }

    public boolean hasPrevious() {
        return currentPosition > 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void pause() {
        if (isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void resume() {
        if (!isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public String getCurrentSongName() {
        if (currentPosition >= 0 && currentPosition < playlist.size()) {
            return playlist.get(currentPosition).getName();
        }
        return "";
    }

    private void playCurrentSong() {
        if (currentPosition < 0 || currentPosition >= playlist.size()) {
            return;
        }

        File currentFile = playlist.get(currentPosition);
        Uri uri = FileProvider.getUriForFile(this, "com.jason.moment.file_provider", currentFile);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
        } catch (Exception e) {
            Log.e(TAG, "Error playing file: " + e.getMessage());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}