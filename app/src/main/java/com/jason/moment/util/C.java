package com.jason.moment.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;

public class C {
    static C instance = null;

    public static C getInstance() {
        if (instance == null) instance = new C();
        return instance;
    }

    public static SharedPreferences sPref = null;

    public static String runner = "Jason";
    public static boolean cloud_up = true;
    public static boolean cloud_dn = true;

    public static boolean showallmarkers = false;
    public static boolean nomarkers = true;
    public static boolean notrack = false;
    public static boolean satellite = false;

    public static boolean MapToolbar = true;
    public static boolean ZoomControl = true;
    public static boolean Compass = true;
    public static boolean LocationButton = true;

    // set all google map function
    public void setGoogleMap(Context _ctx, GoogleMap googleMap) {
        if (googleMap == null) return;

        if (ActivityCompat.checkSelfPermission(_ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(_ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

//        googleMap.getUiSettings().setAllGesturesEnabled(true);
//        googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
//        googleMap.getUiSettings().setRotateGesturesEnabled(true);
//        googleMap.getUiSettings().setScrollGesturesEnabled(true);
//        googleMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
//        googleMap.getUiSettings().setTiltGesturesEnabled(true);
    }

    public static String getRunnerName(Context _ctx) {
        if(sPref==null ) sPref = PreferenceManager.getDefaultSharedPreferences(_ctx);
        return sPref.getString("your_name", "Runner");
    }

    static String last_interval=null;
    static String last_distance=null;

    public static void restore_preference_values_after_battery(Activity _activity) {
        restore_preference_values_after_running(_activity);
    }

    public static void restore_preference_values_after_running(Activity _activity) {
        if(last_interval==null || last_distance==null) return;
        SharedPreferences sharedPref = _activity.getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("interval",last_interval);
        editor.putString("distance",last_distance);
        editor.commit();
        last_interval = last_distance = null;
    }

    public static void init_preference_values_running(Activity _activity, String interval, String distance) {
        Log.d("C", "-- Config, init_preference_values_running");

        last_interval = Config.getPreference(_activity.getApplicationContext(),"interval");
        last_distance = Config.getPreference(_activity.getApplicationContext(),"distance");

        SharedPreferences sharedPref = _activity.getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("interval",interval);
        editor.putString("distance",distance);
        editor.commit();

        Config._loc_interval = Integer.parseInt(interval);
        Config._loc_distance = Float.parseFloat(distance);
    }

    public static void init_preference_value_running_default(Activity _activity) {
        init_preference_values_running(_activity, "1000","1"); // 1sec, 100 centimeter
    }

    public static void init_preference_value_battery_default(Activity _activity) {
        init_preference_values_running(_activity, "60000","1"); // 1sec, 100 centimeter
    }


}
