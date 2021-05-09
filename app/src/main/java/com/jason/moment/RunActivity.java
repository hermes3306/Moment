package com.jason.moment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jason.moment.util.AddressUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.StringUtil;
import com.jason.moment.util.db.MyLoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class RunActivity extends AppCompatActivity {
    String TAG = "RunActivity";
    Context _ctx = null;
    GoogleMap _googleMap=null;
    public LocationManager mLocManager = null;
    boolean quit=false;
    boolean paused;



    private class GPSListener implements LocationListener {
        public GPSListener(String gpsProvider) {
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            if(_googleMap==null) {
                Log.d(TAG,"-- onLocationChanged! but, _googleMap is null ");
                return;
            }
            Log.d(TAG,"-- onLocationChanged! [RunActivity] " + location.getLatitude() + "," + location.getLongitude());
            double l1 = location.getLatitude();
            double l2 = location.getLongitude();

            _googleMap.clear();
            TextView tv_map_address = (TextView)findViewById(R.id.tv_run_activity_address);
            String _snippet = AddressUtil.getAddress(getApplicationContext(),new LatLng(location.getLatitude(), location.getLongitude()));
            String _title = StringUtil.DateToString(new Date(), "hh:mm:ss");
            tv_map_address.setText(_snippet);
            MarkerOptions marker = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()));
            marker.title(_title);
            marker.snippet(_snippet);
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            _googleMap.addMarker(marker);
            _googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }
    }

    RunActivity.GPSListener[] mLocationListeners = new RunActivity.GPSListener[] {
            new RunActivity.GPSListener(LocationManager.GPS_PROVIDER),
            new RunActivity.GPSListener(LocationManager.NETWORK_PROVIDER)
    };

    private void deleteLocationManager() {
        if(mLocManager!=null) {
            mLocManager.removeUpdates(mLocationListeners[0]);
            mLocManager.removeUpdates(mLocationListeners[1]);
            mLocManager = null;
        }
    }

    private void initializeLocationManager() {
        if (mLocManager == null) {
            mLocManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            mLocManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    Config._loc_interval,
                    Config._loc_distance,
                    mLocationListeners[0]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        try {
            mLocManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    Config._loc_interval,
                    Config._loc_distance,
                    mLocationListeners[1]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this._ctx = this;
        initializeLocationManager();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        final MapView mMapView = (MapView) findViewById(R.id.mapView);
        MapsInitializer.initialize(this);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                getLastActivity(googleMap);
            }
            public void getLastActivity(GoogleMap googleMap) {
                _googleMap = googleMap;
                MyLoc myl = new MyLoc(_ctx);
                MyActivity a = myl.lastActivity();
//                _googleMap.setMyLocationEnabled(true);
                _googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                _googleMap.getUiSettings().setCompassEnabled(true);
                _googleMap.getUiSettings().setZoomControlsEnabled(true);

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(a.latitude, a.longitude), 16));
//                googleMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(a.latitude, a.longitude))
//                        .title("OnMapReady() callback"));
            }
        });

        // *** START button
        Button bt_start = (Button)findViewById(R.id.bt_start);
        bt_start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Intent startIntent = new Intent(RunActivity.this, StartActivity.class);
                startIntent.putExtra("1", 1);
                startActivityForResult(startIntent, Config.CALL_START_ACTIVITY);
            }
        });

        // *** Quit button
        Button bt_quit = (Button)findViewById(R.id.bt_quit);
        bt_quit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Log.d(TAG,"-- Activity will be finished!");
                alertQuitDialog();
            }
        });

        //** Setting button
        Button bt_run_setting = (Button)findViewById(R.id.bt_run_setting);
        bt_run_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"-- Setting Activities!");
                Intent runIntent = new Intent(RunActivity.this, ConfigActivity.class);
                runIntent.putExtra("1", 1);
                startActivityForResult(runIntent, Config.CALL_SETTING_ACTIVITY);
            }
        });
    }



    public void alertQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("활동을 중지하시겠습니까?");
        builder.setMessage("활동을 정말 중지하시겠습니까?");
        builder.setPositiveButton("중지",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RunActivity.this.quit = true;
                        RunActivity.this.finish();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RunActivity.this.quit = false;
                    }
                });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"-- onBackPressed.");
        alertQuitDialog();
    }


    @Override
    protected void onResume() {
        Log.d(TAG,"-- onResume.");
        paused = false;
        if(mLocManager==null) initializeLocationManager();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"-- onPause().");
        paused = true;
        deleteLocationManager();
        super.onPause();
    }





}