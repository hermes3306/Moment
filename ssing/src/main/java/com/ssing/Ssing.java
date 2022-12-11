package com.ssing;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ssing {

    public double latitude;
    public double longitude;
    public String id;
    public int battery;
    public String status; //running, parked, out_of_order, preparing
    public Ssing(double l1, double l2, String id, int battery, String status) {
        this.latitude = l1;
        this.longitude = l2;
        this.id = id;
        this.battery = battery;
        this.status = status;
    }

    public Ssing(Ssing ss) {
        this.latitude = ss.latitude;
        this.longitude = ss.longitude;
        this.id = ss.id;
        this.battery = ss.battery;
        this.status = ss.status;
    }

    public String toString() {
        return "("+ id + "," + status + "," + battery + "%," + latitude + "," + longitude + ")";
    }

    public String getTitle() {
        return "[Ssing"+id+"]" + battery +"% (" + status +")";
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }

}


