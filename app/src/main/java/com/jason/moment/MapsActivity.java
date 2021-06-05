package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.jason.moment.service.GPSLogger;
import com.jason.moment.service.GPSLoggerServiceConnection;
import com.jason.moment.util.AddressUtil;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CalcTime;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.FileUtil;
import com.jason.moment.util.GooglemapUtil;
import com.jason.moment.util.LocationUtil;
import com.jason.moment.util.MP3;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.NotificationUtil;
import com.jason.moment.util.StartupBatch;
import com.jason.moment.util.UI;
import com.jason.moment.util.WebUtil;
import com.jason.moment.util.camera.CameraUtil;
import com.jason.moment.util.db.MyLoc;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Integer.parseInt;

// 2021/05/03, MapsActivity extends AppCompatActivity instead of FragmentActivity
public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        LocationListener {
    private GoogleMap googleMap=null;
    private Context _ctx;
    private LocationManager mLocManager = null;
    private Intent gpsLoggerServiceIntent = null;

    private static String TAG = "MapsActivity";
    private static final int DEFAULT_ZOOM = 15;
    public static boolean firstCall = true;
    public static boolean paused = false;
//    public static boolean nomarkers = true;
//    public static boolean notrack = false;
//    public static boolean satellite = false;

    public ImageView imv_start;
    public TextView tv_map_address;
    public ImageButton imbt_prev = null;
    public ImageButton imbt_next = null;
    public TextView tv_activity_name = null;
    public TextView tv_date_str = null;
    public ImageButton imbt_pop_menu = null;
    //public ImageButton imbt_globe = null;
    public ImageButton imbt_down = null;
    public ImageButton imbt_up = null;
    public ImageButton imbt_save = null;
    public ImageButton imbt_marker = null;
//    public ImageButton imbt_trash = null;
    public ImageButton imbt_navi = null;

    public void alertQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("활동을 중지하시겠습니까?");
        builder.setMessage("활동을 정말 중지하시겠습니까?");
        builder.setPositiveButton("중지",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SerializeTodayActivity();
                        Toast.makeText(getApplicationContext(), "JASON's 활동이 저장되었습니다!" , Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this._ctx = this;
        Config.initialize(_ctx);

        StartupBatch sb = new StartupBatch(_ctx);
        sb.execute();

        // list와 mActivityList 정리 필요함.
        // list = mActivityList = MyLoc.getInstance(_ctx).todayActivity();
        list = MyLoc.getInstance(_ctx).getToodayActivities();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set permissions of resources
        if ((ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 50);
        }

        // googleMap is null, when it created
        if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
        }

        // ----------------------------------------------------------------------
        // Configuration here
        // _start_service : run location service
        // _start_timer : my Timer
        // ----------------------------------------------------------------------
        //
        imv_start = (ImageView) findViewById(R.id.imvStart);
        tv_map_address = (TextView) findViewById(R.id.tv_map_address);
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
        imbt_navi = (ImageButton) findViewById(R.id.imbt_navi);
//        imbt_trash = (ImageButton) findViewById(R.id.imbt_trash);

        if (Config._start_service) {
            //startService(new Intent(MapsActivity.this, LocService2.class)); // 서비스 시작
            gpsLoggerServiceIntent = new Intent(this, GPSLogger.class);
            gpsLoggerServiceIntent.putExtra("TID", 100);
            startService(new Intent(MapsActivity.this, GPSLogger.class)); // 서비스 시작
            gpsLoggerConnection = new GPSLoggerServiceConnection(this); // 서비스 바인딩
            bindService(gpsLoggerServiceIntent,gpsLoggerConnection, 0);
        }
        if (Config._start_timer) {
            startMyTimer(); // Timer 시작(onPause()에서도 10초마다 실행됨
        }
    }

    // GPS Logger 관련 함수 들
    // 정리 필요함
    private ServiceConnection gpsLoggerConnection = null;
    private long currentTrackId=0;
    GPSLogger gpsLogger = null;
    public long getCurrentTrackId() {
        return this.currentTrackId;
    }
    public void setGpsLogger(GPSLogger l) {
        this.gpsLogger = l;
    }
    public GPSLogger getGpsLogger() {
        return gpsLogger;
    }

    private void startMyTimer() {
        TimerTask mTask = new MapsActivity.MyTimerTask();
        Timer mTimer = new Timer();
        mTimer.schedule(mTask, Config._timer_delay, Config._timer_period);
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

    private void deleteLocationManager() {
        if(mLocManager!=null) {
            mLocManager.removeUpdates(this);
            mLocManager = null;
        }
    }

    private void initializeLocationManager() {
        mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        if(Config._enable_network_provider) mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config._loc_interval, Config._loc_distance, this);
        mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config._loc_interval, Config._loc_distance, this);

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabledGPS = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean enabledWiFi = service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!enabledGPS) {
            Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else if (!enabledWiFi) {
            Toast.makeText(this, "Network signal not found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"-- onResume.");

        // have to get all the activities from MyLoc DB;
        list = MyLoc.getInstance(_ctx).getToodayActivities();

        // Start GPS Logger service
        if(Config._start_service) {
            startService(gpsLoggerServiceIntent);
            // Bind to GPS service.
            // We can't use BIND_AUTO_CREATE here, because when we'll ubound
            // later, we want to keep the service alive in background
            bindService(gpsLoggerServiceIntent, gpsLoggerConnection, 0);
        }

        initializeMap();
        if(mLocManager==null) initializeLocationManager();
        paused = false;
        super.onResume();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        String _filetype = sharedPreferences.getString("filetype", "");

        try {
            Config._default_ext = parseInt(_filetype);
        }catch(Exception e) {
            Log.d(TAG,"-- parseInt filetype error!");
            Log.d(TAG, "--" + e);
            Config._default_ext = Config._csv;
            Log.d(TAG,"-- set default ext as csv!");
        }

        MyActivityUtil.initialize();
        initializeMap();
    }

    void SerializeTodayActivity() {
        ArrayList<MyActivity> myal = new MyLoc(getApplicationContext()).getToodayActivities();
        String activity_file_name = DateUtil.today();
        if(myal.size()>0) {
            MyActivityUtil.serialize(myal, DateUtil.today());
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"-- onBackPressed.");
        alertQuitDialog();
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"-- onPause().");
        deleteLocationManager();
        SerializeTodayActivity();
        paused = true;

        if(Config._start_service) {
            // 정리 필요
            if (gpsLogger != null) {
                if (!gpsLogger.isTracking()) {
                    Log.d(TAG, "Service is not tracking, trying to stopService()");
                    unbindService(gpsLoggerConnection);
                    stopService(gpsLoggerServiceIntent);
                } else {
                    unbindService(gpsLoggerConnection);
                }
            }
        }
        super.onPause();
    }

    private Location last_location = null;
    private MyActivity last_activity = null;
    private ArrayList<MyActivity> list = new ArrayList<>();

    private double dist=0;
    @Override
    public void onLocationChanged(Location location) {
        // insert into MyLoc 
        LocationUtil.getInstance().onLocationChanged(_ctx,location);
        Date d = new Date();
        if(last_location==null) {
            dist = 0;
            last_activity = new MyActivity(location.getLatitude(), location.getLongitude(),d);
            list.add(last_activity);
            last_location = location;
        }else {
            dist = CalDistance.dist(last_location.getLatitude(), last_location.getLongitude(), location.getLatitude(), location.getLongitude());
            if(dist > Config._minLocChange) {
                last_activity = new MyActivity(location.getLatitude(), location.getLongitude(),d);
                list.add(last_activity);
                last_location = location;
                if(googleMap != null) showActivities();
            }
        }
        showActivities();
    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"-- onMapReady.");
        this.googleMap = googleMap;

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
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            //googleMap.getUiSettings().setZoomControlsEnabled(true);
        }
        showActivities();

        MyLoc myloc = new MyLoc(_ctx);
        MyActivity a = myloc.lastActivity();
        if(a==null) googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Config._olympic_park, DEFAULT_ZOOM));
        else googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(a.toLatLng(), DEFAULT_ZOOM));
        //refresh();
    }

    public void refresh(){
        Log.d(TAG,"-- refresh.");
        Location loc = getLocation();
        if(loc==null) return;
        LatLng defaultLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        drawMarker(defaultLocation);
    }

    private LocService2 mService;
    private boolean isBind=false;

    ServiceConnection sconn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocService2.MyBinder myBinder = (LocService2.MyBinder) service;
            mService = myBinder.getService();
            isBind = true;
            Log.d(TAG, "-- onServiceConnected()");
        }

        @Override //서비스가 종료될 때 호출
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isBind = false;
            Log.d(TAG, "-- onServiceDisconnected()");
        }
    };

    //ArrayList<MyActivity> mActivityList=null;
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
            imbt_navi.setVisibility(View.VISIBLE);
//            imbt_trash.setVisibility(View.VISIBLE);
            imbt_pop_menu.setVisibility(View.GONE);
        }else {
            imbt_pop_menu.setVisibility(View.VISIBLE);
            //imbt_globe.setVisibility(View.GONE);
            imbt_save.setVisibility(View.GONE);
            imbt_up.setVisibility(View.GONE);
            imbt_down.setVisibility(View.GONE);
            imbt_marker.setVisibility(View.GONE);
            imbt_navi.setVisibility(View.GONE);
            //imbt_trash.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG,"-- onClick.");
        int step = 10;
        switch (view.getId()) {
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
                MapUtil.drawTrack(_ctx,googleMap,list);
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
                MapUtil.drawTrack(_ctx,googleMap,list);
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
                MapUtil.DRAW(_ctx,googleMap,_markers,display,list );
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
                MapUtil.DRAW(_ctx,googleMap,_markers,display,list );
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
            case R.id.imvStart:
                Log.d(TAG,"-- StartRunActivity start.");
                    Intent runIntent = new Intent(MapsActivity.this, StartRunActivity.class);
                    runIntent.putExtra("1", 1);
                    startActivityForResult(runIntent, Config.CALL_START_NEW_ACTIVITY);
                break;

            case R.id.imGallary:
                Intent picIntent = new Intent(MapsActivity.this, Pic_Full_Screen_Activity.class);
                startActivityForResult(picIntent, Config.CALL_PIC3_ACTIVITY);
                break;

            case R.id.imSetting:
                Log.d(TAG,"-- Setting Activities!");
                Intent configIntent = new Intent(MapsActivity.this, ConfigActivity.class);
                configIntent.putExtra("1", 1);
                startActivityForResult(configIntent, Config.CALL_SETTING_ACTIVITY);
                break;

            default:
                // doesn't work
                refresh();
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

        MapUtil.drawTrackInRange(_ctx,googleMap,list,marker_pos_prev,marker_pos);

        String addinfo = AddressUtil.getAddress(_ctx, list.get(marker_pos));
        addinfo += " (" + (marker_pos+1) + "/" + cntofactivities +")";
        tv_map_address.setText(addinfo);
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
        for(int i=0;i<mal.size();i++) {
            Marker marker = googleMap.addMarker(
                    new MarkerOptions().position(mal.get(i).toLatLng()).title("").visible(false));
            _markers.add(marker);
        }
        MapUtil.DRAW(_ctx,googleMap,_markers,display,list );
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case Config.PICK_FROM_CAMERA:
                Log.d(TAG, "-- PIC_FROM_CAMERA: ");
                CameraUtil.showImg(_ctx, currentFileName);
                NotificationUtil.notify_new_picture(_ctx, currentFileName);
                break;
            case Config.PICK_FROM_VIDEO:
                Log.d(TAG, "-- PICK_FROM_VIDEO: ");
                CameraUtil.showVideo(_ctx, currentFileName);
                NotificationUtil.notify_new_video(_ctx, currentFileName);
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
            case R.id.action_settings:
                Log.d(TAG,"-- Setting Activities!");
                Intent configIntent = new Intent(MapsActivity.this, ConfigActivity.class);
                configIntent.putExtra("1", 1);
                startActivityForResult(configIntent, Config.CALL_SETTING_ACTIVITY);
                return true;

            case R.id.activityList:
                File dir = null;
                if(Config._default_ext == Config._csv) dir = Config.CSV_SAVE_DIR;
                else dir = Config.MNT_SAVE_DIR;
                File _flist[] = dir.listFiles();
                String fnamelist[] = new String[_flist.length];
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
                cu.DownloadMP3(_ctx);
                return true;

            case R.id.mp3Player:
                MP3.showPlayer(_ctx);
                return true;

            case R.id.stopMp3:
                MP3.stop(_ctx);
                return true;

            case R.id.run_activity:
                Log.d(TAG,"-- Run Activity!");
                Intent runIntent = new Intent(MapsActivity.this, RunActivity.class);
                runIntent.putExtra("1", 1);
                startActivityForResult(runIntent, Config.CALL_RUN_ACTIVITY);
                return true;

            case R.id.file_activity2:
                Log.d(TAG,"-- FileActivity2!");
                Intent fileactivity2 = new Intent(MapsActivity.this, FileActivity2.class);
                fileactivity2.putExtra("1", 1);
                startActivityForResult(fileactivity2, Config.CALL_FILE_ACTIVITY);
                return true;

            case R.id.ReportActivity:
                Log.d(TAG,"-- Report Activity!");
                Intent reportActivity = new Intent(MapsActivity.this, MyReportActivity.class);
                reportActivity.putExtra("activity_file_name", "20210522_110818");
                startActivityForResult(reportActivity, Config.CALL_REPORT_ACTIVITY);
                return true;

            case R.id.StartActivity:
                Log.d(TAG,"-- Start Activity(old)!");
                Intent startNewActivity = new Intent(MapsActivity.this, StartActivity.class);
                startActivityForResult(startNewActivity, Config.CALL_START_ACTIVITY);
                return true;

            case R.id.quote_activity:
                Log.d(TAG,"-- Quote Activity!");
                Intent quoteIntent = new Intent(MapsActivity.this, QuoteActivity.class);
                quoteIntent.putExtra("1", 1);
                startActivityForResult(quoteIntent, Config.CALL_QUOTE_ACTIVITY);
                return true;

            case R.id.scrollpic_activity:
                Log.d(TAG,"-- Scroll Pic Activity!");
                Intent scrollPicIntent = new Intent(MapsActivity.this, ScrollPicActivity.class);
                startActivityForResult(scrollPicIntent, Config.CALL_SCROLL_PIC_ACTIVITY);

                return true;

            case R.id.scrollAllpic_activity:
                Log.d(TAG,"-- Scroll Pic Activity!");
                Intent scrollAllPicIntent = new Intent(MapsActivity.this, ScrollAllPicActivity.class);
                startActivityForResult(scrollAllPicIntent, Config.CALL_SCROLL_ALL_PIC_ACTIVITY);

                return true;

            case R.id.media_activity:
                Log.d(TAG,"-- Media Activity!");
                File folder= Config.PIC_SAVE_DIR;
                File[] files1 = folder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(Config._pic_ext);
                    }
                });

                folder = Config.MOV_SAVE_DIR;
                File[] files2 = folder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(Config._mov_ext);
                    }
                });

                File[] files = null;
                if(files1==null && files2 != null) files = files2;
                else if(files1!=null && files2 == null) files = files1;
                else if(files1==null && files2==null) files=null;
                else files = new File[files1.length + files2.length];

                int j=0;
                if(files1 != null) for(int i=0;i<files1.length;i++) files[j++] = files1[i];
                if(files2 != null) for(int i=0;i<files1.length;i++) files[j++] = files2[i];

                if(files==null) {
                    Toast.makeText(_ctx, "No Pictures in " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    return false;
                } else if (files.length==0) {
                    Toast.makeText(_ctx, "No Pictures in " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    return false;
                }
                Intent mediaIntent = new Intent(MapsActivity.this, MediaActivity.class);
                ArrayList<File> fileArrayList= new ArrayList<File>();
                for(int i=0;i< files.length;i++) {
                    fileArrayList.add(files[i]);
                }
                mediaIntent.putExtra("files", fileArrayList);
                startActivity(mediaIntent);
                return true;

        case R.id.pic_activity:
        Log.d(TAG,"-- Pic Activity!");
        folder= Config.PIC_SAVE_DIR;

        files = folder.listFiles(new FilenameFilter() {
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
        fileArrayList= new ArrayList<File>();
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

    private Marker mMarker  = null;
    public void drawMarker(LatLng ll) {
        Log.d(TAG,"-- drawMarker.");
        String _head = DateToString(new Date(), "hh:mm:ss");
        String _body = AddressUtil.getAddress(getApplicationContext(),ll);
        drawMarker(ll,_head,_body);
        mMarker.showInfoWindow();
    }

    public void drawMarker(LatLng l, String head, String body) {
        if (mMarker == null) {
            MarkerOptions opt = new MarkerOptions()
                    .position(l)
                    .title(head)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .draggable(true).visible(true).snippet(body);
            mMarker = googleMap.addMarker(opt);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(l).zoom(15.0f).build();
        } else {
            mMarker.setPosition(l);
            mMarker.setTitle(head);
            mMarker.setSnippet(body);
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, googleMap.getCameraPosition().zoom));
    }


    public static void drawTrack2(GoogleMap gmap, ArrayList<LatLng> list) {
        if(list == null) return;
        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.RED);
        Polyline line = gmap.addPolyline(plo);
        line.setWidth(20);
        line.setPoints(list);
    }

    public static void drawTrack(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list == null) return;
        ArrayList<LatLng> l = new ArrayList<>();
        for(int i=0; i<list.size();i++) {
            l.add(new LatLng(list.get(i).latitude, list.get(i).longitude));
        }

        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.RED);
        Polyline line = gmap.addPolyline(plo);
        line.setWidth(20);
        line.setPoints(l);
    }

    public static Polyline line_prev = null;
    public static void drawTrackInRange(GoogleMap map, ArrayList<MyActivity> list, int start, int end) {
        if(list == null) return;
        ArrayList<LatLng> l = new ArrayList<>();
        for(int i=start; i < end; i++) {
            l.add(new LatLng(list.get(i).latitude, list.get(i).longitude));
        }

        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.BLACK);
        Polyline line = map.addPolyline(plo);
        line.setWidth(20);
        line.setPoints(l);

        if(line_prev!=null) line_prev.remove();
        line_prev = line;
    }



    public static ArrayList<Marker> markers = null;
    public static void drawStartMarker(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(markers==null) markers = new ArrayList<Marker>();
        if(list.size()==0) return;
        LatLng ll = new LatLng(list.get(0).latitude, list.get(0).longitude);
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("START")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .draggable(true)
                .visible(true)
                .snippet("START"));
        markers.add(marker);
    }

    public static void drawEndMarker(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list.size()==0) return;
        LatLng ll = new LatLng(list.get(list.size()-1).latitude, list.get(list.size()-1).longitude);
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("종료")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(true)
                .visible(true)
                .snippet("FINISH"));
        markers.add(marker);
    }

    // return total distance in meters
    public static double getTotalDistanceDouble(ArrayList<MyActivity> list) {
        if(list == null) return 0;
        if(list.size() ==2) return 0;
        double dist_meter = 0;
        for(int i=0; i<list.size()-1; i++) {
            double bef_lat = list.get(i).latitude;
            double bef_lon = list.get(i).longitude;
            double aft_lat = list.get(i+1).latitude;
            double aft_lon = list.get(i+1).longitude;

            CalDistance cd = new CalDistance(bef_lat, bef_lon, aft_lat, aft_lon);
            double dist_2 = cd.getDistance();
            if(Double.isNaN(dist_2)) {
                Log.d(TAG, "--Double.NaN between ("+bef_lat + ","+ bef_lon +") ~ ("+ aft_lat + ","+ aft_lon + ")" ) ;
                continue;
            } else if ( Double.isNaN(dist_meter + dist_2)) {
                Log.d(TAG, "--Double.NaN between ("+bef_lat + ","+ bef_lon +") ~ ("+ aft_lat + ","+ aft_lon + ")" ) ;
                continue;
            }
            dist_meter = dist_meter + dist_2;
        }
        return dist_meter;
    }

    public static void drawMarkers(GoogleMap gmap, ArrayList<MyActivity> list) {
        double tot_distance = getTotalDistanceDouble(list);

        int disunit = 1000;
        String unitstr = "미터";
        if (tot_distance > 1000) {  // 1km 이상
            disunit = 1000;
            unitstr = "킬로";
        } else disunit = 100;

        double t_distance = 0;
        double t_lap = disunit;
        for(int i=0; i < list.size(); i++) {
            LatLng ll = new LatLng(list.get(i).latitude, list.get(i).longitude);
            float color = (i==0) ?  BitmapDescriptorFactory.HUE_GREEN : ((i==list.size()-1)? BitmapDescriptorFactory.HUE_RED  :  BitmapDescriptorFactory.HUE_CYAN);

            String title = list.get(i).cr_date + " " + list.get(i).cr_time;
            if(i==0) drawStartMarker(gmap,list);
            else if(i==list.size()-1) drawEndMarker(gmap,list);
            else {
                CalDistance cd = new CalDistance(list.get(i-1).latitude, list.get(i-1).longitude, list.get(i).latitude, list.get(i).longitude);
                double dist = cd.getDistance();
                if(Double.isNaN(dist)) continue;
                if(Double.isNaN(dist + t_distance)) continue;

                t_distance = t_distance + dist;
                if(t_distance > t_lap) {
                    int interval = (int)(t_distance / disunit);
                    t_lap += disunit;

                    Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                            .draggable(true)
                            .visible(true)
                            .snippet(""+interval + unitstr));
                    markers.add(marker);
                }
            }
        }
    }

    // LocationManager variable declaration
    private LocationManager mLocationManager = null;
    // return Location of current location of GPS
    public Location getLocation() {
        Log.d(TAG,"-- getLocation.");
        String locationProvider =  mLocationManager.GPS_PROVIDER;
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
            e.printStackTrace();
            Log.e(TAG, e.toString());
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

    // MyTimerTask can run even though the app run in background
    public class MyTimerTask extends java.util.TimerTask{
        public void run() {
            long start = System.currentTimeMillis();
            MapsActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d(TAG,"-- MyTimerTask!");
                    if(!paused) {
                        Log.d(TAG,"-- MapApp does not paused!");
                        return;
                    }

                    Location location = getLocation();
                    if(location==null) {
                        Log.d(TAG,"-- cannot get Location!");
                        Toast.makeText(getApplicationContext(),
                                "-- -- cannot get Location!", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    double dist;
                    if(last_location==null) {
                        dist = 0;
                    }else {
                        dist = CalDistance.dist(last_location.getLatitude(), last_location.getLongitude(), location.getLatitude(), location.getLongitude());
                    }
                    last_location = location;

                    if(dist > Config._minLocChange) { // 5meter
                        MyLoc myloc = new MyLoc(getApplicationContext());
                        myloc.ins(location.getLatitude(), location.getLongitude());
                        Log.d(TAG,"-- new Location"+location.getLatitude()+","+location.getLongitude()+")");
                        Toast.makeText(getApplicationContext(),
                                "-- Timer add new Location", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Log.d(TAG,"-- same Location by MyTimerTask!");
                    }
                }
            });
        } /* end of run() */
    } /* end of MyTimerTask */


}