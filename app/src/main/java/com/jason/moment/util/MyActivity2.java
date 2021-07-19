package com.jason.moment.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyActivity2 extends MyActivity implements Serializable {
    public double altitude;

    public MyActivity2(double l1, double l2, String crd, String crt) {
        super(l1,l2,crd,crt);
        this.altitude = 0;
    }

    public MyActivity2(double l1, double l2, Date d) {
        super(l1,l2,d);
        this.altitude = 0;
    }

    public MyActivity2(LatLng ll) {
        super(ll);
        this.altitude = 0;
    }

    public MyActivity2(double l1, double l2) {
        super(l1,l2);
        this.altitude = 0;
    }

    public MyActivity2(double l1, double l2, double alt) {
        super(l1,l2);
        this.altitude = alt;
    }

    public MyActivity2(double l1, double l2, double alt, Date d) {
        super(l1,l2,d);
        this.altitude = alt;
    }


    public String toString() {
        return "("+ latitude + "," + longitude + "," + altitude + "," + cr_date + "," + cr_time + ")";
    }

    public LatLng toLatLng() {
        return super.toLatLng();
    }
    public Date toDate() {
        return super.toDate();
    }

}
