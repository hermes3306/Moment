package com.jason.moment.util;

import java.util.Calendar;
import java.util.Date;

public class ActivityStat {
        public String name;
        public String date_str;

        public Date start;
        public Date end;
        public double distanceKm;
        public double distanceM;
        public String duration;
        public double minperKm;
        public int calories;
        public long durationInLong;

        public String memo;
        public String weather;
        public String co_runner;
        public int[] progress_info;
        public int[] chart_info;

        public ActivityStat(Date start, Date end, String duration, double distanceM, double distanceKm, double minperKm, int calories) {
            this.start = start;
            this.end = end;
            this.duration = duration;
            this.distanceM = distanceM;
            this.distanceKm = distanceKm;
            this.minperKm = minperKm;
            this.calories = calories;
            this.durationInLong = end.getTime() - start.getTime();

            String H = DateUtil.DateToString(start,"H");
            int t = Integer.parseInt(H);
            if(t >= 4 && t < 12) H = "아침 달리기";
            else if(t>=12 && t <=18) H = "오후 달리기";
            else if(t>18) H= "저녁 달리기";
            else if(t>21) H= "야간 달리기";
            else if(t<4) H= "새벽 달리기";

            this.name = DateUtil.DateToString(start,"E요일 ") + " " + H;
            this.date_str = DateUtil.DateToString(start,"yyyy년MM월dd일 HH:mm a");
        }

        public String toString() {
            return "" + name + "," + distanceKm + "Km," + duration + "," + minperKm + "," + calories;
        }

}
