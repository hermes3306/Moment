package com.jason.moment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.db.MyLoc;

public class RunActivity extends AppCompatActivity implements LocationListener {
    String TAG = "RunActivity";
    Context _ctx = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this._ctx = this;
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
                MyLoc myl = new MyLoc(_ctx);
                MyActivity a = myl.lastActivity();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(a.latitude, a.longitude), 16));
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(a.latitude, a.longitude))
                        .title("Marker in Sydney"));
            }
        });

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "["+TAG+"]" + "--onLocationChanged()" );
    }

}