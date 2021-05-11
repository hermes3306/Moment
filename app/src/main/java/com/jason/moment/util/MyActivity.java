package com.jason.moment.util;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jason.moment.util.StringUtil.StringToDate;

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
    public Date toDate() {
        DateFormat formatter ;
        Date date ;
        try {
            formatter = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss");
            date = (Date)formatter.parse(cr_date + "_" + cr_time);
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

}
