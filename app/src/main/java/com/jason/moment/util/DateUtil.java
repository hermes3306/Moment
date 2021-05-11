package com.jason.moment.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    // Common Utilities
    public static String DateToString(Date date, String format) { // eg) format = "yyyy/MM/dd HH:mm:ss"
        String dformat = format;
        if (format == null) dformat = "yyyy_MM_dd_HH_mm_ss";
        SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat(dformat);
        String date_to_string = dateformatyyyyMMdd.format(date);
        return date_to_string;
    }

    public static String today() {
        Date d = new Date();
        SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("yyyyMMdd");
        String date_to_string = dateformatyyyyMMdd.format(d);
        return date_to_string;
    }

    public static boolean isLongerThan1Min(Date before, Date after) {
        if(after.getTime()-before.getTime() > 1000 * 60) return true;
        return false;
    }


}
