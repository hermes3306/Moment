package com.jason.moment.util;

import java.io.Serializable;

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

    public String toString() {
        return "("+ latitude + "," + longitude + "," + cr_date + "," + cr_time + ")";
    }
}
