package com.jason.moment.util;

import android.os.Environment;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.Date;

public class Config {

    public static final int     _csv            = 0;
    public static final int     _ser            = 1;
    public static final int     _jsn            = 2;
    public static int           _default_ext    = _csv;

    public static final LatLng  _olympic_park   = new LatLng(37.519019,127.124820 );
    public static final float   _height         = 175;
    public static final Date    _age            = StringUtil.StringToDate("19700409","yyyyMMdd");
    public static final float   _weight         = 75;
    public static final float   _strideLengthInMeters = 0.5f;

    public static final long    _timer_period   = 10000;    // 1 sec (최초이후 실행 주기)
    public static final long    _timer_delay    = 1000;     // 1 sec (최초실행)

    public static boolean       _enable_network_provider = false;
    public static int           _loc_interval   = 1000;     // 1 sec
    public static float         _loc_distance   = 1f;       // 1 meter
    public static double        _minLocChange   = _loc_distance;

    public static boolean       _start_service  = false; // start location service
    public static boolean       _start_timer    = false; // start timer background scheduler

    public static String        _filename_fmt   ="yyyyMMdd";
    public static boolean       _save_onPause = false;

    public static int           _file_type_all  = 1;
    public static int           _file_type_day  = 2;
    public static int           _file_type_activity  = 3;

    public static String        _backup_url_dir = "http://ezehub.club/moment/";
    public static String[]      _backup_url_files= {
            "20210420.mnt",
            "20210430.mnt",
            "20210501.mnt",
            "20210502.mnt",
            "20210503.mnt",
            "20210507.mnt",
            "20210508.mnt"
    };


    public static final int PICK_FROM_CAMERA = 1;
    public static final int CALL_RUN_ACTIVITY = 2;
    public static final int CALL_SETTING_ACTIVITY = 3;
    public static final int CALL_START_ACTIVITY = 4;
    public static final int CALL_QUOTE_ACTIVITY = 5;

    public static final int perKM = 1;
    public static final int perMile = 2;
    public static final int per1KM = 3;
    public static final int per1Mile = 4;

    public static final int GENDER_MALE=1;
    public static final int GENDER_FEMALE=2;

    public static File mediaStorageDir                 =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "Moment1.4");

    public static File mediaStorageDir4csv             =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "CVS1.4");

    public static String get_today_filename() {
        return StringUtil.DateToString(new Date(), _filename_fmt) + ((_default_ext==_csv)? ".csv" : ".mnt");
    }

    public static String getAbsolutePath(String fname) {
        File file;
        if(_default_ext == _ser) {
            file = new File(mediaStorageDir, fname);
        }else if(_default_ext == _csv) {
            file = new File(mediaStorageDir4csv, fname);
        }else return null;
        return file.getAbsolutePath();
    }

    public static String getMediaStorageDirPath() {
        if(_default_ext == _ser) return mediaStorageDir.getAbsolutePath();
        else if(_default_ext == _csv) return mediaStorageDir4csv.getAbsolutePath();
        return null;
    }

}
