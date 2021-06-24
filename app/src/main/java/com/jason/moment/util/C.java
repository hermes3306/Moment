package com.jason.moment.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

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

//        googleMap.getUiSettings().setAllGesturesEnabled(true);
//        googleMap.getUiSettings().setCompassEnabled(true);
//        googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
//        googleMap.getUiSettings().setMapToolbarEnabled(true);
//        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//        googleMap.getUiSettings().setRotateGesturesEnabled(true);
//        googleMap.getUiSettings().setScrollGesturesEnabled(true);
//        googleMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
//        googleMap.getUiSettings().setTiltGesturesEnabled(true);
//        googleMap.getUiSettings().setZoomControlsEnabled(true);
//        googleMap.getUiSettings().setZoomGesturesEnabled(true);
//        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    public static String getRunnerName(Context _ctx) {
        if(sPref==null ) sPref = PreferenceManager.getDefaultSharedPreferences(_ctx);
        return sPref.getString("your_name", "Runner");
    }


}
