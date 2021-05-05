package com.jason.moment.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.jason.moment.MapsActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class CameraUtil {
    // 카메라에서 이미지 가져오기
    private static final int PICK_FROM_CAMERA = 2;
    private Context _ctx;
    private Activity _activity;
    public File tempFile=null;
    public String TAG = "PhotoUtil";

    public CameraUtil(Context ctx, Activity activity){
        this._ctx = ctx;
        this._activity = activity;
    }

    public void takePhoto()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        }   catch (Exception e) {
            Toast.makeText(_ctx, "-- 이미지 처리 오류, 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
            //finish();
            e.printStackTrace();
            return;
        }

        if (tempFile != null)
        {
            Uri photoUri = FileProvider.getUriForFile(_ctx, "com.jason.moment.fileprovider", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            _ctx.startActivity(intent);
            startActivityForResult(_activity, intent,2, null);
        }
    }

    // 폴더 및 파일 만들기
    private File createImageFile() throws IOException
    {
        // 이미지 파일명
        String timeStamp = new SimpleDateFormat("HHmmss", Locale.KOREA).format(new Date());
        String imageFileName = "imgFile_" + timeStamp + "_";

        // 이미지가 저장될 폴더명
        //File storageDir = new File(Environment.getExternalStorageDirectory() + "/");
        File storageDir = _ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists())
        {
            storageDir.mkdirs();
        }
        // 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.e(TAG, "-- createImageFile : " + image.getAbsolutePath());
        return image;
    }


}
