package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.PermissionUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class MyDataActivity extends AppCompatActivity {
    Context _ctx = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PermissionUtil.getInstance().setPermission(this);
        this._ctx = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_data);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            } else if (type.startsWith("video/")) {
                handleSendVideo(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/") || type.startsWith("video/") ) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
            Log.e("MyDataActivity", "-- HERE!!!!!!!!");
            finish();
        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
        }
        Toast.makeText(this, "handleSendText", Toast.LENGTH_SHORT).show();
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            try {
                // Update UI to reflect image being shared
                InputStream is = getContentResolver().openInputStream(imageUri);
                File save_file = new File(Config.PIC_SAVE_DIR, Config.getTmpPicName());
                OutputStream os = new FileOutputStream(save_file);

                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                is.close();

                CloudUtil.getInstance().Upload(save_file.getName());

                Log.d("TAG", "-- Picture uploaded successfully!");
                Toast.makeText(this, "Picture uploaded successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }catch(Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e("MyDataActivity", sw.toString());
            }
        }
    }

    void handleSendVideo(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {

            try {
                // Update UI to reflect image being shared
                InputStream is = getContentResolver().openInputStream(imageUri);
                File save_file = new File(Config.MOV_SAVE_DIR, Config.getTmpVideoName());
                OutputStream os = new FileOutputStream(save_file);

                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                is.close();

                CloudUtil.getInstance().Upload(save_file.getName());
                Log.d("TAG", "-- Picture uploaded successfully!");
                Toast.makeText(this, "Video uploaded successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }catch(Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e("MyDataActivity", sw.toString());
            }
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
            Log.d("", "--" + imageUris.toString() );
        }
        Toast.makeText(this, "handleSendMultipleImages", Toast.LENGTH_SHORT).show();
        finish();
    }

}