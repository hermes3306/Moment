package com.jason.moment.util.camera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.core.content.FileProvider;

import com.jason.moment.R;
import com.jason.moment.StartActivity;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;

import java.io.File;

public class CameraUtil {
    static String TAG = "CameraUtil";

    public static void showImg(ImageView iv_pic, String fname) {
        File folder= Config.PIC_SAVE_DIR;
        File file = new File(folder,fname);
        String filepath = file.getAbsolutePath();

        Log.d(TAG,"--show:"+filepath);
        Log.d(TAG, "--filepath to show:" + filepath);
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        int mDegree = 90;
        bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(),matrix,true);
        iv_pic.setImageBitmap(bitmap);
    }

    public static void showImg(Context parentContext, String fname) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(parentContext);
        LayoutInflater factory = LayoutInflater.from(parentContext);

        /// View를 inflate하면 해당 View내의 객체를 접근하려면 해당  view.findViewById를 호출 해야 함
        final View view = factory.inflate(R.layout.layout_imageview, null);
        ImageView iv = view.findViewById(R.id.dialog_imageview);
        showImg(iv, fname);
        alertadd.setView(view);
        alertadd.setPositiveButton("Upload!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {
                CloudUtil.getInstance().Upload(fname);
            }
        });
        alertadd.show();
    }

    public static void showVideo(Context parentContext, VideoView vv, String currentFileName) {
        MediaController m;
        m = new MediaController(parentContext);

        File mediaFile = new File(Config.MOV_SAVE_DIR, currentFileName);
        Uri mediaUri = FileProvider.getUriForFile(parentContext,
                "com.jason.moment.file_provider",
                mediaFile);
        vv.setVideoURI(mediaUri);
        vv.start();
    }

    public static void showVideo(Context parentContext, String fname) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(parentContext);
        LayoutInflater factory = LayoutInflater.from(parentContext);

        /// View를 inflate하면 해당 View내의 객체를 접근하려면 해당  view.findViewById를 호출 해야 함
        final View view = factory.inflate(R.layout.layout_videoview, null);
        VideoView vv = view.findViewById(R.id.dialog_video_view);
        showVideo(parentContext, vv, fname);
        alertadd.setView(view);
        alertadd.setPositiveButton("Upload!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {
                CloudUtil.getInstance().Upload(fname);
            }
        });
        alertadd.show();
    }

}
