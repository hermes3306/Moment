package com.jason.moment.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.jason.moment.R;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class Config {

    static String TAG                       = "Config";
    static String _ver                      = "7";

    public static long  _ONE_SEC            = 1000;
    public static long  _ONE_MIN            = _ONE_SEC * 60;
    public static long  _ONE_HOUR           = _ONE_MIN * 60;

    public static boolean _sharedPreferenceChanged                = false;

    // 정리 필요함
    public final static String PACKAGE_NAME = Config.class.getPackage().getName();
    public final static String INTENT_TRACK_WP = Config.PACKAGE_NAME + ".intent.TRACK_WP";
    public final static String INTENT_UPDATE_WP = Config.PACKAGE_NAME + ".intent.UPDATE_WP";
    public final static String INTENT_DELETE_WP = Config.PACKAGE_NAME + ".intent.DELETE_WP";
    public final static String INTENT_START_TRACKING = Config.PACKAGE_NAME + ".intent.START_TRACKING";
    public final static String INTENT_STOP_TRACKING = Config.PACKAGE_NAME + ".intent.STOP_TRACKING";

    // my first broadcasting intent
    public final static String INTENT_LOCATION_CHANGED = Config.PACKAGE_NAME+ ".intent.LOCATION_CHANGED";
    public final static String INTENT_CONFIG_CHANGE = Config.PACKAGE_NAME+ ".intent.CONFIG_CHANGE";

    public static String _pic_ext           = ".jpeg";
    public static String _mov_ext           = ".mp4";
    public static String _csv_ext           = ".csv";
    public static String _mnt_ext           = ".mnt";

    public static String _notify_ticker     = "Jason";
    public static int _notify_id            = 100;

    /* 지도 */
    public static float _marker_start_color = BitmapDescriptorFactory.HUE_RED;
    public static float _marker_end_color = BitmapDescriptorFactory.HUE_GREEN;
    public static float _marker_color = BitmapDescriptorFactory.HUE_CYAN;
    public static int _marker_icon = R.drawable.drawmarker24;

    public static float _marker_alpha = 0.9f;

    public static int _track_color          = Color.RED;
    public static int _track_width          = 15;
    public static int _default_start_layout = R.layout.activity_start_style1;
    public static int _default_file_layout  = R.layout.activity_file;

    /* 파일 디코딩시 목표 크기 지정 100:흐림 400:보통 800:또렷 */
    public static int PIC_REQUIRED_SIZE     = 400;

    static File cache_path                  = null;    /* 0. 내부저장소 */
    static File file_path                   = null;    /* 1. 내부저장소 */
    static File external_path               = null;    /* 2. 외부 저장소 */
    static File external_cache_path         = null;    /* 3. 외부 저장소 */
    static File external_files_path1        = null;    /* 4. 외부 저장소 (PIC)*/
    static File external_files_path2        = null;    /* 5. 외부 저장소 (VIDEO)*/
    static File external_pub_files_path1    = null;    /* 6. 외부 공유저장소 */
    static File external_pub_files_path2    = null;    /* 7. 외부 공유저장소 */
    static File external_pub_files_path_csv = null;    /* 8. 외부 공유저장소 (CSV)*/
    static File external_pub_files_path_mnt = null;    /* 9. 외부 공유저장소 (MNT) */
    static File external_pub_files_path_pic = null;    /* 10. 외부 공유저장소 (PIC) */
    static File external_pub_files_path_mov = null;    /* 11. 외부 공유저장소 (MOV) */
    static File external_pub_files_path_mp3 = null;    /* 12. 외부 공유저장소 (MP3) */
    static File external_pub_files_path_apk = null;    /* 13. 외부 공유저장소 (APK) */
    static File external_pub_files_path_jsn = null;    /* 14. 외부 공유저장소 (JSN) */

    static File[] _SAVE_DIRS = null;
    public static File CSV_SAVE_DIR         = null;
    public static File MNT_SAVE_DIR         = null;
    public static File PIC_SAVE_DIR         = null;
    public static File PIC_GALLERY_DIR      = null;
    public static File MOV_SAVE_DIR         = null;
    public static File MP3_SAVE_DIR         = null;
    public static File APK_SAVE_DIR         = null;
    public static File JSN_SAVE_DIR         = null;


    public static String        _serverURL      = "http://ezehub.club/moment";        //Z
    public static String        _apkname        = "moment.apk";
    public static String        _apkurl         = _serverURL + "/apk/moment.apk";
    public static String        _apkver         = _serverURL + "/apk/version";

    //public static String        _serverURL      = "http://ezehub.club:8080/moment";   //W
    //public static String        _serverURL      = "http://ezehub.club:8888/moment";   //M
    //public static String        _serverURL      = "http://ezehub.club:8899/moment";     //DESKTOP1
    public static String        _serverFolder   = "/upload";
    public static String        _sererMP3Folder = "/mp3";
    public static String        _uploadURL      = _serverURL + "/upload.php";
    public static String        _listFiles      = _serverURL + "/list.php";
    public static String        _listImageFiles = _serverURL + "/list.php?dir=upload&&ext=jpeg";
    public static String        _listMovFiles   = _serverURL + "/list.php?dir=upload&&ext=mp4";
    public static String        _listCSVFiles   = _serverURL + "/list.php?dir=upload&&ext=csv";
    //public static String        _listCSVFiles   = _serverURL + "/list.php?dir=tmp&&ext=csv";
    public static String        _listSerFiles   = _serverURL + "/list.php?dir=upload&&ext=mnt";
    public static String        _listMP3Files   = _serverURL + "/list.php?dir=mp3&&ext=mp3";

    public static final int     _csv            = 0;
    public static final int     _ser            = 1;
    public static final int     _jsn            = 2;
    public static final int     _img            = 3;
    public static final int     _mov            = 4;
    public static final int     _mp3            = 5;
    public static final int     _apk            = 6;

    public static int           _default_ext    = _csv;

    public static final LatLng  _olympic_park   = new LatLng(37.519019,127.124820 );
    public static final float   _height         = 1.75f;  // in meters
    public static final Date    _age            = StringUtil.StringToDate("19700409","yyyyMMdd");
    public static final float   _weight         = 75;
    public static final float   _strideLengthInMeters = 0.5f;
    public static final int     GENDER_MALE     =1;
    public static final int     GENDER_FEMALE   =2;
    public static int           _gender = GENDER_MALE;

    public static final long    _timer_period   = 10000;    // 1 sec (최초이후 실행 주기)
    public static final long    _timer_delay    = 1000;     // 1 sec (최초실행)

    public static boolean       _enable_network_provider = false;
    public static int           _loc_interval   = 1000;     // 1 sec
    public static float         _loc_distance   = 1f;       // 1 meter

    public static boolean       _start_service  = true; // start location service
    public static boolean       _start_timer    = false; // start timer background scheduler

    public static String        _filename_fmt   ="yyyyMMdd";
    public static boolean       _save_onPause = true;

    public static int           _file_type_all  = 1;
    public static int           _file_type_day  = 2;
    public static int           _file_type_activity  = 3;

    public static final int PICK_FROM_CAMERA        = 1;
    public static final int PICK_FROM_CAMERA2       = 2;
    public static final int CALL_RUN_ACTIVITY       = 3;
    public static final int CALL_SETTING_ACTIVITY   = 4;
    public static final int CALL_START_ACTIVITY     = 41;
    public static final int CALL_QUOTE_ACTIVITY     = 5;
    public static final int CALL_PIC_ACTIVITY       = 6;
    public static final int CALL_PIC3_ACTIVITY      = 7;
    public static final int CALL_SCROLL_PIC_ACTIVITY = 8;
    public static final int PICK_FROM_VIDEO         = 9;
    public static final int CALL_SCROLL_ALL_PIC_ACTIVITY = 10;
    public static final int CALL_FILE_ACTIVITY      = 11;
    public static final int CALL_REPORT_ACTIVITY    = 12;
    public static final int CALL_START_NEW_ACTIVITY = 13;
    public static final int CALL_RESULT_LOAD_IMAGE = 14;

    public static final int perKM       = 1;
    public static final int perMile     = 2;
    public static final int per1KM      = 3;
    public static final int per1Mile    = 4;

    static String mnt_folder_name       = "Moment_MNT" + _ver;
    static String csv_folder_name       = "Moment_CSV" + _ver;
    static String pic_folder_name       = "Moment_PIC" + _ver;
    static String mov_folder_name       = "Moment_MOV" + _ver;
    static String jsn_folder_name       = "Moment_JSN" + _ver;
    static String mp3_folder_name       = "Moment_MP3" + _ver;
    static String apk_folder_name       = "Moment_APK" + _ver;

    private static SharedPreferences sharedPreferences = null;
    private static Context _ctx4SharedPreferences = null;


    public static String getPreference(Context context, String name) {
        String _preference_val = sharedPreferences.getString(name, "0");
        return _preference_val;
    }

    public static int getIntPreference(Context context, String name) {
        int _preference_val = 0;
        try {
            _preference_val = Integer.parseInt(sharedPreferences.getString(name, "0"));
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        return _preference_val;
    }

    public static String getTmpPicName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name  = "" + timeStamp + _pic_ext;
        return name;
    }

    public static String getTmpVideoName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name  = "" + timeStamp + _mov_ext;
        return name;
    }
    public static String get_today_filename() {
        return StringUtil.DateToString(new Date(), _filename_fmt) + ((_default_ext==_csv)? ".csv" : ".mnt");
    }

    public static String getAbsolutePath(String fname) {
        File file;
        if(_default_ext == _ser) {
            file = new File(mediaStorageDir4mnt, fname);
        }else if(_default_ext == _csv) {
            file = new File(mediaStorageDir4csv, fname);
        }else return null;
        return file.getAbsolutePath();
    }

    public static String getMediaStorageDirPath() {
        if(_default_ext == _ser) return mediaStorageDir4mnt.getAbsolutePath();
        else if(_default_ext == _csv) return mediaStorageDir4csv.getAbsolutePath();
        return null;
    }

    static File mediaStorage =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    public static File mediaStorageDir4mnt  = new File(mediaStorage, mnt_folder_name);
    public static File mediaStorageDir4csv  = new File(mediaStorage, csv_folder_name);
    public static File mediaStorageDir4pic  = new File(mediaStorage, pic_folder_name);
    public static File mediaStorageDir4mov  = new File(mediaStorage, mov_folder_name);
    public static File mediaStorageDir4jsn  = new File(mediaStorage, jsn_folder_name);
    public static File mediaStorageDir4mp3  = new File(mediaStorage, mp3_folder_name);
    public static File mediaStorageDir4apk  = new File(mediaStorage, apk_folder_name);


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
        external_pub_files_path_csv = new File(external_pub_files_path1, csv_folder_name);    /* 8. 외부 공유저장소 (CSV)*/
        external_pub_files_path_mnt = new File(external_pub_files_path1, mnt_folder_name);   /* 9. 외부 공유저장소 (MNT) */
        external_pub_files_path_pic = new File(external_pub_files_path1, pic_folder_name);   /* 10. 외부 공유저장소 (PIC) */
        external_pub_files_path_mov = new File(external_pub_files_path1, mov_folder_name);   /* 11. 외부 공유저장소 (MOV) */
        external_pub_files_path_mp3 = new File(external_pub_files_path1, mp3_folder_name);   /* 12. 외부 공유저장소 (MP3) */
        external_pub_files_path_apk = new File(external_pub_files_path1, apk_folder_name);   /* 13. 외부 공유저장소 (APK) */
        external_pub_files_path_jsn = new File(external_pub_files_path1, jsn_folder_name);   /* 13. 외부 공유저장소 (APK) */

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
            external_pub_files_path_mnt,    /* 9. 외부 공유 저장소 (MNT)) */
            external_pub_files_path_pic,    /* 10. 외부 공유 저장소 (PIC)) */
            external_pub_files_path_mov,    /* 11. 외부 공유 저장소 (MOV)) */
            external_pub_files_path_mp3,    /* 12. 외부 공유 저장소 (MP3)) */
            external_pub_files_path_apk,     /* 13. 외부 공유 저장소 (APK)) */
            external_pub_files_path_jsn     /* 14. 외부 공유 저장소 (JSN)) */
        };

        Log.d(TAG, "--cache_path:" + cache_path);
        Log.d(TAG, "--file_path:" + file_path);
        Log.d(TAG, "--external_path:" + external_path);
        Log.d(TAG, "--external_cache_path:" + external_cache_path);
        Log.d(TAG, "--external_files_path1:" + external_files_path1);
        Log.d(TAG, "--external_files_path2:" + external_files_path2);
        Log.d(TAG, "--external_pub_files_path1:" + external_pub_files_path1);
        Log.d(TAG, "--external_pub_files_path2:" + external_pub_files_path2);

        PIC_GALLERY_DIR   = _SAVE_DIRS[7];
        CSV_SAVE_DIR   = _SAVE_DIRS[8];
        MNT_SAVE_DIR   = _SAVE_DIRS[9];
        PIC_SAVE_DIR   = _SAVE_DIRS[10];
        MOV_SAVE_DIR   = _SAVE_DIRS[11];
        MP3_SAVE_DIR   = _SAVE_DIRS[12];
        APK_SAVE_DIR   = _SAVE_DIRS[13];
        JSN_SAVE_DIR   = _SAVE_DIRS[14];

        if(!CSV_SAVE_DIR.exists()) CSV_SAVE_DIR.mkdirs();
        if(!MNT_SAVE_DIR.exists()) MNT_SAVE_DIR.mkdirs();
        if(!PIC_SAVE_DIR.exists()) PIC_SAVE_DIR.mkdirs();
        if(!MOV_SAVE_DIR.exists()) MOV_SAVE_DIR.mkdirs();
        if(!MP3_SAVE_DIR.exists()) MP3_SAVE_DIR.mkdirs();
        if(!APK_SAVE_DIR.exists()) APK_SAVE_DIR.mkdirs();
        if(!JSN_SAVE_DIR.exists()) JSN_SAVE_DIR.mkdirs();

        initialized_file_provider = true;
    }

    public static void init_preference_values_running(String interval, String distance) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("interval",interval);
        editor.putString("distance",distance);
        _sharedPreferenceChanged = true;
    }

    public static void init_preference_value_running_default() {
        init_preference_values_running("1000","1"); // 1sec, 1meter
    }

    public static void init_preference_values(Context _ctx) {
        _ctx4SharedPreferences = _ctx;
        if(sharedPreferences==null ) sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(_ctx /* Activity context */);

        Map<String,?> keys = sharedPreferences.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d(TAG,"--    " + entry.getKey() + ": " + entry.getValue().toString());
        }

        Config._enable_network_provider = sharedPreferences.getBoolean("NetworkProvider", Config._enable_network_provider);
        String _loc_interval = sharedPreferences.getString("interval", "10000");
        String _loc_distance = sharedPreferences.getString("distance", "1");
        int[] colors = {
                Color.RED, Color.CYAN, Color.BLUE, Color.WHITE, Color.BLACK, Color.YELLOW, Color.DKGRAY, Color.GREEN, Color.LTGRAY
        };

        try {
            Config._loc_interval = parseInt(_loc_interval);
            Config._loc_distance = parseFloat(_loc_distance);
            Config._track_color = colors[Config.getIntPreference(_ctx, "track_color")];
            Config._track_width = Config.getIntPreference(_ctx, "track_width");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
    }

    public static void initialize(Context _ctx) {
        initialize_file_provider(_ctx);
        init_preference_values(_ctx);
    }

    static {
        if(!mediaStorageDir4mnt.exists()) mediaStorageDir4mnt.mkdirs();
        if(!mediaStorageDir4csv.exists()) mediaStorageDir4csv.mkdirs();
        if(!mediaStorageDir4pic.exists()) mediaStorageDir4pic.mkdirs();
        if(!mediaStorageDir4mov.exists()) mediaStorageDir4mov.mkdirs();
        if(!mediaStorageDir4jsn.exists()) mediaStorageDir4jsn.mkdirs();
        if(!mediaStorageDir4mp3.exists()) mediaStorageDir4mp3.mkdirs();
        if(!mediaStorageDir4apk.exists()) mediaStorageDir4apk.mkdirs();
    }


}
