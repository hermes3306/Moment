package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.jason.moment.service.GPSLogger;
import com.jason.moment.service.GPSLoggerServiceConnection;
import com.jason.moment.util.AddressUtil;
import com.jason.moment.util.AlertDialogUtil;
import com.jason.moment.util.C;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CalcTime;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.LocationUtil;
import com.jason.moment.util.MP3;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.NotificationUtil;
import com.jason.moment.util.PermissionUtil;
import com.jason.moment.util.StartupBatch;
import com.jason.moment.util.db.MyLoc;
import com.jason.moment.util.db.MyMedia;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;
import static java.lang.Integer.parseInt;

// 2021/05/03, MapsActivity extends AppCompatActivity instead of FragmentActivity
public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener {
    private GoogleMap googleMap=null;
    private Context _ctx;
    private Intent gpsLoggerServiceIntent = null;

    private static final String TAG = "MapsActivity";
    private static final int DEFAULT_ZOOM = 15;
    public static boolean firstCall = true;
    public static boolean paused = false;

    public ImageButton imb_Running;
    public TextView tv_log;
    public ImageButton imbt_prev = null;
    public ImageButton imbt_next = null;
    public TextView tv_activity_name = null;
    public TextView tv_date_str = null;
    public ImageButton imbt_pop_menu = null;
    public ImageButton imbt_down = null;
    public ImageButton imbt_hide_arrow = null;
    public ImageButton imbt_up = null;
    public ImageButton imbt_save = null;
    public ImageButton imbt_marker = null;
    public ImageButton imbt_navi = null;

    static boolean already_quit = false;
    public void alertQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("활동을 중지하시겠습니까?");
        builder.setMessage("활동을 정말 중지하시겠습니까?");
        builder.setPositiveButton("중지",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG,"-- alertQuitDialog()");
                        already_quit = true;
                        SerializeTodayActivity();
                        Log.e(TAG,"-- SerializeTodayActivity()");

                        if(receiver != null) {
                            unregisterReceiver(receiver);
                        }

                        if(gpsLoggerConnection != null)  {
                            unbindService(gpsLoggerConnection);
                            Log.e(TAG,"-- unbindService()");
                            gpsLoggerConnection = null;
                        }
                        if(gpsLoggerServiceIntent != null) {
                            stopService(gpsLoggerServiceIntent);
                            gpsLoggerServiceIntent = null;
                            Log.e(TAG,"-- stopService()");
                        }
                        Log.e(TAG,"-- before MapsActivity.this.finish()");
                        MapsActivity.this.finish();
                        Log.e(TAG,"-- after MapsActivity.this.finish()");
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    /**
     * Receives Intent for new Location from GPS services
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(paused) return;

            Log.d(TAG, "-- Received intent " + intent.getAction());
            if (Config.INTENT_LOCATION_CHANGED.equals(intent.getAction())) {
                // Track a way point
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Log.d(TAG, "-- got broad casting message of INTENT_LOCATION_CHANGED ");
                    Location location = (Location)extras.get("location");
                    Log.d(TAG,"-- Broad casting Location received:" + location);
                    onLocationChanged(location);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        Log.e(TAG,"-- onDestroy()");
        // Unregister broadcast receiver
        if(gpsLoggerConnection != null)  unbindService(gpsLoggerConnection);
        if(gpsLoggerServiceIntent != null) stopService(gpsLoggerServiceIntent);
        Log.e(TAG,"-- after onDestroy()");
        super.onDestroy();
    }

    public void registerLocationChangedReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.INTENT_LOCATION_CHANGED);
        registerReceiver(receiver, filter);
        Log.d(TAG, "-- INTENT LOCATION CHANGED registerReceiver()");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PermissionUtil.getInstance().setPermission(this);
        this._ctx = this;
        Config.initialize(getApplicationContext());

        StartupBatch sb = new StartupBatch(_ctx);
        sb.execute();

        // Register our broadcast receiver
        registerLocationChangedReceiver();


        currentTrackId = DateUtil.today();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // ----------------------------------------------------------------------
        // Configuration here
        // _start_service : run location service
        // _start_timer : my Timer
        // ----------------------------------------------------------------------
        //
        imb_Running = (ImageButton) findViewById(R.id.imb_Running);
        tv_log = (TextView) findViewById(R.id.tv_log);
        imbt_prev = (ImageButton) findViewById(R.id.imbt_prev);
        imbt_next = (ImageButton) findViewById(R.id.imbt_next);
        tv_date_str = (TextView) findViewById(R.id.tv_date_str);
        tv_activity_name = (TextView) findViewById(R.id.tv_activity_name);
        imbt_pop_menu = (ImageButton) findViewById(R.id.imbt_pop_menu);
        //imbt_globe = (ImageButton) findViewById(R.id.imbt_Globe);
        imbt_save = (ImageButton) findViewById(R.id.imbt_Save);
        imbt_up = (ImageButton) findViewById(R.id.imbt_up);
        imbt_down = (ImageButton) findViewById(R.id.imbt_Down);
        imbt_marker = (ImageButton) findViewById(R.id.imbt_marker);
        imbt_hide_arrow = (ImageButton) findViewById(R.id.imbt_hide_arrow);
        imbt_navi = (ImageButton) findViewById(R.id.imbt_navi);
//        imbt_trash = (ImageButton) findViewById(R.id.imbt_trash);


        gpsLoggerServiceIntent = new Intent(this, GPSLogger.class);
        String activity_file_name = DateUtil.today();
        gpsLoggerServiceIntent.putExtra("activity_file_name", activity_file_name );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(MapsActivity.this, GPSLogger.class)); // 서비스 시작
        } else {
            startService(new Intent(MapsActivity.this, GPSLogger.class)); // 서비스 시작
        }
        gpsLoggerConnection = new GPSLoggerServiceConnection(this); // 서비스 바인딩
        bindService(gpsLoggerServiceIntent,gpsLoggerConnection, 0);

        if(this.getGpsLogger()!=null) this.getGpsLogger().set_use_broadcast(true);

        list = MyLoc.getInstance(_ctx).getToodayActivities();
        Toast.makeText(_ctx, "# of Today's activities are " + list.size(), Toast.LENGTH_LONG).show();

        // check last activity not saving...
        AlertDialogUtil.getInstance().checkActiveRunning(_ctx);
    }

    // GPS Logger 관련 함수 들
    // 정리 필요함
    private ServiceConnection gpsLoggerConnection = null;
    private String currentTrackId;
    GPSLogger gpsLogger = null;
    public String getCurrentTrackId() {
        return this.currentTrackId;
    }
    public void setGpsLogger(GPSLogger l) {
        this.gpsLogger = l;
    }
    public GPSLogger getGpsLogger() {
        return gpsLogger;
    }

    public void boardCastConfigChanged(long gpsLoggingInterval, long gpsLoggingMinDistance ) {
        Intent intent = new Intent(Config.INTENT_CONFIG_CHANGE);
        intent.putExtra("gpsLoggingInterval", gpsLoggingInterval);
        intent.putExtra("gpsLoggingMinDistance", gpsLoggingMinDistance);
        sendBroadcast(intent);
        Log.e(TAG, "--INTENT_CONFIG_CHANGED message sent :");
        Log.e(TAG, "--gpsLoggingInterval:" + gpsLoggingInterval);
        Log.e(TAG, "--gpsLoggingMinDistance:" +  gpsLoggingMinDistance);
    }

    private void initializeMap() {
        // check if map is created
        if (googleMap == null) {
            //googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(); // creates the map
            // check if map is created successfully or not
            if (googleMap == null) {
                //      Log.e(TAG,"-- Map cannot not be created. because the map is not ready!");
//                Toast.makeText(getApplicationContext(),
//                        "Map could not be created", Toast.LENGTH_SHORT)
//                        .show();
            }
        }
    }


    private void get_last_run_from_db() {
        long cur_pk = LocationUtil.getInstance().get_last_pk();
        if(last_pk != -1 && last_pk < cur_pk) {
            Toast.makeText(_ctx, "Last pk: "
                    + last_pk + "\nCurrent pk: "
                    + cur_pk + "\n" + (cur_pk-last_pk) +
                    " gaps", Toast.LENGTH_LONG).show();

            Log.e(TAG, "----- HERE ----------");
            Log.e(TAG, "----- HAVE TO PROCESS from last_pk ----------");
            Log.e(TAG, "----- paused_last_pk : " + last_pk );
            Log.e(TAG, "----- current_last_pk : " + LocationUtil.getInstance().get_last_pk() );

            ArrayList<MyActivity> t = MyLoc.getInstance(_ctx).getActivitiesFrom(last_pk);
            for(int i=0;i<t.size();i++) {
                Log.e(TAG,"----- " + t.get(i).toString());
                list.add(t.get(i));
            }
        }
    }


    @Override
    protected void onResume() {
        paused = false;
        Log.d(TAG,"-- onResume.");
        get_last_run_from_db();

//        startService(gpsLoggerServiceIntent);
//        if(gpsLoggerConnection==null)
//            gpsLoggerConnection = new GPSLoggerServiceConnection(this);
//        bindService(gpsLoggerServiceIntent, gpsLoggerConnection, 0);
//        registerLocationChangedReceiver();
//
//

        gpsLoggerServiceIntent = new Intent(this, GPSLogger.class);
        String activity_file_name = DateUtil.today();
        gpsLoggerServiceIntent.putExtra("activity_file_name", activity_file_name );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(MapsActivity.this, GPSLogger.class)); // 서비스 시작
        } else {
            startService(new Intent(MapsActivity.this, GPSLogger.class)); // 서비스 시작
        }
        gpsLoggerConnection = new GPSLoggerServiceConnection(this); // 서비스 바인딩
        bindService(gpsLoggerServiceIntent,gpsLoggerConnection, 0);


        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        String _filetype = sharedPreferences.getString("filetype", "0");

        try {
            Config._default_ext = parseInt(_filetype);
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        MyActivityUtil.initialize();
        initializeMap();

        if(this.getGpsLogger()!=null) this.getGpsLogger().set_use_broadcast(true);
        super.onResume();
    }

    void SerializeTodayActivity() {
        ArrayList<MyActivity> myal = new MyLoc(getApplicationContext()).getToodayActivities();
        String activity_file_name = DateUtil.today();
        if(myal.size()>0) {
            String file_name = DateUtil.today() + "_" + C.getRunnerName(getApplicationContext());
            MyActivityUtil.serialize(myal, file_name);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"-- onBackPressed.");
        alertQuitDialog();
    }

    long last_pk = -1;

    @Override
    protected void onPause() {
        // 배터리 절약을 위해서 마지막 PK를 저장하고 Loc Change 메시지를 받지 않는다.
        last_pk = LocationUtil.getInstance().get_last_pk();
        if(this.getGpsLogger()!=null) this.getGpsLogger().set_use_broadcast(false);

        if(receiver != null) {
            try {
                unregisterReceiver(receiver);
            }catch(Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG, "-- " + sw);
            }
        }

        if(already_quit) {
            super.onPause();
            return;
        } else {
            paused = true;
            try {
                if (gpsLogger != null) {
                    if (gpsLoggerConnection != null) unbindService(gpsLoggerConnection);
                    gpsLoggerConnection = null;
                }
            }catch(Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG, sw.toString());
            }
            super.onPause();
        }
    }

    private Location last_location = null;
    private MyActivity last_activity = null;
    public ArrayList<MyActivity> list = new ArrayList<>();
    ImageButton imb_wifi_off;
    ImageButton imb_wifi_on;

    static Timer timer = new Timer();
    private void showGPS() {
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
        }
        ImageButton imb_wifi_off = (ImageButton)findViewById(R.id.imbt_wifi_off);
        ImageButton imb_wifi_on = (ImageButton)findViewById(R.id.imbt_wifi_on);
        imb_wifi_on.setVisibility(View.VISIBLE);
        imb_wifi_off.setVisibility(View.GONE);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                MapsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        imb_wifi_on.setVisibility(View.GONE);
                        imb_wifi_off.setVisibility(View.VISIBLE);
                    }
                });
            }
        },1000);
    }

    private double dist=0;
    public void onLocationChanged(Location location) {
        Date d = new Date();
        if(last_location==null) {
            dist = 0;
            last_activity = new MyActivity(location.getLatitude(), location.getLongitude(),d);
            list.add(last_activity);
            last_location = location;
        }else {
            dist = CalDistance.dist(last_location.getLatitude(), last_location.getLongitude(), location.getLatitude(), location.getLongitude());
            if(dist > Config._loc_distance) {
                last_activity = new MyActivity(location.getLatitude(), location.getLongitude(),d);
                list.add(last_activity);
                last_location = location;
            }
        }

        // onPaused, don't display for battery saving
        if(!paused) {
            // new loc notify
            showGPS();
            if(googleMap != null) showActivities();
            String txt = "" + location.getLatitude() + "," + location.getLongitude() + " " +Config._loc_interval + " / " + Config._loc_distance;
            tv_log.setText("" + txt);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"-- onMapReady.");
        this.googleMap = googleMap;
        C.getInstance().setGoogleMap(_ctx, googleMap);

        // Add a marker in Sydney and move the camera
        // Original example
//        LatLng sydney = new LatLng(-34, 151);
//        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney12"));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        if (googleMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            googleMap.setMyLocationEnabled(C.LocationButton);
            googleMap.getUiSettings().setMyLocationButtonEnabled(C.LocationButton);
            googleMap.getUiSettings().setCompassEnabled(C.Compass);
            googleMap.getUiSettings().setZoomControlsEnabled(C.ZoomControl);

        }
        showActivities();

        if(list.size()==0) {
            try {
                MyActivity ma = MyLoc.getInstance(_ctx).getLastActivity();
                if(ma != null) MapUtil.drawMarker(googleMap, "Last Activity", "" + ma.cr_date + " " + ma.cr_time, ma);
            }catch(Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG, "-- " + sw.toString());
            }
        }
    }

    int cntofactivities=0;
    int marker_pos_prev=0;
    int marker_pos=0;
    public static Marker last_marker=null;
    public static Marker bef_last_marker=null;

    public void hidePopMenu(boolean hidePop) {
        if(hidePop) {
            //imbt_globe.setVisibility(View.VISIBLE);
            imbt_save.setVisibility(View.VISIBLE);
            imbt_up.setVisibility(View.VISIBLE);
            imbt_down.setVisibility(View.VISIBLE);
            imbt_marker.setVisibility(View.VISIBLE);
            imbt_hide_arrow.setVisibility(View.VISIBLE);
            imbt_navi.setVisibility(View.VISIBLE);
//            imbt_trash.setVisibility(View.VISIBLE);
            imbt_pop_menu.setVisibility(GONE);
        }else {
            imbt_pop_menu.setVisibility(View.VISIBLE);
            //imbt_globe.setVisibility(View.GONE);
            imbt_save.setVisibility(View.GONE);
            imbt_up.setVisibility(View.GONE);
            imbt_down.setVisibility(View.GONE);
            imbt_marker.setVisibility(View.GONE);
            imbt_hide_arrow.setVisibility(View.GONE);
            imbt_navi.setVisibility(View.GONE);
            //imbt_trash.setVisibility(View.GONE);
        }
    }

    static boolean hide_arrow = true;
    static boolean battery_toggle = false;

    @Override
    public void onClick(View view) {
        Log.d(TAG,"-- onClick.");
        int step = 10;
        ImageButton imbt_prev = (ImageButton) findViewById(R.id.imbt_prev);
        ImageButton imbt_next = (ImageButton) findViewById(R.id.imbt_next);
        ImageButton imbt_wifi_off = (ImageButton)findViewById(R.id.imbt_wifi_off);
        ImageButton imbt_wifi_on = (ImageButton)findViewById(R.id.imbt_wifi_on);

        switch (view.getId()) {
            case R.id.imbt_battery:
                battery_toggle = !battery_toggle;
                if(battery_toggle) {
                    C.init_preference_value_battery_default(this);
                    Toast.makeText(_ctx,"Battery saving mode ON!", Toast.LENGTH_LONG).show();
                }
                else {
                    C.restore_preference_values_after_battery(this);
                    Toast.makeText(_ctx,"Battery saving mode OFF!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.tv_activity_name:
                AlertDialogUtil.getInstance().show_today_stat(_ctx, this);
                break;
            case R.id.imbt_wifi_on:
                C.satellite = false;
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                view.setVisibility(View.GONE);
                imbt_wifi_off.setVisibility(View.VISIBLE);
                break;
            case R.id.imbt_wifi_off:
                C.satellite= true;
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                view.setVisibility(View.GONE);
                imbt_wifi_on.setVisibility(View.VISIBLE);
                break;
            case R.id.imbt_prev:
                Log.d(TAG,"-- marker_pos:" + marker_pos + " cntofactivities:" + cntofactivities );
                if(list.size() == 0) break;
                cntofactivities = list.size();
                step = cntofactivities / 10;
                if (marker_pos - step > 0) {
                    marker_pos -= step;
                }
                else break;
                LatLng ll1 = list.get(marker_pos).toLatLng();
                float myzoom = googleMap.getCameraPosition().zoom;
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll1, myzoom));
                MapUtil.drawTrack(getApplicationContext(),googleMap,list);
                showNavigate();
                break;
            case R.id.imbt_next:
                Log.d(TAG,"-- marker_pos:" + marker_pos + " cntofactivities:" + cntofactivities );
                if(list.size()==0) break;
                cntofactivities = list.size();
                step = cntofactivities / 10;
                if(marker_pos + step  < cntofactivities-1) {
                    marker_pos+= step;
                } else break;

                Log.d(TAG,"-- cntofactivities:" + cntofactivities);
                Log.d(TAG,"-- marker_pos:" + marker_pos);
                Log.d(TAG,"-- step:" + step);

                LatLng ll2 = list.get(marker_pos).toLatLng();
                float myzoom2 = googleMap.getCameraPosition().zoom;
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll2, myzoom2));
                MapUtil.drawTrack(getApplicationContext(),googleMap,list);
                showNavigate();
                break;

            case R.id.imbt_marker:
                MapUtil.toggleNoMarker();
                showActivities();
                hidePopMenu(false);
                Display display = getWindowManager().getDefaultDisplay();
                ArrayList<Marker> _markers = new ArrayList<>();

                for(int i=0;i<list.size();i++) {
                    Marker marker = googleMap.addMarker(
                            new MarkerOptions().position(list.get(i).toLatLng()).title("").visible(false));
                    _markers.add(marker);
                }
                MapUtil.DRAW(_ctx,googleMap,display,list );
                break;

            case R.id.imbt_hide_arrow:
                hide_arrow = !hide_arrow;
                if(hide_arrow) {
                    imbt_prev.setVisibility(GONE);
                    imbt_next.setVisibility(GONE);
                }else {
                    imbt_prev.setVisibility(View.VISIBLE);
                    imbt_next.setVisibility(View.VISIBLE);
                }
                hidePopMenu(false);
                break;
            case R.id.imbt_navi:
                MapUtil.toggleNoTrack();
                display = getWindowManager().getDefaultDisplay();
                _markers = new ArrayList<>();
                for(int i=0;i<list.size();i++) {
                    Marker marker = googleMap.addMarker(
                            new MarkerOptions().position(list.get(i).toLatLng()).title("").visible(false));
                    _markers.add(marker);
                }
                MapUtil.DRAW(_ctx,googleMap,display,list );
                hidePopMenu(false);
                break;
            case R.id.imbt_pop_menu:
                hidePopMenu(true);
                break;
            case R.id.imbt_Save:
                if(list.size()>0) {
                    MyActivityUtil.serialize(list, DateUtil.today());
                    String _msg = "Total " + list.size() + " activities is serialized into " + DateUtil.today();
                    Snackbar.make(view, _msg, Snackbar.LENGTH_SHORT).show();
                }
                hidePopMenu(false);
                break;
            case R.id.imbt_up:
                CloudUtil cu = new CloudUtil();
                cu.UploadAll(_ctx, Config._default_ext);
                hidePopMenu(false);
                break;
            //this is used for temporary
            case R.id.imbt_Down:
                new CloudUtil().DownloadAll(_ctx, Config._default_ext);
                NotificationUtil.notify_download_activity(_ctx);
                hidePopMenu(false);
                break;
            case R.id.imb_record_video:
                Log.d(TAG,"-- image record video.");
                recordVideo();
                break;
            case R.id.imb_start_camera:
                Log.d(TAG,"-- image button Camera.");
                takePic();
                break;
            case R.id.imb_start_list:
                Intent intent = new Intent(MapsActivity.this, FileActivity.class);
                intent.putExtra("pos", 0);
                intent.putExtra("filetype", Config._file_type_all);
                Log.d(TAG, "-- before call FileActivity");
                startActivity(intent);
                break;
            case R.id.imb_Running:
                Log.d(TAG,"-- Start Run Activity!");
                AlertDialogUtil.getInstance().choose_running_type(_ctx);
                break;

            case R.id.imGallary:
                Intent picIntent = new Intent(MapsActivity.this, Pic_Full_Screen_Activity.class);
                startActivityForResult(picIntent, Config.CALL_PIC3_ACTIVITY);
                break;

            case R.id.imVideo:
                Intent mediaIntent = new Intent(MapsActivity.this, com.jason.moment.MediaActivity.class);
                startActivity(mediaIntent);
                break;

            default:
        }
    }

    private void showNavigate() {
        if(list.size()==0) {
            Toast.makeText(_ctx, "No Activities yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng nextpos = list.get(marker_pos).toLatLng();
        LatLng prevpos = list.get(marker_pos_prev).toLatLng();
        Log.d(TAG,"-- marker_pos=" + marker_pos + ", marker_pos_prev=" + marker_pos_prev);

        CalDistance cd =  new CalDistance(prevpos, nextpos);
        double dist = cd.getDistance();
        Log.d(TAG,"-- distance between prevpos to nextpost: " + dist);

        String diststr = null;
        String elapsedstr=null;
        if(dist > 1000.0f) diststr = cd.getDistanceKmStr();
        else diststr = cd.getDistanceMStr();

        CalcTime ct = new CalcTime(list.get(marker_pos_prev), list.get(marker_pos));
        long elapsed = ct.getElapsed();
        Log.d(TAG,"-- Elapsed time between prevpos to nextpost: " + elapsed);

        if(elapsed > 60*60000) elapsedstr = ct.getElapsedHourStr();
        else if(elapsed > 60000) elapsedstr = ct.getElapsedMinStr();
        else elapsedstr = ct.getElapsedSecStr();

        float myzoom = googleMap.getCameraPosition().zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(nextpos).zoom(myzoom).build();

        float color = (marker_pos==0? BitmapDescriptorFactory.HUE_ROSE:BitmapDescriptorFactory.HUE_CYAN);
        Marker marker = googleMap.addMarker(new MarkerOptions().position(nextpos).title(AddressUtil.getAddressDong(_ctx, list.get(marker_pos)))
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(true)
                .visible(true)
                .snippet(elapsedstr + " ("+diststr+")"));

        if(bef_last_marker!=null) bef_last_marker.remove();
        if(last_marker!=null) last_marker.remove();
        last_marker = marker;
        marker_pos_prev = marker_pos;

        marker.showInfoWindow();

        MapUtil.drawTrackInRange(getApplicationContext(),googleMap,list,marker_pos_prev,marker_pos);

        String addinfo = AddressUtil.getAddress(_ctx, list.get(marker_pos));
        addinfo += " (" + (marker_pos+1) + "/" + cntofactivities +")";
        tv_log.setText(addinfo);
    }

    private void setHeadMessages() {
        TextView name = findViewById(R.id.tv_activity_name);
        TextView date_str = findViewById(R.id.tv_date_str);
        Date d = new Date();
        name.setText(DateUtil.getActivityName(d));
        date_str.setText(DateUtil.getDateString(d));
    }

    private void showActivities() {
        setHeadMessages();
        ArrayList<MyActivity> mal = list;
        MyActivity lastActivity = null;
        if(mal==null) return;
        if(mal.size()==0) return;
        ArrayList<Marker> _markers = new ArrayList<>();
        Display display = getWindowManager().getDefaultDisplay();
        MapUtil.DRAW(_ctx,googleMap,display,list );
    }

    // 사진 촬영 기능
    String currentFileName;
    private void takePic() {
        currentFileName = Config.getTmpPicName();
        File mediaFile = new File(Config.PIC_SAVE_DIR, currentFileName);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        startActivityForResult(intent, Config.PICK_FROM_CAMERA);
    }

    private void recordVideo() {
        currentFileName = Config.getTmpVideoName();
        File mediaFile = new File(Config.MOV_SAVE_DIR, currentFileName);
        Uri mediaUri = FileProvider.getUriForFile(this,
                "com.jason.moment.file_provider",
                mediaFile);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        startActivityForResult(intent, Config.PICK_FROM_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch(requestCode) {
            case Config.PICK_FROM_CAMERA:
                Log.d(TAG, "-- PIC_FROM_CAMERA: ");
                CloudUtil.getInstance().Upload(currentFileName);
//                CameraUtil.showImg(_ctx, currentFileName);
                NotificationUtil.notify_new_picture(_ctx, currentFileName);
                MyMedia.getInstance(_ctx).ins(new File(Config.PIC_SAVE_DIR, currentFileName), last_location);
                break;
            case Config.PICK_FROM_VIDEO:
                Log.d(TAG, "-- PICK_FROM_VIDEO: ");
                CloudUtil.getInstance().Upload(currentFileName);
//                CameraUtil.showVideo(_ctx, currentFileName);
                NotificationUtil.notify_new_video(_ctx, currentFileName);
                MyMedia.getInstance(_ctx).ins(new File(Config.MOV_SAVE_DIR, currentFileName), last_location);
                break;
        }
    }



    // check how to use this galleryAddPic
    private void galleryAddPic() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_update:
                Log.d(TAG,"-- Self Install!");
                Intent self_install = new Intent(MapsActivity.this, SelfInstallActivity.class);
                startActivity(self_install);
                return true;

            case R.id.action_settings:
                Log.d(TAG,"-- Setting Activities!");
                Intent configIntent = new Intent(MapsActivity.this, ConfigActivity.class);
                startActivity(configIntent);
                return true;

            case R.id.rebuild_rank:
                new StartupBatch(_ctx).rebuildActivitySummaries(_ctx);
                return true;

            case R.id.activityList:
                File dir = null;
                if(Config._default_ext == Config._csv) dir = Config.CSV_SAVE_DIR;
                else dir = Config.MNT_SAVE_DIR;
                File[] _flist = dir.listFiles();
                String[] fnamelist = new String[_flist.length];
                for(int i=0;i<_flist.length;i++) {
                    fnamelist[i] = _flist[i].getName().substring(0,_flist[i].getName().length()-4);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this )
                        .setItems(fnamelist, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(_ctx, MyReportActivity.class);
                                intent.putExtra("activity_file_name", fnamelist[i]);
                                startActivity(intent);
                            }
                        })
                        .setTitle("Choose an activity");
                AlertDialog mSportSelectDialog = builder.create();
                mSportSelectDialog.show();
                return true;

            case R.id.download_mp3:
                CloudUtil cu = new CloudUtil();
                cu.DownloadAll(_ctx, Config._mp3);
//                cu.DownloadMP3(_ctx);
                return true;

            case R.id.mp3Player:
                MP3.showPlayer(_ctx);
                return true;

            case R.id.stopMp3:
                MP3.stop(_ctx);
                return true;

            case R.id.ReportActivity:
                Log.d(TAG,"-- Report Activity!");
                Intent reportActivity = new Intent(MapsActivity.this, MyReportActivity.class);
                reportActivity.putExtra("activity_file_name", "20210522_110818");
                startActivityForResult(reportActivity, Config.CALL_REPORT_ACTIVITY);
                return true;

            case R.id.StartRunActivity:
                Log.d(TAG,"-- Start Run Activity!");
                Intent _StartActivity = new Intent(MapsActivity.this, StartRunActivity.class);
                startActivity(_StartActivity);
                return true;

//            case R.id.quote_activity:
//                Log.d(TAG,"-- Quote Activity!");
//                Intent quoteIntent = new Intent(MapsActivity.this, QuoteActivity.class);
//                quoteIntent.putExtra("1", 1);
//                startActivityForResult(quoteIntent, Config.CALL_QUOTE_ACTIVITY);
//                return true;

            case R.id.scrollpic_activity:
                Log.d(TAG,"-- Scroll Pic Activity!");
                Intent scrollPicIntent = new Intent(MapsActivity.this, Pic_Full_Screen_Activity.class);
                startActivityForResult(scrollPicIntent, Config.CALL_SCROLL_PIC_ACTIVITY);

                return true;

            case R.id.scrollAllpic_activity:
                Log.d(TAG,"-- Scroll Pic Activity!");
                Intent scrollAllPicIntent = new Intent(MapsActivity.this, ScrollAllPicActivity.class);
                startActivityForResult(scrollAllPicIntent, Config.CALL_SCROLL_ALL_PIC_ACTIVITY);
                return true;

            case R.id.pic_activity:
                Log.d(TAG,"-- Pic Activity!");
                File folder= Config.PIC_SAVE_DIR;

                File[] files = folder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith("jpeg");
                    }
                });

                if(files==null) {
                    Toast.makeText(_ctx, "No Pictures in " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    return false;
                } else if (files.length==0) {
                    Toast.makeText(_ctx, "No Pictures in " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    return false;
                }
                Intent picIntent = new Intent(MapsActivity.this, PicActivity.class);
                ArrayList<File> fileArrayList= new ArrayList<File>();
                for(int i=0;i< files.length;i++) {
                    fileArrayList.add(files[i]);
                }
                picIntent.putExtra("files", fileArrayList);
                startActivity(picIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private LocationManager mLocationManager = null;
    // return Location of current location of GPS
    public Location getLocation() {
        Log.d(TAG,"-- getLocation.");
        String locationProvider = LocationManager.GPS_PROVIDER;
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "no Permission"); // but never occur!
                return null;
            }

            // this function will return current location
            Location lastKnownLocation = mLocationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                Location location = lastKnownLocation;
                return location;
            }
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        return null;
    }

    // Common Utilities
    public static String DateToString(Date date, String format) { // eg) format = "yyyy/MM/dd HH:mm:ss"
        String dformat = format;
        if (format == null) dformat = "yyyy_MM_dd_HH_mm_ss";

        SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat(dformat);
        String date_to_string = dateformatyyyyMMdd.format(date);
        return date_to_string;
    }

}