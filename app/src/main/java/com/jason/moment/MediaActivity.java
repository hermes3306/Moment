package com.jason.moment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.jason.moment.R;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.MediaUtil;
import com.jason.moment.util.NotificationUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MediaActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MediaActivity";
    String currentFileName;
    ArrayList<File> _files=null;
    int pos=0;
    int size=0;
    int mDegree=0;
    Context _ctx;
    TextView tv;
    TextView tv_activity_name;
    TextView tv_date_str;

    /* begin of full screen configuration */
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
    private View mControlsView2;
    private View mControlsView3;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
            mControlsView2.setVisibility(View.VISIBLE);
            mControlsView3.setVisibility(View.VISIBLE);
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
    /* end of full screen configuration */

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        /* begin of features and functions for full screen */
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mControlsView2 = findViewById(R.id.fullscreen_content_controls2);
        mControlsView3 = findViewById(R.id.fullscreen_content_controls3);
        mContentView = findViewById(R.id.media_linearLayer);
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        /* end of features and functions for full screen */

        tv = findViewById(R.id.tv_picinfo);
        tv_activity_name = (TextView)findViewById(R.id.name);
        tv_date_str = (TextView)findViewById(R.id.date_str);

        _ctx = this;
        pos=0;
        reload();
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
        mControlsView2.setVisibility(View.GONE);
        mControlsView3.setVisibility(View.GONE);

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void reload() {
        File folder= Config.MOV_SAVE_DIR;
        File[] files1 = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(Config._mov_ext);
            }
        });

        folder= Config.PIC_SAVE_DIR;
        File[] files2 = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(Config._pic_ext);
            }
        });

        File[] files = null;
        if(files1==null && files2 != null) files = files2;
        else if(files1!=null && files2 == null) files = files1;
        else if(files1==null && files2==null) files=null;
        else files = new File[files1.length + files2.length];

        int j=0;
        if(files1 != null) for(int i=0;i<files1.length;i++) files[j++] = files1[i];
        if(files2 != null) for(int i=0;i<files2.length;i++) files[j++] = files2[i];

        //이름순으로 정렬
        //Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));
        Arrays.sort(files, (a, b) -> -a.getName().compareTo(b.getName()));

        //마지막 수정 날짜로 Sort
        //Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        //Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        ArrayList<File> fileArrayList= new ArrayList<File>();
        for(int i=0;i< files.length;i++) {
            fileArrayList.add(files[i]);
        }
        _files = fileArrayList;
        if(_files.size() > 0) {
            size = _files.size();
            if(pos>size-1) pos=size-1;
            show_media();
        }
    }

    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            // The new size we want to scale to
            int REQUIRED_SIZE= Config.PIC_REQUIRED_SIZE;
            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        return null;
    }

    public void showImage(ImageView iv_pic, String fname) {
        MediaUtil.getInstance().showImage(iv_pic, fname);
    }

    public void showImageBug(ImageView iv_pic, String fname) {
        File f = new File(Config.PIC_SAVE_DIR, fname);
        setPic(iv_pic,f);
    }

    public void showVideo(VideoView vv, String fname) {
        //Video Loop
        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                vv.start(); //need to make transition seamless.
            }
        });

        MediaController m;
        m = new MediaController(this);
        File mediaFile = new File(Config.MOV_SAVE_DIR, fname);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);
        vv.setVideoURI(mediaUri);
        vv.start();
    }

    public void show_media() {
        if(_files==null) return;
        if(_files.size()==0) return;

        String file_path = _files.get(pos).getAbsolutePath();
        currentFileName = _files.get(pos).getName();

        tv.setText("" + (pos+1) + "/" + size);

        Log.d(TAG, "-- pos=" + pos + " size=" + size + " file =" +  file_path);

        if(currentFileName.endsWith(Config._mov_ext)) {
            VideoView vv_view = (VideoView) findViewById(R.id.vv_view);
            ImageView iv_pic = (ImageView) findViewById(R.id.iv_pic);
            vv_view.setVisibility(View.VISIBLE);
            iv_pic.setVisibility(View.GONE);
            showVideo(vv_view, currentFileName);
        } else if(currentFileName.endsWith(Config._pic_ext)) {
            VideoView vv_view = (VideoView) findViewById(R.id.vv_view);
            ImageView iv_pic = (ImageView) findViewById(R.id.iv_pic);
            vv_view.setVisibility(View.GONE);
            iv_pic.setVisibility(View.VISIBLE);
            showImage(iv_pic, currentFileName);
        }
    }

    public void deleteMov() {
        if(_files.size()==0) {
            Toast.makeText(_ctx,"No files to be deleted!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(size==1) {
            Toast.makeText(_ctx,"At least 1 file needed to be located in the folder!",Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(_ctx);
        builder.setTitle("파일을 삭제하시겠습니까?");
        builder.setMessage("파일을 삭제하시겠습니까?");
        builder.setPositiveButton("삭제",
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onClick(DialogInterface dialog, int which) {
                        _files.get(pos).delete();
                        reload();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch(requestCode) {
            case Config.PICK_FROM_CAMERA2:
                Log.d(TAG, "-- REQUEST_IMAGE_CAPTURE: ");
                VideoView vv_view = (VideoView) findViewById(R.id.vv_view);
                ImageView iv_pic = (ImageView) findViewById(R.id.iv_pic);
                vv_view.setVisibility(View.GONE);
                iv_pic.setVisibility(View.VISIBLE);
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                iv_pic.setImageBitmap(imageBitmap);
                MediaUtil.getInstance().onActivityResult(this,requestCode, data, iv_pic);
                break;
            case Config.PICK_FROM_VIDEO:
                Log.d(TAG, "-- PICK_FROM_VIDEO: ");
                CloudUtil.getInstance().Upload(currentFileName);
                break;
            case Config.PICK_FROM_CAMERA:
                Log.d(TAG, "-- PICK_FROM_CAMERA: ");
                CloudUtil.getInstance().Upload(currentFileName);
                break;
            case Config.CALL_RESULT_LOAD_IMAGE:
                Log.d(TAG, "-- CALL_RESULT_LOAD_IMAGE: ");
                vv_view = (VideoView) findViewById(R.id.vv_view);
                iv_pic = (ImageView) findViewById(R.id.iv_pic);
                vv_view.setVisibility(View.GONE);
                iv_pic.setVisibility(View.VISIBLE);
                MediaUtil.getInstance().onActivityResult(this,requestCode, data, iv_pic);
                break;
        }
    }

    private void shareMov() {
        File movieFile = new File(Config.MOV_SAVE_DIR, currentFileName);
        Uri movieURI = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                movieFile);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello!");
        // (Optional) Here we're setting the title of the content
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Send message");
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("video/mp4");
        sendIntent.putExtra(Intent.EXTRA_STREAM, movieURI);
        startActivity(Intent.createChooser(sendIntent, null));
    }

    private void upload() {
        CloudUtil cu = new CloudUtil();
        cu.UploadAll(_ctx, Config._mov);
    }

    private void recordVideo() {
        currentFileName = Config.getTmpVideoName();
        File mediaFile = new File(Config.MOV_SAVE_DIR, currentFileName);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        startActivityForResult(intent, Config.PICK_FROM_VIDEO);
    }

    private void takePic() {
        currentFileName = Config.getTmpPicName();
        File mediaFile = new File(Config.PIC_SAVE_DIR, currentFileName);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        startActivityForResult(intent, Config.PICK_FROM_CAMERA);
    }

    private void takePic2() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, Config.PICK_FROM_CAMERA2);
        }
    }

    private void setPic(ImageView imageView, File f ) {
        setPic(imageView, f.getAbsolutePath());
    }

    private void setPic(ImageView imageView, String absolutePath ) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 1;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(absolutePath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_camera2:
                takePic2();
                break;
            case R.id.imRotate:
                ImageView iv_pic = (ImageView) findViewById(R.id.iv_pic);
                MediaUtil.getInstance().showImageRotated(iv_pic,_files.get(pos).getName());
                break;
            case R.id.save_gallery:
                MediaUtil.getInstance().saveImageInGallery(this, _files.get(pos).getName());
                MediaUtil.getInstance().updateGallery(_ctx, _files.get(pos).getName());
                break;
            case R.id.pickgallery:
                MediaUtil.getInstance().getImageFromAlbum(this);
                break;
            case R.id.imvCamera:
                takePic();
                break;
            case R.id.imvVideo:
                recordVideo();
                break;
            case R.id.imbt_next:
                if (pos < size - 1) pos++;
                else pos = 0;
                show_media();
                break;
            case R.id.imbt_prev:
                if (pos > 0) pos--;
                else pos = size - 1;
                show_media();
                break;
            case R.id.imTrash:
                deleteMov();
                break;
            case R.id.imShare:
                shareMov();
                break;
            case R.id.uploadall:
                upload();
                break;
            case R.id.imDown:
                new CloudUtil().DownloadAll(_ctx, Config._mov);
                NotificationUtil.notify_new_picture(_ctx, "서버로 부터 동영상을 다운로드 하였습니다.");
                break;
        }
    }
}
