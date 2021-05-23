package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MyReportActivity extends AppCompatActivity {
    String activity_filename=null;
    Context _ctx=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _ctx = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_report);

        Intent intent = getIntent();
        activity_filename = intent.getExtras().getString("activity_file_name");

        Toast.makeText(_ctx, "Activity filename: " + activity_filename, Toast.LENGTH_LONG).show();
    }
}