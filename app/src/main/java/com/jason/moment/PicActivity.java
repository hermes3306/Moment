package com.jason.moment;

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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jason.moment.util.Config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class PicActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "PicActivity";
    ArrayList<File> _files=null;
    int pos=0;
    int size=0;
    int mDegree=0;
    Context _ctx;
    TextView tv;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);
        tv = (TextView)findViewById(R.id.tv_picinfo);


        _ctx = this;
        Intent intent = getIntent();
        _files = (ArrayList<File>)getIntent().getSerializableExtra("files");

        if(_files==null) {
            Toast.makeText(this,"No pics!",Toast.LENGTH_SHORT).show();
            finish();
        }
        if(_files.size() > 0) {
            size = _files.size();
            show1();
        }
        else {
            Log.e(TAG, "No files to show pictures!");
        }


    }

    public void reload() {
        File folder= _ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File files[] = folder.listFiles();

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

    public void show1() {
        String filepath = _files.get(pos).getAbsolutePath();
        currentFileName = _files.get(pos).getName();

        Log.d(TAG, "-- pos=" + pos + " size=" + size + " file =" +  filepath);
        ImageView iv_pic = (ImageView) findViewById(R.id.iv_pic);
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        mDegree = 90;
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
        tv.setText("" + pos + "/" + size);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imvCamera:
                dispatchTakePictureIntent();
                break;
            case R.id.imb_next:
                if(pos<size-1) pos++; else pos=0;
                show1();
                break;
            case R.id.imb_prev:
                if(pos>0) pos--; else pos=size-1;
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
        }
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
                String imageFileName = "IMG_" + timeStamp + ".jpeg";
                photoFile = new File(_ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName);
                Toast.makeText(_ctx, "photoFile " + photoFile.getAbsolutePath() + " is used for this picture!", Toast.LENGTH_LONG).show();
                Log.d(TAG,"-- >>>>after createImageFile" + photoFile.getAbsolutePath());
            } catch (Exception ex) {
                // Error occurred while creating the File
                Log.d(TAG,"-- >>>>" +ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.jason.moment.fileprovider",
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
            reload();
            galleryAddPic(currentFileName); }
    }

    private void sharePic() {
        File photoFile = new File(_ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), currentFileName);
        Uri photoURI = FileProvider.getUriForFile(this,
                "com.jason.moment.fileprovider",
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
        File f = new File(_ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
        Uri contentUri = Uri.fromFile(f);
        Log.d(TAG,"-- >>>>contentUri to be added to Gallary " + contentUri);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}