package com.jason.moment.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class CalDistance {
    public double theta, dist;
    public double bef_lat, bef_long, cur_lat, cur_long;
    public static String TAG = "CalDistance";

    public static CalDistance instance = null;
    public static CalDistance getInstance() {
        if(instance==null) instance =new CalDistance();
        return instance;
    }

    public CalDistance() {}

    public CalDistance(double bef_lat, double bef_long, double cur_lat, double cur_long) {
        this.theta = 0;
        this.dist = 0;
        this.bef_lat = bef_lat;
        this.bef_long = bef_long;
        this.cur_lat = cur_lat;
        this.cur_long = cur_long;
    }

    public CalDistance(MyActivity a, MyActivity b) {
        this.theta = 0;
        this.dist = 0;
        this.bef_lat = a.latitude;
        this.bef_long = a.longitude;
        this.cur_lat = b.latitude;
        this.cur_long = b.longitude;
    }

    public CalDistance(LatLng prevpos, LatLng nextpos) {
        this.theta = 0;
        this.dist = 0;
        this.bef_lat = prevpos.latitude;
        this.bef_long = prevpos.longitude;
        this.cur_lat = nextpos.latitude;
        this.cur_long = nextpos.longitude;
    }

    public double getDistance_old() {
        theta = bef_long - cur_long;
        dist = Math.sin(deg2rad(bef_lat)) * Math.sin(deg2rad(cur_lat)) + Math.cos(deg2rad(bef_lat))
                * Math.cos(deg2rad(cur_lat))*Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        dist = dist * 1000.0;  // 단위 Km 에서 m로 변환

        //Log.d(TAG, "-- getDistance:" + dist + " m");
        return dist; // 단위 m
    }

    public double getDistance() {
        return calculateDistance(bef_lat,bef_long,cur_lat,cur_long);
    }

    public double calculateDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaLatitude = Math.toRadians(Math.abs(latitude1 - latitude2));
        double deltaLongitude = Math.toRadians(Math.abs(longitude1 - longitude2));
        double latitude1Rad = Math.toRadians(latitude1);
        double latitude2Rad = Math.toRadians(latitude2);
        double a = Math.pow(Math.sin(deltaLatitude / 2), 2) +
                (Math.cos(latitude1Rad) * Math.cos(latitude2Rad) * Math.pow(Math.sin(deltaLongitude / 2), 2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c * 1000; //Distance in meters
    }

    public double calculateDistance(Location l1, Location l2) {
        double latitude1    = l1.getLatitude();
        double longitude1   = l1.getLongitude();
        double latitude2     = l2.getLatitude();
        double longitude2   = l2.getLongitude();
        return calculateDistance(latitude1,longitude1, latitude2, longitude2);
    }

    private double deg2rad(double deg) {
        return (double)(deg*Math.PI/(double)180d);
    }
    private double rad2deg(double rad) {
        return (double)(rad * (double)180d / Math.PI);
    }

    public static double dist(double lat1, double lng1, double lat2, double lng2) {
        CalDistance cd = new CalDistance(lat1, lng1, lat2, lng2);
        double dist = cd.getDistance();
        //Log.d(TAG, "-- getDistance:" + dist + " m");
        return dist;
    }

    public String getDistanceKmStr() {
        return String.format("%.2fkm",getDistance() / 1000.0);
    }
    public  String getDistanceMStr() {
        return String.format("%.2fm",getDistance());
    }

}