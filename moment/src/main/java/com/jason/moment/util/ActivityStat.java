package com.jason.moment.util;

import android.util.Log;

import java.util.ArrayList;
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

        public void setExtra() {
            this.memo = "메모가 없습니다.";
            this.weather = "날씨 정보가 없습니다.";
            this.co_runner = "함께 달린 사람이 없습니다.";
        }

        static ActivityStat instance = null;
        public static ActivityStat getInstance() {
            if(instance==null) instance = new ActivityStat();
            return instance;
        }

        public ActivityStat() {}

        public ActivityStat(String name, double distanceKm, long durationInLong, double minperKm, int calories) {
            this._fname = name;
            this.distanceKm = distanceKm;
            this.minperKm = minperKm;
            this.calories = calories;
            this.durationInLong = durationInLong;
            this.distanceM = distanceKm * 1000;
            setExtra();

            String fnameWithoutExtension = name.substring(0,name.length()-4);
            Date start;
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
            setExtra();
            genStatInformation(start);
        }

        void genStatInformation(Date start) {
            this.durationM = CalcTime.durationMinStr(durationInLong);
            this.minperKms = CalcTime.minperKmStr(minperKm);

            String H = DateUtil.DateToString(start,"H");
            int t = Integer.parseInt(H);
            if(t >= 4 && t < 12) H = "Morning Run";
            else if(t>=12 && t <=18) H = "Afternoon Run";
            else if(t<4) H= "Early Morning Run";
            else if(t>21) H= "Night Run";
            else  H= "Evening Run";

//            this.name = DateUtil.DateToString(start,"E요일 ") + " " + H;
//            this.date_str = DateUtil.DateToString(start,"yyyy년MM월dd일 HH:mm a");
//            this.date_str2 = DateUtil.DateToString(start,"MM월dd일 HH:mm a");

            this.name = DateUtil.DateToString(start, "EEEE") + " " + H;
            this.date_str = DateUtil.DateToString(start, "MMMM d, yyyy h:mm a");
            this.date_str2 = DateUtil.DateToString(start, "MMMM d, h:mm a");
        }

        public String toString() {
            return  name + "," + distanceKm + "Km," + duration + "," + minperKm + "," + calories;
        }

        public ActivityStat stat(ArrayList<MyActivity> list) {
            return getActivityStat(list);
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
        burntkCal = CaloryUtil.calculateEnergyExpenditure((float)total_distM / 1000f, durationInSeconds);
        return new ActivityStat(start_date, stop_date, duration, total_distM, total_distKm, minpk, (int)burntkCal);
    }

}
