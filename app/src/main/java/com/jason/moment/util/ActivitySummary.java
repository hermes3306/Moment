package com.jason.moment.util;

import java.io.Serializable;
import java.util.Date;

public class ActivitySummary implements Serializable {
    public String name;
    public double dist;
    public long duration;
    public double minpk;
    public int cal;

    public ActivitySummary(String name, double dist, long duration, double minpk, int cal) {
        this.name = name;
        this.dist = dist;
        this.duration = duration;
        this.minpk = minpk;
        this.cal = cal;
    }

    public String toString() {
        return "" + name + ": (" + dist + "Km," + duration + "," + minpk + " min/km," + cal + " calories)";
    }
}


