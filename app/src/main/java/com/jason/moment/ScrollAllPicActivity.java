package com.jason.moment;

import android.annotation.SuppressLint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.jason.moment.util.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static android.view.View.GONE;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ScrollAllPicActivity extends AppCompatActivity implements View.OnClickListener{
    public String TAG = "ScrollAllPic";
    ArrayList<File> _files=null;
    int pos=0;
    int size=0;
    int mDegree=0;
    Context _ctx;
    TextView tv;
    ScrollView hsv;
    LinearLayout gallery;


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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scroll_all_pic);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mControlsView2 = findViewById(R.id.fullscreen_content_controls2);
        mControlsView3 = findViewById(R.id.fullscreen_content_controls3);
        mContentView = findViewById(R.id.gallery);
        hsv = findViewById(R.id.hsv);
        tv = findViewById(R.id.tv_picinfo);
        gallery = findViewById(R.id.gallery);

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

        hsv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollX = hsv.getScrollX(); //for horizontalScrollView
                int scrollY = hsv.getScrollY(); //for verticalScrollView
                int scrollWidth = hsv.getWidth();
                int scrollHeight = hsv.getHeight();


                //ImageView iv = (ImageView)hsv.getFocusedChild();
                //tv.setText(""+hsv.getScrollY() + "ImageView" + iv.getId());
                //tv.setText("" + hsv.getScrollY());

                //DO SOMETHING WITH THE SCROLL COORDINATES
                Log.d(TAG,"-- scrollX:" + scrollX + ", scrollY:"+scrollY  );
                Log.d(TAG,"-- scroll width:" + scrollWidth + ", scrollHeight:"+scrollHeight  );
                tv.setText("");
                pos = -1;
            }
        });
        _ctx = this;
        build_all_Image_Views();
    }

    int[] file_height = null;
    private void build_all_Image_Views() {
        _files = new ArrayList<>();
        File folder= Config.PIC_SAVE_DIR;
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("jpeg");
            }
        });
        Log.d(TAG,"-- files.length:" + files.length);

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

        for(int i=0;i<files.length;i++) {
            _files.add(files[i]);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ImageView imageView = (ImageView)inflater.inflate(R.layout.layout_imageitem, null);
            imageView.setId(i);
            Log.d(TAG,"-- ImageView id:" + i);

            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    pos = v.getId();
                    String str = "" + (pos+1) + "/" + number_of_members_in_scroll_view;
                    tv.setText(str);
                    //Snackbar.make(v,"ImageView" + v.getId(), Snackbar.LENGTH_SHORT).show();
                    return false;
                }
            });

            /* Set Bitmap for the image view */
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            mDegree = 90;
            Bitmap bitmap = decodeFile(new File(files[i].getAbsolutePath()));
            bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(),matrix,true);
            imageView.setImageBitmap(bitmap);
            /* add to LinearLayer, gallery  */
            gallery.addView(imageView);
            number_of_members_in_scroll_view = files.length;
            
            //file_height[i] = bitmap.getHeight(); // numbers
            Log.d(TAG, "-- hsv.getTop()" + hsv.getTop());
            Log.d(TAG, "-- hsv.getBottom()" + hsv.getBottom());
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
        mControlsView.setVisibility(GONE);
        mControlsView2.setVisibility(GONE);
        //mControlsView3.setVisibility(View.GONE);

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

    public void rotate1() {
    }

    int delete_img_num = -1;
    int number_of_members_in_scroll_view = 0;
    
    public void deletePic() {
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
                    public void onClick(DialogInterface dialog, int which) {
                        _files.get(pos).delete();
                        delete_img_num = pos;
                        number_of_members_in_scroll_view --;
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        delete_img_num = -1;
                    }
                });
        builder.show();
    }

    String currentFileName;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "-- onActivityResult called!");
        if (requestCode == Config.PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            Log.d(TAG, "-- resultCode - PICK_FROM_CAMERA");
            if (data == null) {
                Log.d(TAG, "-- Intent data is NULL!!!!");
            } else {
                Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            imageView.setImageBitmap(imageBitmap);
            }
        }
    }

    private void sharePic() {
        File photoFile = _files.get(pos);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imvCamera:
                takePic();
                break;
            case R.id.imbt_next:
                hsv.post(new Runnable() {
                    public void run() {
                        hsv.fullScroll(View.FOCUS_DOWN);
                    }
                });
                break;
            case R.id.imbt_prev:
                hsv.post(new Runnable() {
                    public void run() {
                        hsv.fullScroll(View.FOCUS_UP);
                    }
                });
                break;
            case R.id.imRotate:
                rotate1();
                break;
            case R.id.imTrash:
                if(pos==-1) {
                    Snackbar.make(v,"이미지를 선택후 삭제 하세요.", Snackbar.LENGTH_SHORT).show();
                    break;
                }
                deletePic();
                if(delete_img_num!= -1) {
                    ImageView iv = findViewById(delete_img_num);
                    iv.setVisibility(View.GONE);
                    gallery.removeView(iv);
//                  gallery.removeViewAt(delete_img_num);
                    String str = "" + (pos+1) + "/" + number_of_members_in_scroll_view;
                    tv.setText(str);
                    delete_img_num=-1;
                }
                break;
            case R.id.imShare:
                if(pos==-1) {
                    Snackbar.make(v,"이미지를 선택후 공유 하세요.", Snackbar.LENGTH_SHORT).show();
                    break;
                }
                sharePic();
                break;
        }
    }
}
