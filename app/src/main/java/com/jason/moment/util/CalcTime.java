package com.jason.moment.util;

import java.text.SimpleDateFormat;

public class CalcTime {
    public long bef, cur;
    public static String TAG = "CalTime";

    public CalcTime(long bef, long cur) {
        this.bef = bef;
        this.cur = cur;
    }

    public CalcTime(MyActivity bef, MyActivity cur) {
        String bef_added_on = bef.cr_date + " " + bef.cr_time;
        String cur_added_on = cur.cr_date + " " + cur.cr_time;

        this.bef = StringUtil.StringToDate(bef_added_on, "yyyy/MM/dd HH:mm:ss").getTime();
        this.cur = StringUtil.StringToDate(cur_added_on, "yyyy/MM/dd HH:mm:ss").getTime();
    }

    public long getBef() {
        return bef;
    }
    public void setBef(long bef) {
        this.bef = bef;
    }
    public long getCur() {
        return cur;
    }
    public void setCur(long cur) {
        this.cur = cur;
    }
    public long getElapsed() {
        return Math.abs(cur - bef);
    }
    public float getElapsedSec() {
        return (getElapsed())/1000.0f;
    }
    public float getElapsedMin() {
        return getElapsedSec()/60.0f;
    }
    public float getElapsedHour() {
        return getElapsedMin()/60.f;
    }
    public String getElapsedSecStr() {
        return String.format("%.0f초", getElapsedSec());
    }
    public String getElapsedMinStr() {
        return String.format("%.0f분", getElapsedMin());
    }

    public String getElapsedHourStr() {
        return String.format("%.0f시간%d분", getElapsedHour(), (int)getElapsedMin() - (int)getElapsedHour() * 60 );
    }

    public static String minperKmStr(double minperKm) {
        long t_sec = (long) (minperKm * 60);
        long t_min = t_sec / 60;
        t_sec = (long) (minperKm * 60) - t_min * 60;
        return "" + t_min + ":" + t_sec;
    }

    public static String durationMinStr(long durationInLong) {
        long t_dur_h = durationInLong / Config._ONE_HOUR;
        long t_dur_m = (durationInLong - (t_dur_h * Config._ONE_HOUR)) / Config._ONE_MIN;
        long t_dur_s = (durationInLong - (t_dur_h * Config._ONE_HOUR) - (t_dur_m * Config._ONE_MIN)) / Config._ONE_SEC;
        String durationMinStr;
        if (t_dur_h > 0) durationMinStr = "" + t_dur_h + ":" + t_dur_m + ":" + t_dur_s;
        else durationMinStr = t_dur_m + ":" + t_dur_s;
        return durationMinStr;
    }

}
