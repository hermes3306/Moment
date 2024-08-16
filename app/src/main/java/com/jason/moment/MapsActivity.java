package com.jason.moment;

// Android core imports
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

// AndroidX imports
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

// Google Maps imports
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

// Google Material Design imports
import com.google.android.material.snackbar.Snackbar;

// Application-specific imports
import com.jason.moment.activity.Run4;
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

// Java utility imports
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

// Static imports
import static android.view.View.GONE;
import static java.lang.Integer.parseInt;


public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener {

    private static final String TAG = "MapsActivity";
    private static final int DEFAULT_ZOOM = 15;

    private GoogleMap googleMap = null;
    private Context _ctx;
    private Intent gpsLoggerServiceIntent = null;
    private ServiceConnection gpsLoggerConnection = null;
    private BroadcastReceiver receiver = null;

    public static boolean firstCall = true;
    public static boolean paused = false;
    static boolean already_quit = false;

    private TextView tv_log;
    private ImageButton image_button_pop_menu = null;
    private ImageButton image_button_down = null;
    private ImageButton image_button_hide_arrow = null;
    private ImageButton image_button_up = null;
    private ImageButton image_button_save = null;
    private ImageButton image_button_marker = null;
    private ImageButton image_button_navi = null;

    private String currentTrackId;
    private GPSLogger gpsLogger = null;
    private ArrayList<MyActivity> list = new ArrayList<>();
    private Location last_location = null;

    static boolean hide_arrow = true;
    static boolean battery_toggle = false;
    static Timer timer = new Timer();

    private TextView tv_activity_name;
    private TextView tv_date_str; // Add this line to declare tv_date_str

    int count_of_activities = 0;
    int marker_pos_prev = 0;
    int marker_pos = 0;
    public static Marker last_marker = null;
    public static Marker bef_last_marker = null;

    String currentFileName;

    // Add this new public method
    public int getListSize() {
        return list != null ? list.size() : 0;
    }

    public String getFirstActivityTime() {
        if (list != null && !list.isEmpty()) {
            return list.get(0).cr_time;
        }
        return "";
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PermissionUtil.getInstance().setPermission(this);
        this._ctx = this;
        Config.initialize(getApplicationContext());

        StartupBatch sb = new StartupBatch(_ctx);
        sb.execute();

        registerLocationChangedReceiver();

        currentTrackId = DateUtil.today();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        MyLoc.getInstance(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        initializeViews();

        startGPSLoggerService();

        list = MyLoc.getInstance(_ctx).getToodayActivities();
        Toast.makeText(_ctx, "# of Today's activities are " + list.size(), Toast.LENGTH_LONG).show();

        AlertDialogUtil.getInstance().checkActiveRunning(_ctx);
    }

    private void initializeViews() {
        ImageButton imb_Running = findViewById(R.id.imb_Running);
        tv_log = findViewById(R.id.tv_log);
        ImageButton image_button_prev = findViewById(R.id.image_button_prev);
        ImageButton image_button_next = findViewById(R.id.image_button_next);
        tv_date_str = findViewById(R.id.tv_date_str);
        tv_activity_name = findViewById(R.id.tv_activity_name);
        image_button_pop_menu = findViewById(R.id.image_button_pop_menu);
        image_button_save = findViewById(R.id.image_button_Save);
        image_button_up = findViewById(R.id.image_button_up);
        image_button_down = findViewById(R.id.image_button_Down);
        image_button_marker = findViewById(R.id.image_button_marker);
        image_button_hide_arrow = findViewById(R.id.image_button_hide_arrow);
        image_button_navi = findViewById(R.id.image_button_navi);

        imb_Running.setOnClickListener(this);
        image_button_prev.setOnClickListener(this);
        image_button_next.setOnClickListener(this);
        image_button_pop_menu.setOnClickListener(this);
        image_button_save.setOnClickListener(this);
        image_button_up.setOnClickListener(this);
        image_button_down.setOnClickListener(this);
        image_button_marker.setOnClickListener(this);
        image_button_hide_arrow.setOnClickListener(this);
        image_button_navi.setOnClickListener(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "-- onMapReady.");
        this.googleMap = googleMap;
        C.getInstance().setGoogleMap(_ctx, googleMap);

        if (googleMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(C.LocationButton);
            googleMap.getUiSettings().setMyLocationButtonEnabled(C.LocationButton);
            googleMap.getUiSettings().setCompassEnabled(C.Compass);
            googleMap.getUiSettings().setZoomControlsEnabled(C.ZoomControl);
        }
        showActivities();

        if (list.isEmpty()) {
            try {
                MyActivity ma = MyLoc.getInstance(_ctx).getLastActivity();
                if (ma != null) {
                    assert googleMap != null;
                    MapUtil.drawMarker(googleMap, "Last Activity", ma.cr_date + " " + ma.cr_time, ma);
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG, "-- " + sw.toString());
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "-- onDestroy()");
        if (gpsLoggerConnection != null) unbindService(gpsLoggerConnection);
        if (gpsLoggerServiceIntent != null) stopService(gpsLoggerServiceIntent);
        if (receiver != null) unregisterReceiver(receiver);
        Log.e(TAG, "-- after onDestroy()");
        super.onDestroy();
    }

    public String getCurrentTrackId() {
        return currentTrackId;
    }

    private void startGPSLoggerService() {
        gpsLoggerServiceIntent = new Intent(this, GPSLogger.class);
        String activity_file_name = DateUtil.today();
        gpsLoggerServiceIntent.putExtra("activity_file_name", activity_file_name);
        startForegroundService(gpsLoggerServiceIntent);
        gpsLoggerConnection = new GPSLoggerServiceConnection(this);
        bindService(gpsLoggerServiceIntent, gpsLoggerConnection, 0);

        if (this.getGpsLogger() != null) this.getGpsLogger().set_use_broadcast(true);
    }

    // Add the missing getGpsLogger() method
    public GPSLogger getGpsLogger() {
        return gpsLogger;
    }

    // Add a method to set the GPSLogger
    public void setGpsLogger(GPSLogger logger) {
        this.gpsLogger = logger;
    }


    private void registerLocationChangedReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (paused) return;

                Log.d(TAG, "-- Received intent " + intent.getAction());
                if (Config.INTENT_LOCATION_CHANGED.equals(intent.getAction())) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        Log.d(TAG, "-- got broad casting message of INTENT_LOCATION_CHANGED ");
                        Location location = (Location) extras.get("location");
                        Log.d(TAG, "-- Broad casting Location received:" + location);
                        onLocationChanged(location);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.INTENT_LOCATION_CHANGED);
        registerReceiver(receiver, filter);
        Log.d(TAG, "-- INTENT LOCATION CHANGED registerReceiver()");
    }

    public void onLocationChanged(Location location) {
        Date d = new Date();
        MyActivity last_activity = null;
        double dist = 0;
        if (last_location == null) {
            dist = 0;
            last_activity = new MyActivity(location.getLatitude(), location.getLongitude(), d);
            list.add(last_activity);
            last_location = location;
        } else {
            dist = CalDistance.dist(last_location.getLatitude(), last_location.getLongitude(), location.getLatitude(), location.getLongitude());
            if (dist > Config._loc_distance) {
                last_activity = new MyActivity(location.getLatitude(), location.getLongitude(), d);
                list.add(last_activity);
                last_location = location;
            }
        }

        // onPaused, don't display for battery saving
        if (!paused) {
            // new loc notify
            showGPS();
            if (googleMap != null) showActivities();
            String txt = location.getLatitude() + "," + location.getLongitude() + " " + Config._loc_interval + " / " + Config._loc_distance;
            tv_log.setText(txt);
        }
    }

    private void showGPS() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
        }
        ImageButton imb_wifi_off = findViewById(R.id.image_button_wifi_off);
        ImageButton imb_wifi_on = findViewById(R.id.image_button_wifi_on);
        imb_wifi_on.setVisibility(View.VISIBLE);
        imb_wifi_off.setVisibility(View.GONE);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MapsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        imb_wifi_on.setVisibility(View.GONE);
                        imb_wifi_off.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 1000);
    }

    private void hidePopMenu(boolean show) {
        if (show) {
            image_button_save.setVisibility(View.VISIBLE);
            image_button_up.setVisibility(View.VISIBLE);
            image_button_down.setVisibility(View.VISIBLE);
            image_button_marker.setVisibility(View.VISIBLE);
            image_button_hide_arrow.setVisibility(View.VISIBLE);
            image_button_navi.setVisibility(View.VISIBLE);
            image_button_pop_menu.setVisibility(View.GONE);
        } else {
            image_button_pop_menu.setVisibility(View.VISIBLE);
            image_button_save.setVisibility(View.GONE);
            image_button_up.setVisibility(View.GONE);
            image_button_down.setVisibility(View.GONE);
            image_button_marker.setVisibility(View.GONE);
            image_button_hide_arrow.setVisibility(View.GONE);
            image_button_navi.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "-- onClick: " + view.getId());
        int step = 10;
        ImageButton image_button_prev = findViewById(R.id.image_button_prev);
        ImageButton image_button_next = findViewById(R.id.image_button_next);
        ImageButton image_button_wifi_off = findViewById(R.id.image_button_wifi_off);
        ImageButton image_button_wifi_on = findViewById(R.id.image_button_wifi_on);

        switch (view.getId()) {
            case R.id.image_button_battery:
                battery_toggle = !battery_toggle;
                if (battery_toggle) {
                    C.init_preference_value_battery_default(this);
                    Toast.makeText(_ctx, "Battery saving mode ON!", Toast.LENGTH_LONG).show();
                } else {
                    C.restore_preference_values_after_battery(this);
                    Toast.makeText(_ctx, "Battery saving mode OFF!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.tv_activity_name:
                AlertDialogUtil.getInstance().show_today_stat(_ctx, this);
                break;
            case R.id.image_button_wifi_on:
                C.satellite = false;
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                view.setVisibility(View.GONE);
                image_button_wifi_off.setVisibility(View.VISIBLE);
                break;
            case R.id.image_button_wifi_off:
                C.satellite = true;
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                view.setVisibility(View.GONE);
                image_button_wifi_on.setVisibility(View.VISIBLE);
                break;
            case R.id.image_button_prev:
                handlePrevButton();
                break;
            case R.id.image_button_next:
                handleNextButton();
                break;
            case R.id.image_button_marker:
                handleMarkerButton();
                break;
            case R.id.image_button_hide_arrow:
                handleHideArrowButton(image_button_prev, image_button_next);
                break;
            case R.id.image_button_navi:
                handleNaviButton();
                break;
            case R.id.image_button_pop_menu:
                hidePopMenu(true);
                break;
            case R.id.image_button_Save:
                handleSaveButton(view);
                break;
            case R.id.image_button_up:
                handleUploadButton();
                break;
            case R.id.image_button_Down:
                handleDownloadButton();
                break;
            case R.id.imb_record_video:
                recordVideo();
                break;
            case R.id.imb_start_camera:
                takePic();
                break;
            case R.id.imb_start_list:
                startFileActivity();
                break;
            case R.id.imb_Running:
                Log.d(TAG, "-- Start Run Activity!");
                startRun4Activity();
                break;
            case R.id.imGallary:
                startPicFullScreenActivity();
                break;
            case R.id.imVideo:
                startMediaActivity();
                break;
            default:
                Log.d(TAG, "-- Unhandled click event for view: " + view.getId());
        }
    }


    private void handleNextButton() {
        // Similar implementation to handlePrevButton, but for next
    }

    private void handleMarkerButton() {
        MapUtil.toggleNoMarker();
        showActivities();
        hidePopMenu(false);
        Display display = getWindowManager().getDefaultDisplay();
        ArrayList<Marker> _markers = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Marker marker = googleMap.addMarker(
                    new MarkerOptions().position(list.get(i).toLatLng()).title("").visible(false));
            _markers.add(marker);
        }
        MapUtil.DRAW(_ctx, googleMap, display, list);
    }

    private void handleHideArrowButton(ImageButton image_button_prev, ImageButton image_button_next) {
        hide_arrow = !hide_arrow;
        if (hide_arrow) {
            image_button_prev.setVisibility(GONE);
            image_button_next.setVisibility(GONE);
        } else {
            image_button_prev.setVisibility(View.VISIBLE);
            image_button_next.setVisibility(View.VISIBLE);
        }
        hidePopMenu(false);
    }

    private void handleNaviButton() {
        MapUtil.toggleNoTrack();
        Display display = getWindowManager().getDefaultDisplay();
        ArrayList<Marker> _markers = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Marker marker = googleMap.addMarker(
                    new MarkerOptions().position(list.get(i).toLatLng()).title("").visible(false));
            _markers.add(marker);
        }
        MapUtil.DRAW(_ctx, googleMap, display, list);
        hidePopMenu(false);
    }

    private void handleSaveButton(View view) {
        if (list.size() > 0) {
            MyActivityUtil.serialize(list, DateUtil.today());
            String _msg = "Total " + list.size() + " activities is serialized into " + DateUtil.today();
            Snackbar.make(view, _msg, Snackbar.LENGTH_SHORT).show();
        }
        hidePopMenu(false);
    }

    private void handleUploadButton() {
        CloudUtil cu = new CloudUtil();
        cu.UploadAll(_ctx, Config._default_ext);
        hidePopMenu(false);
    }

    private void handleDownloadButton() {
        new CloudUtil().DownloadAll(_ctx, Config._default_ext);
        NotificationUtil.notify_download_activity(_ctx);
        hidePopMenu(false);
    }

    private void startFileActivity() {
        Intent intent = new Intent(MapsActivity.this, FileActivity.class);
        intent.putExtra("pos", 0);
        intent.putExtra("filetype", Config._file_type_all);
        Log.d(TAG, "-- before call FileActivity");
        startActivity(intent);
    }

    private void startPicFullScreenActivity() {
        Intent picIntent = new Intent(MapsActivity.this, Pic_Full_Screen_Activity.class);
        startActivityForResult(picIntent, Config.CALL_PIC3_ACTIVITY);
    }

    private void startMediaActivity() {
        Intent mediaIntent = new Intent(MapsActivity.this, MediaActivity.class);
        startActivity(mediaIntent);
    }


    private void handlePrevButton() {
        Log.d(TAG, "-- marker_pos:" + marker_pos + " cntofactivities:" + count_of_activities);
        if (list.size() == 0) return;
        count_of_activities = list.size();
        int step = count_of_activities / 10;
        if (marker_pos - step > 0) {
            marker_pos -= step;
        } else return;
        LatLng ll1 = list.get(marker_pos).toLatLng();
        float myzoom = googleMap.getCameraPosition().zoom;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll1, myzoom));
        MapUtil.drawTrack(getApplicationContext(), googleMap, list);
        showNavigate();
    }



    private void startRun4Activity() {
        Intent runIntent = new Intent(MapsActivity.this, Run4.class);
        startActivity(runIntent);
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
        addinfo += " (" + (marker_pos+1) + "/" + count_of_activities +")";
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