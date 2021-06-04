package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jason.moment.util.ActivityStat;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.db.MyLoc;

import java.util.ArrayList;

public class MyReportActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener {
    String TAG = "MyReportActivity";
    String activity_filename = null;
    Context _ctx = null;
    private GoogleMap googleMap;
    boolean satellite=false;

    private void showActivities() {
        MyLoc myLoc = new MyLoc(getApplicationContext());
        //ArrayList<MyActivity> mal = myLoc.todayActivity();

        ArrayList<String> media_list = MyActivityUtil.deserializeMediaInfoFromCSV(activity_filename);
        Log.e(TAG,"-- showActivities");

        if(media_list!=null) {
            for(int i=0;i<media_list.size();i++) Log.d(TAG,"-- " + media_list.get(i));
        }
        ArrayList<MyActivity> mActivityList = MyActivityUtil.deserialize(activity_filename);
        MyActivity lastActivity = null;
        if(mActivityList==null) return;
        if(mActivityList.size()==0) {
            Toast.makeText(_ctx,"No activities!", Toast.LENGTH_SHORT).show();
        } else {
            lastActivity = mActivityList.get(mActivityList.size()-1);
        }

        TextView name = findViewById(R.id.name);
        TextView date_str = findViewById(R.id.date_str);
        TextView distancekm = findViewById(R.id.distancekm);
        TextView duration = findViewById(R.id.duration);
        TextView calories = findViewById(R.id.calories);
        TextView minperkm = findViewById(R.id.minperkm);
        TextView memo = findViewById(R.id.memo);
        TextView weather = findViewById(R.id.weather);
        TextView co_runner = findViewById(R.id.co_runner);

        ActivityStat activityStat = MyActivityUtil.getActivityStat(mActivityList);
        if(activityStat!=null) {
            name.setText(activityStat.name);
            date_str.setText(activityStat.date_str);
            distancekm.setText("" + String.format("%.1f", activityStat.distanceKm));
            duration.setText(activityStat.duration);
            calories.setText("" + activityStat.calories);
            minperkm.setText("" + String.format("%.1f", activityStat.minperKm));
            memo.setText(activityStat.memo);
            weather.setText(activityStat.weather);
            co_runner.setText(activityStat.co_runner);
        }
        ArrayList<Marker> _markers = new ArrayList<>();
        Display display = getWindowManager().getDefaultDisplay();
        for(int i=0;i<mActivityList.size();i++) {
            Marker marker = googleMap.addMarker(
                    new MarkerOptions().position(mActivityList.get(i).toLatLng()).title("").visible(false));
            _markers.add(marker);
        }
        MapUtil.DRAW(_ctx,googleMap,_markers,display,mActivityList);
    }

    protected void initialize_views(Bundle savedInstanceState) {
        setContentView(R.layout.activity_my_report);
        MapView mapView = findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        // mMap is null, when it created
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
            googleMap.getUiSettings().setZoomControlsEnabled(true);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _ctx = this;
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        activity_filename = intent.getExtras().getString("activity_file_name");
        Toast.makeText(_ctx, "Activity filename: " + activity_filename, Toast.LENGTH_LONG).show();
        initialize_views(savedInstanceState);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        showActivities();
    }
}