package com.jason.moment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        View.OnClickListener {

    private GoogleMap mMap;
    private String TAG = "MapsActivity";
    private static final int DEFAULT_ZOOM = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        Toast.makeText(getApplicationContext(), "onCreate()", Toast.LENGTH_LONG).show();
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        // Original example
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney12"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        // +/- Zoom Controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Toolbar for navigation and map
        mMap.getUiSettings().setMapToolbarEnabled(true);
        // Need to check the below .setCompassEnabled is working well
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        Toast.makeText(getApplicationContext(), "onMapReady()", Toast.LENGTH_LONG).show();
        refresh();

    }

    public void refresh(){
        Location loc = getLocation();
        LatLng defaultLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        drawMarker(defaultLocation);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case 1:
                break;
            default:
                // doesn't work
                Toast.makeText(getApplicationContext(), "onClick()", Toast.LENGTH_LONG).show();
                refresh();

        }
    }


    private Marker mMarker                 = null;
    public void drawMarker(LatLng ll) {
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
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            mMarker.setPosition(l);
            mMarker.setTitle(head);
            mMarker.setSnippet(body);
        }
    }


    // LocationManager variable declaration
    private LocationManager mLocationManager = null;
    // return Location of current location of GPS
    public Location getLocation() {
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

    /*
    OnStart()
        1) on start
        2) background to foreground
     */
    @Override
    protected void onStart() {
        super.onStart();
        //Intent myI = new Intent(this, MyLocationService.class);
        //bindService(myI, conn, Context.BIND_AUTO_CREATE);
        //doMyTimeTask();
        //int size = MyLocationService.getSize();
        //String str = " - total: ";
        //if(size == -1) str += " -1(null)";
        //else str += " " + size + " locations";
        //str = "SERVICE STARTED" + str;
        Toast.makeText(getApplicationContext(),"onStart()", Toast.LENGTH_LONG).show();
    }

    /*
    OnStop()
        1) on stop
        2) foreground to background
     */

    @Override
    protected void onStop() {
        Toast.makeText(getApplicationContext(),"onStop()", Toast.LENGTH_LONG).show();
        super.onStop();
    }

    /*
    OnDestroy()
    1) on destroy
    */
    @Override
    protected void onDestroy() {
        // if super.onDestory() commented out, it doesn't work well
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "ERR: Moment is about to destroy !", Toast.LENGTH_LONG).show();
    }


}