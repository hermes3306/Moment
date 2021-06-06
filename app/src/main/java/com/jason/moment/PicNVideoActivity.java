package com.jason.moment;

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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.jason.moment.R;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.NotificationUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PicNVideoActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "PicNVideoActivity";
    String currentFileName;
    ArrayList<File> _files=null;
    int pos=0;
    int size=0;
    int mDegree=0;
    Context _ctx;
    TextView tv;
    TextView tv_activity_name;
    TextView tv_date_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activit_picnvideo);

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
            show();
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
        } catch (FileNotFoundException e) {}
        return null;
    }

    public void showImage(ImageView iv_pic, String fname) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        mDegree = 90;

        File mediaFile = new File(Config.PIC_SAVE_DIR, fname);
        Bitmap bitmap = decodeFile(mediaFile);

        bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(),matrix,true);
        iv_pic.setImageBitmap(bitmap);
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

    public void show() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch(requestCode) {
            case Config.PICK_FROM_VIDEO:
                Log.d(TAG, "-- PICK_FROM_VIDEO: ");
                CloudUtil.getInstance().Upload(_ctx,currentFileName);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imvCamera:
                recordVideo();
                break;
            case R.id.imb_next:
                if (pos < size - 1) pos++;
                else pos = 0;
                show();
                break;
            case R.id.imb_prev:
                if (pos > 0) pos--;
                else pos = size - 1;
                show();
                break;
            case R.id.imRotate:
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