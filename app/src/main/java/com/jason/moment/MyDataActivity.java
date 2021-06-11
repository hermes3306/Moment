package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.jason.moment.util.CloudUtil;

public class MyDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    void handleSendText(Intent intent) {
        Toast.makeText(this, "handleSendText", Toast.LENGTH_SHORT).show();
    }

    void handleSendImage(Intent intent) {
        intent.getData();

        CloudUtil.getInstance().Upload(this,Config);
        Toast.makeText(this, "handleSendImage", Toast.LENGTH_SHORT).show();
    }

    void handleSendMultipleImages(Intent intent) {
        Toast.makeText(this, "handleSendMultipleImages", Toast.LENGTH_SHORT).show();
    }

}