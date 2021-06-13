package com.jason.moment.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ActivityStat {
        static String TAG = "ActivityStat";
        public String _fname;
        public String name;
        public String date_str;
        public String date_str2;

        public double distanceKm;
        public double distanceM;
        public String duration;
        public String durationM;
        public double minperKm;
        public String minperKms;
        public int calories;
        public long durationInLong;

        public String memo;
        public String weather;
        public String co_runner;
        public int[] progress_info;
        public int[] chart_info;

        public ActivityStat(String name, double distanceKm, long durationInLong, double minperKm, int calories) {
            this._fname = name;
            this.duration = duration;
            this.distanceKm = distanceKm;
            this.minperKm = minperKm;
            this.calories = calories;
            this.durationInLong = durationInLong;
            this.distanceM = distanceKm * 1000;

            String fnameWithoutExtension = name.substring(0,name.length()-4);
            Date start = new Date();
            if(fnameWithoutExtension.length() < 15) {  // Day Activity
                start = StringUtil.StringToDate(fnameWithoutExtension, "yyyyMMdd");
            }else { // Activity
                start = StringUtil.StringToDate(fnameWithoutExtension, "yyyyMMdd_HHmmss");
            }
            genStatInformation(start);
        }

        public static ActivityStat fromActivitySummary(ActivitySummary as) {
            return new ActivityStat(as.name, as.dist, as.duration, as.minpk, as.cal);
        }

        public ActivityStat(Date start, Date end, String duration, double distanceM, double distanceKm, double minperKm, int calories) {
            this.duration = duration;
            this.distanceM = distanceM;
            this.distanceKm = distanceKm;
            this.minperKm = minperKm;
            this.calories = calories;
            this.durationInLong = end.getTime() - start.getTime();
            genStatInformation(start);
        }

        void genStatInformation(Date start) {
            long t_dur_h = durationInLong/Config._ONE_HOUR;
            long t_dur_m = (durationInLong - (t_dur_h * Config._ONE_HOUR)) / Config._ONE_MIN;
            long t_dur_s = (durationInLong - (t_dur_h * Config._ONE_HOUR) - (t_dur_m * Config._ONE_MIN)) / Config._ONE_SEC;
            if(t_dur_h>0) this.durationM =  "" +  t_dur_h + ":" + t_dur_m + ":" + t_dur_s;
            else  this.durationM = t_dur_m + ":" + t_dur_s;

            long t_sec = (long)(minperKm * 60);
            long t_min = t_sec / 60;
            t_sec = (long)(minperKm * 60) - t_min * 60;
            this.minperKms = "" + t_min + ":" + t_sec;

            String H = DateUtil.DateToString(start,"H");
            int t = Integer.parseInt(H);
            if(t >= 4 && t < 12) H = "아침 달리기";
            else if(t>=12 && t <=18) H = "오후 달리기";
            else if(t>18) H= "저녁 달리기";
            else if(t>21) H= "야간 달리기";
            else if(t<4) H= "새벽 달리기";

            this.name = DateUtil.DateToString(start,"E요일 ") + " " + H;
            this.date_str = DateUtil.DateToString(start,"yyyy년MM월dd일 HH:mm a");
            this.date_str2 = DateUtil.DateToString(start,"MM월dd일 HH:mm a");
        }

        public String toString() {
            return "" + name + "," + distanceKm + "Km," + duration + "," + minperKm + "," + calories;
        }

        public static ActivityStat getActivityStat(ArrayList<MyActivity> list) {
        if(list == null) {
            return null;
        }
        if(list.size() < 2) {
            return null;
        }

        MyActivity start, stop;
        start = list.get(0);
        stop = list.get(list.size()-1);

        Date start_date, stop_date;
        start_date = StringUtil.StringToDate(start);
        stop_date = StringUtil.StringToDate(stop);

        Log.d(TAG, "-- start_date:" + start_date);
        Log.d(TAG, "-- stop_date:" + stop_date);

        String duration = StringUtil.elapsedStr(start_date, stop_date); // <- Error code
        Log.e(TAG, duration);

        double total_distM = MyActivityUtil.getTotalDistanceInDouble(list);  // <-
        double total_distKm = total_distM / 1000f;
        double minpk = MyActivityUtil.getMinPerKm(start_date, stop_date, total_distKm); // <-

        float burntkCal;
        int durationInSeconds = MyActivityUtil.durationInSeconds(list);
        int stepsTaken = (int) (total_distM / Config._strideLengthInMeters);
        burntkCal = CaloryUtil.calculateEnergyExpenditure((float)total_distM / 1000f, durationInSeconds);
        ActivityStat as = new ActivityStat(start_date, stop_date, duration, total_distM, total_distKm, minpk, (int)burntkCal);
        return as;
    }

}
