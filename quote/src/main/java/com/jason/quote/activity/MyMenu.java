package com.jason.quote.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import com.jason.quote.R;
import com.jason.quote.activity.run.Run1;
import com.jason.quote.activity.run.Run2;
import com.jason.quote.activity.run.Run3;
import com.jason.quote.util.MP3;

public class MyMenu {
    static Context _ctx;
    static MyMenu instance = null;
    public static MyMenu getInstance(Context ctx) {
        _ctx = ctx;
        if(instance==null) instance = new MyMenu();
        return instance;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingActivity = new Intent(_ctx, SettingsActivity.class);
                _ctx.startActivity(settingActivity);
                return true;

            case R.id.action_file:
                Intent fileActivity = new Intent(_ctx, FileActivity.class);
                _ctx.startActivity(fileActivity);
                return true;

            case R.id.action_media:
                Intent mediaActivity = new Intent(_ctx, MediaActivity.class);
                _ctx.startActivity(mediaActivity);
                return true;


            case R.id.action_run1:
                Intent run1 = new Intent(_ctx, Run1.class);
                _ctx.startActivity(run1);
                return true;

            case R.id.action_run2:
                Intent run2 = new Intent(_ctx, Run2.class);
                _ctx.startActivity(run2);
                return true;

            case R.id.action_run3:
                Intent run3 = new Intent(_ctx, Run3.class);
                _ctx.startActivity(run3);
                return true;

            default:
                return true;
        }
    }
}
