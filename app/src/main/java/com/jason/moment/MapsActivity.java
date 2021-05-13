package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.FileUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.UI;
import com.jason.moment.util.WebUtil;
import com.jason.moment.util.db.MyLoc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

// 2021/05/03, MapsActivity extends AppCompatActivity instead of FragmentActivity
public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        LocationListener {
    private GoogleMap mMap;
    private Context _ctx;
    private LocationManager mLocManager = null;

    private static String TAG = "MapsActivity";
    private static final int DEFAULT_ZOOM = 15;
    public static boolean firstCall = true;
    public static boolean paused = false;
    public static boolean nomarker = false;
    public static boolean notrack = false;
    public static boolean satellite = false;

    public TextView tv_status;
    public ImageButton imb_snap;
    public TextView tv_map_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this._ctx = this;
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

        // mMap is null, when it created
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }

        // ----------------------------------------------------------------------
        // Configuration here
        // _start_service : run location service
        // _start_timer : my Timer
        // ----------------------------------------------------------------------
        //
        tv_status = (TextView) findViewById(R.id.tv_status);
        imb_snap = (ImageButton) findViewById(R.id.imbSnap);
        tv_map_address = (TextView) findViewById(R.id.tv_map_address);

        if (Config._start_service) {
            startService(new Intent(MapsActivity.this, LocService2.class)); // 서비스 시작
            tv_status.setText("LocService2 started...");
        }
        if (Config._start_timer) {
            startMyTimer(); // Timer 시작(onPause()에서도 10초마다 실행됨
            tv_status.setText("Timer started...");
        }
    }

    private void startMyTimer() {
        TimerTask mTask = new MapsActivity.MyTimerTask();
        Timer mTimer = new Timer();
        mTimer.schedule(mTask, Config._timer_delay, Config._timer_period);
    }


    private void initializeMap() {
        // check if map is created
        if (mMap == null) {
            //mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(); // creates the map
            // check if map is created successfully or not
            if (mMap == null) {
                Log.e(TAG,"-- Map cannot not be created. because the map is not ready!");
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
        mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config._loc_interval, Config._loc_distance, this);
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
        initializeMap();
        if(mLocManager==null) initializeLocationManager();
        paused = false;
        super.onResume();
        initializeMap();
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"-- onPause().");
        deleteLocationManager();
        if(Config._save_onPause) {
            ArrayList<MyActivity> myal = new MyLoc(getApplicationContext()).todayActivity();
            MyActivityUtil.serialize(myal, DateUtil.today()+".mnt");
            Toast.makeText(_ctx,"saved into " + DateUtil.today()+".mnt", Toast.LENGTH_LONG ).show();
        }
        paused = true;
        super.onPause();
    }

    private Location lastloc = null;
    @Override
    public void onLocationChanged(Location location) {
        double dist;
        if(lastloc==null) {
            dist = 0;
        }else {
            dist = CalDistance.dist(lastloc.getLatitude(), lastloc.getLongitude(), location.getLatitude(), location.getLongitude());
        }
        lastloc = location;

        Log.d(TAG,"-- onLocationChanged("+location.getLatitude()+","+location.getLongitude()+")");
        if(!firstCall && dist < Config._minLocChange) return;

        Log.d(TAG,"-- onLocationChanged("+dist+"m)");
        MyLoc myloc = new MyLoc(getApplicationContext());

        if(firstCall) {
            firstCall = false;
            //myloc.createNew();
            MyActivity ma = myloc.lastActivity();
            if(ma == null) {
                myloc.ins(location.getLatitude(), location.getLongitude());
                return;
            }
            double d2 = CalDistance.dist(ma.latitude, ma.longitude, location.getLatitude(), location.getLongitude());
            if (d2 > Config._minLocChange) myloc.ins(location.getLatitude(), location.getLongitude()); //minLocChange = 5meter
        }
        else {
            if (dist > Config._minLocChange) {
                myloc.ins(location.getLatitude(), location.getLongitude());
                tv_status.setText("onLocationChanged...");
//                Toast.makeText(getApplicationContext(),
//                        "-- onLocationChanged("+dist+"meter)", Toast.LENGTH_SHORT)
//                        .show();
            } else return;
        }

        String _title = DateToString(new Date(), "hh:mm:ss");
        String _snippet = getAddress(getApplicationContext(),new LatLng(location.getLatitude(), location.getLongitude()));

        mMap.clear();
        tv_map_address.setText(_snippet);
        MarkerOptions marker = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()));
        marker.title(_title);
        marker.snippet(_snippet);
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        mMap.addMarker(marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));

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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        // Original example
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney12"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        if (mMap != null) {
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
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
        //refresh();
    }

    public void refresh(){
        Log.d(TAG,"-- refresh.");
        Location loc = getLocation();
        if(loc==null) return;
        LatLng defaultLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
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


    @Override
    public void onClick(View view) {
        Log.d(TAG,"-- onClick.");
        switch (view.getId()) {
            case R.id.imGlobe:
                Log.d(TAG,"-- image button Path event.");
                MyLoc myLoc = new MyLoc(getApplicationContext());

                ArrayList<LatLng> todaypath = myLoc.todayPath();
                ArrayList<MyActivity> mActivityList = myLoc.todayActivity();
                //GooglemapUtil.drawTrack2(mMap, todaypath );

                Log.d(TAG, "-- nomarker = " + nomarker + " notracke = " + notrack);
                mMap.clear();
                if(!nomarker) drawMarkers(mMap,mActivityList);
                if(!notrack) drawTrack(mMap,mActivityList);
                if(!satellite) mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                else mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                if(nomarker || notrack) {
                    drawStartMarker(mMap,mActivityList);
                    drawEndMarker(mMap,mActivityList);
                }
                nomarker = !nomarker;
                notrack = !notrack;
                tv_status.setText("Total " + mActivityList.size() + " activities...");
                break;
            case R.id.imSave:
                ArrayList<MyActivity> myal = new MyLoc(getApplicationContext()).todayActivity();
                MyActivityUtil.serialize(myal, DateUtil.today()+".mnt");

                String _msg = "Total " + myal.size() + " activities is serialized into " + DateUtil.today()+".mnt";
                tv_status.setText(_msg);
                Snackbar.make(view, _msg, Snackbar.LENGTH_SHORT).show();

                break;
            case R.id.imFolder:
                MyActivityUtil.serialize(new MyLoc(getApplicationContext()).todayActivity(), DateUtil.today()+".mnt");
                Intent intent = new Intent(MapsActivity.this, FileActivity.class);
                intent.putExtra("pos", 0);
                intent.putExtra("filetype", Config._file_type_day);

                Log.d(TAG, "-- before call FileActivity");

                Log.d(TAG, "-- file:" + Config.getAbsolutePath(Config.get_today_filename()));
                startActivity(intent);
                break;

                //this is used for temporary
            case R.id.imDown:
                Log.d(TAG,"-- image button Down.");
                try {
                    String _url_dir = Config._backup_url_dir; //"http://ezehub.club/moment/";
                    String _files[] = Config._backup_url_files;
                    String _urls[] = new String[_files.length];
                    for(int x=0;x<_files.length;x++) {
                        _urls[x] = _url_dir + _files[x];
                    }

                    WebUtil.downloadFileAsync(_ctx, _urls, _ctx.getCacheDir().getAbsolutePath());
                    MyLoc myl = new MyLoc(_ctx);
                    ArrayList<MyActivity> todayal = new MyLoc(getApplicationContext()).todayActivity();

                    myl.deleteAll(); // Delete all the DB contents

                    for(int i=0;i<_files.length;i++) {
                        File tf = new File(_ctx.getCacheDir(), _files[i]);
                        Log.d(TAG, "-- " + _files[i] + " will be deserialzied.");
                        ArrayList<MyActivity> mal = MyActivityUtil.deserializeActivity(tf);
                        Log.d(TAG, "-- " + _files[i] + " is deserialzied into " + mal.size() + " activities.");
                        for(int j=0;j<mal.size();j++) {
                            myl.ins(mal.get(j).latitude, mal.get(j).longitude, mal.get(j).cr_date, mal.get(j).cr_time);
                        }
                        Log.d(TAG, "-- " + "Activities in" + _files[i] + " inserted into DB!");
                        tv_status.setText("Activities in" + _files[i] + " inserted into DB!");
                        //Toast.makeText(_ctx, "Activities in" + _files[i] + " inserted into DB!", Toast.LENGTH_SHORT).show();
                        MyActivityUtil.serialize(mal, _files[i]);
                        Log.d(TAG, "-- " + "Activities in" + _files[i] + " serialized!");
                        tv_status.setText("Activities in" + _files[i] + " serialized again!");
                        //Toast.makeText(_ctx, "Activities in" + _files[i] + " serialized again!", Toast.LENGTH_SHORT).show();
//
//                        String jfname = _files[i].substring(0,_files[i].length()-4) + ".jsn";
//                        MyActivityUtil.serializeIntoJason(mal,0,mal.size()-1, jfname);
//                        Log.d(TAG, "-- " + "Activities in" + jfname + " serialized into JSON file!");
//                        Toast.makeText(_ctx, "Activities in" + jfname + " serialized into JSON file", Toast.LENGTH_LONG).show();
                    }

                    Log.d(TAG, "-- " + "going to insert today activity ("+ todayal.size()+")");
                    for(int j=0;j<todayal.size();j++) {
                        myl.ins(todayal.get(j).latitude, todayal.get(j).longitude, todayal.get(j).cr_date, todayal.get(j).cr_time);
                    }
                    Log.d(TAG, "-- " + "going to serialize today activity into ("+ DateUtil.today()+".mnt");
                    MyActivityUtil.serialize(todayal, DateUtil.today()+".mnt");

                } catch (Exception e) {
                    tv_status.setText("Download Fail!" + e.toString());
                    e.printStackTrace();
                    return;
                }
                tv_status.setText("Download Success!");
                break;

            case R.id.imCamerea:
                Log.d(TAG,"-- image button Camera.");
                dispatchTakePictureIntent();
                break;
            case R.id.imbSnap:
                Log.d(TAG,"-- image button Snap.");
                Intent runIntent = new Intent(MapsActivity.this, RunActivity.class);
                runIntent.putExtra("1", 1);
                startActivityForResult(runIntent, Config.CALL_RUN_ACTIVITY);
                break;

            case R.id.imGallary:
                Log.d(TAG,"-- image button Gallery.");
                File folder= _ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                Log.d(TAG, "-- folder name to find pictures in:" + folder.getAbsolutePath());

                File files[] = FileUtil.getFilesStartsWith(folder,"IMG", true);
                if(files==null) {
                    Toast.makeText(_ctx, "No Pictures in " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    break;
                } else if (files.length==0) {
                    Toast.makeText(_ctx, "No Pictures in " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    break;
                }

                Intent picIntent = new Intent(MapsActivity.this, PicActivity.class);
                ArrayList<File> fileArrayList= new ArrayList<File>();
                for(int i=0;i< files.length;i++) {
                    fileArrayList.add(files[i]);
                }

                picIntent.putExtra("files", fileArrayList);
                Log.d(TAG, "-- before call PicActivity");
                Log.d(TAG, "-- # of file:" + fileArrayList.size());
                startActivity(picIntent);
                break;

            default:
                // doesn't work
                refresh();
        }
    }

    // 사진 촬영 기능
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                Log.e(TAG,"-- before createImageFile");
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "IMG_" + timeStamp + ".jpeg";
                photoFile = new File(_ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName);
                Toast.makeText(_ctx, "photoFile " + photoFile.getAbsolutePath() + " is used for this picture!", Toast.LENGTH_LONG).show();
                Log.d(TAG,"-- >>>>after createImageFile" + photoFile.getAbsolutePath());
            } catch (Exception ex) {
                // Error occurred while creating the File
                Log.d(TAG,"-- >>>>" +ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.jason.moment.fileprovider",
                        photoFile);

                Log.d(TAG, "-- >>>> photoURI is " + photoURI.getPath());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    Log.d(TAG, "-- >>>> resolveActivity called!");
                    startActivityForResult(takePictureIntent, Config.PICK_FROM_CAMERA);
                }

                currentPhotoPath = photoFile.getAbsolutePath();
                Log.d(TAG, "-- >>>> currentPhotoPath is " + currentPhotoPath);
                Log.d(TAG, "-- >>>> photoURI is " + photoURI.getPath());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "-- onActivityResult called!");
        if (requestCode == Config.PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            Log.d(TAG, "-- resultCode - PICK_FROM_CAMERA");
            if(data==null) {
                Log.d(TAG, "-- Intent data is NULL!!!!");
                return;
            }
            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            imageView.setImageBitmap(imageBitmap);
        }

        if(requestCode == Config.CALL_RUN_ACTIVITY && resultCode == RESULT_OK) {
            Log.d(TAG, "-- after Call RunActivity and get the return");
            if(data==null) {
                Log.d(TAG, "-- Intent data is NULL!!!!");
                return;
            } else {
                Log.d(TAG, "-- " +data);
            }
            return;
        }
    }



    // check how to use this galleryAddPic
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        Log.d(TAG,"-- >>>>contentUri to be added to Gallary " + contentUri);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MapsActivity.this, RunActivity.class);
                intent.putExtra("1", 1);
                startActivityForResult(intent, Config.CALL_RUN_ACTIVITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private Marker mMarker  = null;
    public void drawMarker(LatLng ll) {
        Log.d(TAG,"-- drawMarker.");
        String _head = DateToString(new Date(), "hh:mm:ss");
        String _body = getAddress(getApplicationContext(),ll);
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
            mMarker = mMap.addMarker(opt);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(l).zoom(15.0f).build();
        } else {
            mMarker.setPosition(l);
            mMarker.setTitle(head);
            mMarker.setSnippet(body);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 16));
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
        drawTrack2(gmap, l);
    }

    public static Polyline line_prev = null;
    public static void drawTrack(GoogleMap map, ArrayList<MyActivity> list, int start, int end) {
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

    public static String getAddress(final Context _ctx, LatLng ll) {
        Log.d(TAG,"-- getAddress.");
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ll.latitude, ll.longitude,1);
        }catch(Exception e) {
            e.printStackTrace();
        }

        String addinfo = null;
        if(addresses == null || addresses.size() ==0) {
        }else {
            addinfo = addresses.get(0).getAddressLine(0).toString();
        }
        return addinfo;
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
                    if(lastloc==null) {
                        dist = 0;
                    }else {
                        dist = CalDistance.dist(lastloc.getLatitude(), lastloc.getLongitude(), location.getLatitude(), location.getLongitude());
                    }
                    lastloc = location;

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