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

import androidx.core.content.FileProvider;

import com.jason.moment.R;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MP3 {
    private static final String TAG = "MP3";
    private static File[] playlist = null;
    private static MediaPlayer mediaPlayer = null;
    private static Timer timer = null;
    private static int currentPosition = 0;
    private static boolean isPlaying = false;
    private static TextView titleTextView = null;
    private static ArrayList<Integer> shuffledIndices = new ArrayList<>();
    private static int shuffleIndex = 0;
    private static boolean isShuffleMode = false;

    public static void showPlayer(Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_mp3view, null);
        ImageView imageView = view.findViewById(R.id.dialog_imageview);
        titleTextView = view.findViewById(R.id.view_title);

        playlist = list();
        if (playlist == null || playlist.length == 0) {
            Toast.makeText(context, "No music files available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPlaying) {
            playCurrentSong(context);
        }

        updateTitleDisplay();
        alertDialog.setView(view);

        if (playlist.length > currentPosition + 1) {
            alertDialog.setPositiveButton("Next", (dialog, which) -> {
                playNext(context);
                showPlayer(context);
            });
        }

        if (currentPosition > 0) {
            alertDialog.setNegativeButton("Prev", (dialog, which) -> {
                playPrevious(context);
                showPlayer(context);
            });
        }

        alertDialog.setNeutralButton("Play All", (dialog, which) -> {
            isShuffleMode = false;
            playNext(context);
        });

        alertDialog.setNeutralButton("Shuffle", (dialog, which) -> {
            isShuffleMode = true;
            shuffleAndPlay(context);
        });

        alertDialog.show();
    }

    private static File[] list() {
        if (playlist == null) {
            playlist = Config.MP3_SAVE_DIR.listFiles();
            Log.d(TAG, "Number of MP3 files: " + (playlist != null ? playlist.length : 0));
        }
        return playlist;
    }


    public static void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
    }

    public static void playCurrentSong(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } else {
            mediaPlayer.reset();
        }

        Uri songUri = FileProvider.getUriForFile(context,
                "com.jason.moment.file_provider", playlist[currentPosition]);
        Log.d(TAG, "Playing: " + playlist[currentPosition].getName());

        try {
            mediaPlayer.setDataSource(context, songUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
        } catch (Exception e) {
            logError(e);
        }
    }

    public static void playNext(Context context) {
        currentPosition = (currentPosition + 1) % playlist.length;
        playAndScheduleNext(context);
    }

    private static void playPrevious(Context context) {
        currentPosition = (currentPosition - 1 + playlist.length) % playlist.length;
        playAndScheduleNext(context);
    }

    public static void shuffleAndPlay(Context context) {
        if (shuffledIndices.isEmpty() || shuffleIndex >= shuffledIndices.size()) {
            shuffledIndices.clear();
            for (int i = 0; i < playlist.length; i++) {
                shuffledIndices.add(i);
            }
            Collections.shuffle(shuffledIndices);
            shuffleIndex = 0;
        }

        currentPosition = shuffledIndices.get(shuffleIndex);
        shuffleIndex++;
        playAndScheduleNext(context);
    }

    private static void playAndScheduleNext(Context context) {
        playCurrentSong(context);
        updateTitleDisplay();

        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isShuffleMode) {
                    shuffleAndPlay(context);
                } else {
                    playNext(context);
                }
            }
        }, mediaPlayer.getDuration() + 100);
    }

    private static void updateTitleDisplay() {
        if (titleTextView != null) {
            titleTextView.setText(playlist[currentPosition].getName());
        }
    }

    private static void logError(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        Log.e(TAG, "Error: " + sw.toString());
    }
}