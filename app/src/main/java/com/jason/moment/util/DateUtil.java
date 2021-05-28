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

    public static String getActivityName(Date d) {
        String H = DateUtil.DateToString(d,"H");
        int t = Integer.parseInt(H);
        if(t >= 4 && t < 12) H = "아침 활동";
        else if(t>=12 && t <=18) H = "오후 활동";
        else if(t>18) H= "저녁 활동";
        else if(t>21) H= "야간 활동";
        else if(t<4) H= "새벽 활동";
        String activityName = DateUtil.DateToString(d,"E요일 ") + " " + H;
        return activityName;
    }

    public static String getDateString(Date d) {
        String date_str = DateUtil.DateToString(d,"yyyy년MM월dd일 HH:mm a");
        return date_str;
    }

}
