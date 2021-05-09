package com.jason.moment.util;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;

public class MyActivity implements Serializable {
    public double latitude;
    public double longitude;
    public String cr_date;
    public String cr_time;

    public MyActivity(double l1, double l2, String crd, String crt) {
        this.latitude = l1;
        this.longitude = l2;
        this.cr_date = crd;
        this.cr_time = crt;
    }

    public MyActivity(double l1, double l2) {
        Date d = new Date();
        this.latitude = l1;
        this.longitude = l2;
        this.cr_date = StringUtil.DateToString(d,"yyyy/MM/dd");
        this.cr_time = StringUtil.DateToString(d,"HH:mm:ss");
    }

    public MyActivity(double l1, double l2, Date d) {
        this.latitude = l1;
        this.longitude = l2;
        this.cr_date = StringUtil.DateToString(d,"yyyy/MM/dd");
        this.cr_time = StringUtil.DateToString(d,"HH:mm:ss");
    }

    public String toString() {
        return "("+ latitude + "," + longitude + "," + cr_date + "," + cr_time + ")";
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }
}
