package com.jason.moment.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Config {

    static String TAG = "Config";
    static String _ver= "0.92";

    /* 파일 디코딩시 목표 크기 지정 100:흐림 400:보통 800:또렷 */
    public static int PIC_REQUIRED_SIZE = 400;

    static File cache_path = null;                  /* 0. 내부저장소 */
    static File file_path  = null;                  /* 1. 내부저장소 */
    static File external_path = null;               /* 2. 외부 저장소 */
    static File external_cache_path = null;         /* 3. 외부 저장소 */
    static File external_files_path1 = null;        /* 4. 외부 저장소 (PIC)*/
    static File external_files_path2 = null;        /* 5. 외부 저장소 (VIDEO)*/
    static File external_pub_files_path1 = null;    /* 6. 외부 공유저장소 */
    static File external_pub_files_path2 = null;    /* 7. 외부 공유저장소 */
    static File external_pub_files_path_csv = null;    /* 8. 외부 공유저장소 (CSV)*/
    static File external_pub_files_path_mnt = null;    /* 9. 외부 공유저장소 (MNT) */

    static File _SAVE_DIRS[]   = null;
    public static File PIC_SAVE_DIR   = null;
    public static File VIDEO_SAVE_DIR = null;
    public static File CVS_SAVE_DIR   = null;
    public static File MNT_SAVE_DIR   = null;



    public static String        _serverURL      = "http://ezehub.club/moment";
    public static String        _serverFolder   = "/upload";
    public static String        _uploadURL       = _serverURL + "/upload.php";
    public static String        _listFiles       = _serverURL + "/list.php";
    public static String        _listImageFiles  = _serverURL + "/list.php?dir=upload&&ext=jpeg";
    public static String        _listCSVFiles  = _serverURL + "/list.php?dir=upload&&ext=csv";
    public static String        _listSerFiles  = _serverURL + "/list.php?dir=upload&&ext=mnt";

    public static final int     _csv            = 0;
    public static final int     _ser            = 1;
    public static final int     _jsn            = 2;
    public static final int     _img            = 3;
    public static int           _default_ext    = _ser;

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

    public static final int PICK_FROM_CAMERA = 1;
    public static final int CALL_RUN_ACTIVITY = 2;
    public static final int CALL_SETTING_ACTIVITY = 3;
    public static final int CALL_START_ACTIVITY = 4;
    public static final int CALL_QUOTE_ACTIVITY = 5;
    public static final int CALL_PIC_ACTIVITY = 6;
    public static final int CALL_PIC3_ACTIVITY = 7;
    public static final int CALL_SCROLL_PIC_ACTIVITY = 8;
    public static final int PICK_FROM_VIDEO = 9;
    public static final int CALL_SCROLL_ALL_PIC_ACTIVITY = 10;


    public static final int perKM = 1;
    public static final int perMile = 2;
    public static final int per1KM = 3;
    public static final int per1Mile = 4;

    public static final int GENDER_MALE=1;
    public static final int GENDER_FEMALE=2;

    public static File mediaStorageDir                 =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "Moment" + _ver);
    public static File mediaStorageDir4csv             =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "CSV" + _ver);
    public static File mediaStorageDir4pic             =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "Picture" + _ver);

    static {
        if(!mediaStorageDir.exists()) mediaStorageDir.mkdirs();
        if(!mediaStorageDir4csv.exists()) mediaStorageDir4csv.mkdirs();
    }

    public static String getPicName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name  = "IMG_" + timeStamp + ".jpeg";
        return name;
    }

    public static String getVideoName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name  = "VIDEO_" + timeStamp + ".jpeg";
        return name;
    }
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

    static boolean initialized_file_provider = false;
    public static void initialize_file_provider(Context _ctx) {
        if(initialized_file_provider) return;
        cache_path = _ctx.getCacheDir(); /* 0. 내부저장소 */
        file_path  = _ctx.getFilesDir(); /* 1. 내부저장소 */
        external_path = Environment.getExternalStorageDirectory(); /* 2. 외부 저장소 */
        external_cache_path = _ctx.getExternalCacheDir(); /* 3. 외부 저장소 */
        external_files_path1 = _ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES); /* 4. 외부 저장소 Pictures */
        external_files_path2 = _ctx.getExternalFilesDir(Environment.DIRECTORY_MOVIES); /* 5. 외부 공유 저장소 Movies */
        external_pub_files_path1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS); /* 6.외부 공유저장소 */
        external_pub_files_path2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); /* 7.외부 공유저장소 */
        external_pub_files_path_csv = new File(external_pub_files_path1, "CSV" + _ver);    /* 8. 외부 공유저장소 (CSV)*/
        external_pub_files_path_mnt = new File(external_pub_files_path1, "Moment" + _ver);   /* 9. 외부 공유저장소 (MNT) */

        _SAVE_DIRS = new File[] {
            cache_path,                     /* 0. 내부 저장소 */
            file_path,                      /* 1. 내부 저장소 */
            external_path,                  /* 2. 외부 저장소 */
            external_cache_path,            /* 3. 외부 저장소 */
            external_files_path1,           /* 4. 외부 저장소 (Pictures) */
            external_files_path2,           /* 5 외부 저장소  (Movies) */
            external_pub_files_path1,       /* 6. 외부 공유 저장소 Document */
            external_pub_files_path2,       /* 7. 외부 공유 저장소 Pictures */
            external_pub_files_path_csv,    /* 8. 외부 공유 저장소 (CSV) */
            external_pub_files_path_mnt     /* 9. 외부 공유 저장소 (MNT)) */
        };

        Log.d(TAG, "--cache_path:" + cache_path);
        Log.d(TAG, "--file_path:" + file_path);
        Log.d(TAG, "--external_path:" + external_path);
        Log.d(TAG, "--external_cache_path:" + external_cache_path);
        Log.d(TAG, "--external_files_path1:" + external_files_path1);
        Log.d(TAG, "--external_files_path2:" + external_files_path2);
        Log.d(TAG, "--external_pub_files_path1:" + external_pub_files_path1);
        Log.d(TAG, "--external_pub_files_path2:" + external_pub_files_path2);

        PIC_SAVE_DIR   = _SAVE_DIRS[4];
        VIDEO_SAVE_DIR = _SAVE_DIRS[5];
        CVS_SAVE_DIR   = _SAVE_DIRS[8];
        MNT_SAVE_DIR   = _SAVE_DIRS[9];
        initialized_file_provider = true;
    }

    public static void initialize(Context _ctx) {
        initialize_file_provider(_ctx);
    }

}
