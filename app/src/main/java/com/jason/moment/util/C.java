package com.jason.moment.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;

public class C {
    public static SharedPreferences sPref = null;

    public static String  runner        = "Jason";
    public static boolean cloud_up      = true;
    public static boolean cloud_dn      = true;

    public static boolean nomarkers     = true;
    public static boolean notrack       = false;
    public static boolean satellite     = false;

    public static boolean MapToolbar  = true;
    public static boolean ZoomControl = true;
    public static boolean Compass = true;
    public static boolean LocationButton = true;

    // set all google map function
    public static void setGoogleMap(GoogleMap googleMap) {
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    public static String getRunnerName(Context _ctx) {
        if(sPref==null ) sPref = PreferenceManager.getDefaultSharedPreferences(_ctx);
        return sPref.getString("your_name", "Runner");
    }


}
