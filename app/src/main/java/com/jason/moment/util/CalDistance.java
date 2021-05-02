package com.jason.moment.util;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class CalDistance {
    public double theta, dist;
    public double bef_lat, bef_long, cur_lat, cur_long;
    public static String TAG = "CalDistance";

    public CalDistance(double bef_lat, double bef_long, double cur_lat, double cur_long) {
        this.theta = 0;
        this.dist = 0;
        this.bef_lat = bef_lat;
        this.bef_long = bef_long;
        this.cur_lat = cur_lat;
        this.cur_long = cur_long;
    }

    public CalDistance(LatLng prevpos, LatLng nextpos) {
    }

    public double getDistance() {
        Log.d(TAG,"-- getDistance.");
        theta = bef_long - cur_long;
        dist = Math.sin(deg2rad(bef_lat)) * Math.sin(deg2rad(cur_lat)) + Math.cos(deg2rad(bef_lat))
                * Math.cos(deg2rad(cur_lat))*Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        dist = dist * 1000.0;  // 단위 Km 에서 m로 변환

        //Log.e(TAG, "Distance:" + dist);
        return dist; // 단위 m
    }
    private double deg2rad(double deg) {
        return (double)(deg*Math.PI/(double)180d);
    }
    private double rad2deg(double rad) {
        return (double)(rad * (double)180d / Math.PI);
    }

    public static double prev_lat=-1;
    public static double prev_lng=-1;

    public static double dist(double lat1, double lng1, double lat2, double lng2) {
        Log.d(TAG,"-- dist.");
        prev_lat = lat2;
        prev_lng = lng2;

        CalDistance cd = new CalDistance(lat1, lng1, lat2, lng2);
        double dist = cd.getDistance();
        return dist;
    }

    public static double dist(double lat, double lng) {
        Log.d(TAG,"-- dist.");

        if(prev_lat==-1) return 0;
        if(prev_lng==-1) return 0;

        CalDistance cd = new CalDistance(prev_lat, prev_lng, lat, lng);
        double dist = cd.getDistance();
        prev_lat = lat;
        prev_lng = lng;
        return dist;
    }

    public String getDistanceKmStr() {
        return String.format("%.1fkm",getDistance() / 1000.0);
    }
    public String getDistanceMStr(){
        return String.format("%.0fm",getDistance());
    }

}