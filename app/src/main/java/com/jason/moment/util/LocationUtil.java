package com.jason.moment.util;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.jason.moment.util.db.MyLoc;

public class LocationUtil {
    private final String TAG="LocationUtil";
    private static boolean first_called=true;
    private Location last_location=null;

    private static LocationUtil locationUtil=null;
    public static LocationUtil getInstance() {
        if(locationUtil==null) locationUtil= new LocationUtil();
        return locationUtil;
    }

    public Location getLast_location() {
        return last_location;
    }

    public void onLocationChanged(Context context, Location location) {
        double dist;
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        if(last_location==null) {
            dist = 0;
        }else {
            dist = CalDistance.dist(last_location.getLatitude(), last_location.getLongitude(), location.getLatitude(), location.getLongitude());
        }
        last_location = location;
        Log.d(TAG,"-- onLocationChanged("+location.getLatitude()+","+location.getLongitude()+")");
        if(!first_called && dist < Config._loc_distance) return;
        Log.d(TAG,"-- onLocationChanged("+dist+"m)");
        MyLoc myloc = new MyLoc(context);
        if(first_called) {
            first_called = false;
            MyActivity ma = myloc.lastActivity();
            if(ma == null) {
                myloc.ins(location.getLatitude(), location.getLongitude());
                return;
            }
            double d2 = CalDistance.dist(ma.latitude, ma.longitude, location.getLatitude(), location.getLongitude());
            if (d2 > Config._loc_distance) myloc.ins(location.getLatitude(), location.getLongitude());
        }
        else {
            if (dist > Config._loc_distance) {
                myloc.ins(location.getLatitude(), location.getLongitude());
            } else return;
        }
    }
}
