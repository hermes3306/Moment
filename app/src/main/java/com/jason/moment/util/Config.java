package com.jason.moment.util;

import android.os.Environment;

import java.io.File;
import java.util.Date;

public class Config {
    public static final long    _timer_period   = 10000; // 1 sec (최초이후 실행 주기)
    public static final long    _timer_delay    = 1000; // 1 sec (최초실행)
    public static boolean       _start_service  = false; // start location service
    public static boolean       _start_timer    = true; // start timer background scheduler
    public static String        _default_ext    = ".mnt"; // 기본확장자
    public static String        _filename_fmt   ="yyyyMMdd";

    public static File mediaStorageDir                 =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "Moment");

    public static String get_today_filename() {
        return StringUtil.DateToString(new Date(), _filename_fmt) + _default_ext;
    }

    public static String getAbsolutePath(String fname) {
        File file = new File(MyActivityUtil.getMediaStorageDirectory(), fname);
        return file.getAbsolutePath();
    }

}
