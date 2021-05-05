package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import static com.jason.moment.FileActivity.fname;

public class PicActivity extends AppCompatActivity {
    private String TAG = "PicActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);

        Intent intent = getIntent();
        ArrayList<File> files = (ArrayList<File>)getIntent().getSerializableExtra("files");

        if(files.size() > 0) {
            show1(files.get(0).getAbsolutePath());
        }
        else {
            Log.e(TAG, "No files to show pictures!");
        }
    }

    public void show1(String filepath) {
        ImageView iv_pic = (ImageView) findViewById(R.id.iv_pic);
        iv_pic.setImageBitmap(BitmapFactory.decodeFile(filepath));
    }


}