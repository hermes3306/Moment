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

    public MyActivity(double l1, double l2, Date d) {
        this.latitude = l1;
        this.longitude = l2;
        this.cr_date = StringUtil.DateToString(d,"yyyy/MM/dd");
        this.cr_time = StringUtil.DateToString(d,"HH:mm:ss");
    }

    public MyActivity(LatLng ll) {
        Date d = new Date();
        new MyActivity(ll.latitude, ll.longitude);
    }

    public MyActivity(double l1, double l2) {
        Date d = new Date();
        this.latitude = l1;
        this.longitude = l2;
        this.cr_date = StringUtil.DateToString(d,"yyyy/MM/dd");
        this.cr_time = StringUtil.DateToString(d,"HH:mm:ss");
    }

    public double getLatitude() { return this.latitude;}
    public double getLongitude() {return this.longitude;}


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
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.d("MyActivity","Err:" + sw.toString());
            return null;
        }
        return date;
    }

}
