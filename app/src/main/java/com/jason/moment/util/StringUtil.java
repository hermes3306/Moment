package com.jason.moment.util;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {
    private static String TAG = "StringUtil";

    public static Date StringToDate(MyActivity a) {
        return StringToDate(a.cr_date + "_" + a.cr_time,"yyyy/MM/dd_HH:mm:ss" );
    }

    public static String getDateTimeString(MyActivity a) {
        return a.cr_date + "_" + a.cr_time;
    }

    public static String DateToString(Date date, String format) { // eg) format = "yyyy/MM/dd_HH:mm:ss");
        if(date==null) {
            Log.d(TAG, "-- date null");
            return null;
        }
        String dformat = format;
        if (format == null) dformat = "yyyy/MM/dd_HH:mm:ss";

        SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat(dformat);
        String date_to_string = dateformatyyyyMMdd.format(date);
        return date_to_string;
    }

    public static Date StringToDate (String str_date, String fmt)  {
        DateFormat formatter ;
        Date date ;

        try {
            formatter = new SimpleDateFormat(fmt);
            date = (Date)formatter.parse(str_date);
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    public static String Duration(Date start_date, Date end_date) {
        long endTime, startTime;
        startTime = start_date.getTime();
        endTime = end_date.getTime();
        long duration = endTime - startTime;

        long dur_sec = duration / 1000;
        long duration_r;

        int dur_hour_Int = (int)(dur_sec/3600);
        duration_r = dur_sec - (long)(dur_hour_Int) * 60 * 60;
        int dur_min_Int = (int)(duration_r / 60);
        duration_r = duration_r  - (long)(dur_min_Int) * 60;
        int dur_sec_Int = (int)(duration_r);

        String dur_str="";
        if(dur_hour_Int < 10) dur_str += "0";
        dur_str = dur_str + dur_hour_Int + ":";

        if(dur_min_Int < 10) dur_str += "0";
        dur_str = dur_str + dur_min_Int + ":";

        if(dur_sec_Int < 10) dur_str += "0";
        dur_str = dur_str + dur_sec_Int;

        return(dur_str);
    }
}
