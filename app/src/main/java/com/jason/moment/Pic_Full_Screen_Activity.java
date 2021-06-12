package com.jason.moment;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.NotificationUtil;
import com.jason.moment.util.camera.CameraUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Pic_Full_Screen_Activity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "PicActivity";
    ArrayList<File> _files=null;
    int pos=0;
    int size=0;
    int mDegree=0;
    Context _ctx;
    TextView tv;
    TextView tv_activity_name;
    TextView tv_date_str;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mControlsView2 = findViewById(R.id.fullscreen_content_controls2);
        mControlsView3 = findViewById(R.id.fullscreen_content_controls3);
        mContentView = findViewById(R.id.iv_pic);
        tv = findViewById(R.id.tv_picinfo);
        tv_activity_name = (TextView)findViewById(R.id.name);
        tv_date_str = (TextView)findViewById(R.id.date_str);

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
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

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



    public void reload() {
        File folder= Config.PIC_SAVE_DIR;
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("jpeg");
            }
        });

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        //Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        ArrayList<File> fileArrayList= new ArrayList<File>();
        for(int i=0;i< files.length;i++) {
            fileArrayList.add(files[i]);
        }
        _files = fileArrayList;
        if(_files.size() > 0) {
            size = _files.size();
            if(pos>size-1) pos=size-1;
            show1();
        }
    }

    // From Andriod Tutorial
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // From Andriod Tutorial
    public static Bitmap decodeSampledBitmapFromResource(String file_path,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file_path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.d(TAG,"--options.inSampleSize=" +options.inSampleSize);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file_path, options);
    }

    // From Android Tutorial
    // Decodes image and scales it to reduce memory consumption
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

    public void show1() {
        if(_files==null) return;
        if(_files.size()==0) return;

        String file_path = _files.get(pos).getAbsolutePath();
        currentFileName = _files.get(pos).getName();

        Log.d(TAG, "-- pos=" + pos + " size=" + size + " file =" +  file_path);
        ImageView iv_pic = (ImageView) findViewById(R.id.iv_pic);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        mDegree = 90;

        File _file = new File(file_path);
        Bitmap bitmap = decodeFile(_file);

        bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(),matrix,true);
        iv_pic.setImageBitmap(bitmap);

        tv.setText("" + (pos+1) + "/" + size);
        /* 함수로 정리해야 함 */
        Date d = new Date(_file.lastModified());
        String name = DateUtil.getActivityName(d);
        String date_str = DateUtil.getDateString(d);
//        tv_activity_name.setText(name);
//        tv_date_str.setText(date_str);
    }

    public void show1_old() {
        String file_path = _files.get(pos).getAbsolutePath();
        currentFileName = _files.get(pos).getName();

        Log.d(TAG, "-- pos=" + pos + " size=" + size + " file =" +  file_path);
        ImageView iv_pic = (ImageView) findViewById(R.id.iv_pic);

        // BitmapFactory.Options로 이미지 사이즈 확인
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        /* 이미지 정보를 얻는 함수 */
        BitmapFactory.decodeFile(file_path, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
        Log.d(TAG, "-- imageHeight:" + imageHeight);
        Log.d(TAG, "-- imageWidth:" + imageWidth);
        Log.d(TAG, "-- imageType:" + imageType);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        mDegree = 90;

        // From Android Tutorial
        decodeSampledBitmapFromResource(file_path, 100, 100);

        Bitmap bitmap = BitmapFactory.decodeFile(file_path);
        bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(),matrix,true);
        iv_pic.setImageBitmap(bitmap);

        tv.setText("" + (pos+1) + "/" + size);
    }

    public void rotate1() {
        String filepath = _files.get(pos).getAbsolutePath();
        Log.d(TAG, "-- pos=" + pos + " size=" + size + " file =" +  filepath);
        ImageView iv_pic = (ImageView) findViewById(R.id.iv_pic);
        iv_pic.setImageBitmap(BitmapFactory.decodeFile(filepath));
        mDegree = mDegree+90;
        Matrix matrix = new Matrix();
        matrix.postRotate(mDegree);
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);
        bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(),matrix,true);
        iv_pic.setImageBitmap(bitmap);
        tv.setText("" + (pos+1) + "/" + size);
    }

    public void deletePic() {
        if(_files.size()==0) {
            Toast.makeText(_ctx,"No files to be deleted!",0).show();
            return;
        }
        if(size==1) {
            Toast.makeText(_ctx,"At least 1 file needed to be located in the folder!",0).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(_ctx);
        builder.setTitle("파일을 삭제하시겠습니까?");
        builder.setMessage("파일을 삭제하시겠습니까?");
        builder.setPositiveButton("삭제",
                new DialogInterface.OnClickListener() {
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


    // 사진 촬영 기능
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentFileName;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                Log.e(TAG,"-- before createImageFile");
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "" + timeStamp + ".jpeg";
                photoFile = new File(Config.PIC_SAVE_DIR, imageFileName);
                Toast.makeText(_ctx, "photoFile " + photoFile.getAbsolutePath() + " is used for this picture!", Toast.LENGTH_LONG).show();
                Log.d(TAG,"-- >>>>after createImageFile" + photoFile.getAbsolutePath());
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG,"Err:" + sw.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.jason.moment.file_provider",
                        photoFile);

                Log.d(TAG, "-- >>>> photoURI is " + photoURI.getPath());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    Log.d(TAG, "-- >>>> resolveActivity called!");
                    startActivityForResult(takePictureIntent, Config.PICK_FROM_CAMERA);
                }

                currentFileName = photoFile.getName();
                Log.d(TAG, "-- >>>> currentPhotoPath is " + photoFile.getAbsolutePath());
                Log.d(TAG, "-- >>>> photoURI is " + photoURI.getPath());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch(requestCode) {
            case Config.PICK_FROM_CAMERA:
                Log.d(TAG, "-- PIC_FROM_CAMERA: ");

                CloudUtil.getInstance().Upload(currentFileName);
                CameraUtil.showImg(_ctx, currentFileName);

                NotificationUtil.notify_new_picture(_ctx, currentFileName);
                break;
            case Config.PICK_FROM_VIDEO:
                Log.d(TAG, "-- PICK_FROM_VIDEO: ");
                CameraUtil.showVideo(_ctx, currentFileName);
                break;
        }
    }

    private void sharePic() {
        File photoFile = new File(Config.PIC_SAVE_DIR, currentFileName);
        Uri photoURI = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                photoFile);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello!");
        // (Optional) Here we're setting the title of the content
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Send message");
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("image/jpg");
        sendIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        startActivity(Intent.createChooser(sendIntent, null));
    }

    // check how to use this galleryAddPic
    private void galleryAddPic(String filename) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(Config.PIC_SAVE_DIR , filename);
        Uri contentUri = Uri.fromFile(f);
        Log.d(TAG,"-- >>>>contentUri to be added to Gallary " + contentUri);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void upload() {
        CloudUtil cu = new CloudUtil();
        cu.UploadAll(_ctx, Config._img);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imvCamera:
                dispatchTakePictureIntent();
                break;
            case R.id.imb_next:
                if (pos < size - 1) pos++;
                else pos = 0;
                show1();
                break;
            case R.id.imb_prev:
                if (pos > 0) pos--;
                else pos = size - 1;
                show1();
                break;
            case R.id.imRotate:
                rotate1();
                break;
            case R.id.imTrash:
                deletePic();
                break;
            case R.id.imShare:
                sharePic();
                break;
            case R.id.uploadall:
                upload();
                break;
            case R.id.imDown:
                new CloudUtil().DownloadAll(_ctx, Config._img);
                NotificationUtil.notify_new_picture(_ctx, "서버로 부터 사진을 다운로드 하였습니다.");
                break;
        }
    }
}
