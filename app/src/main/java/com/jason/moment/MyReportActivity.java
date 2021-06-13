package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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
import com.jason.moment.util.ActivitySummary;
import com.jason.moment.util.C;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.db.MyActiviySummary;
import com.jason.moment.util.db.MyLoc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class MyReportActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener {
    String TAG = "MyReportActivity";
    String activity_filename = null;
    Context _ctx = null;
    private GoogleMap googleMap;
    boolean satellite = false;

    private void showActivities2() {
        ArrayList<String> media_list = MyActivityUtil.deserializeMediaInfoFromCSV(activity_filename);
        if (media_list != null) {
            for (int i = 0; i < media_list.size(); i++) Log.d(TAG, "-- " + media_list.get(i));
        }
        ArrayList<MyActivity> mActivityList = MyActivityUtil.deserialize(activity_filename);
        MyActivity lastActivity = null;
        if (mActivityList == null) return;
        if (mActivityList.size() == 0) {
            Toast.makeText(_ctx, "No activities!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            lastActivity = mActivityList.get(mActivityList.size() - 1);
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
        TextView tv_rank = findViewById(R.id.tv_rank);

        ActivityStat activityStat = MyActivityUtil.getActivityStat(mActivityList);
        if (activityStat != null) {
            name.setText(activityStat.name);
            date_str.setText(activityStat.date_str);
            distancekm.setText("" + String.format("%.1f", activityStat.distanceKm));
            duration.setText(activityStat.duration);
            calories.setText("" + activityStat.calories);
            minperkm.setText("" + String.format("%.1f", activityStat.minperKm));
            memo.setText(activityStat.memo);
            weather.setText(activityStat.weather);
            co_runner.setText(activityStat.co_runner);
            int rank = MyActiviySummary.getInstance(_ctx).rank(activityStat.minperKm);
            tv_rank.setText("" + rank + "번째로 빠릅니다.");
        }
        MapView mv = findViewById(R.id.mapView);
        MapUtil.DRAW(_ctx, googleMap, mv.getWidth(), mv.getHeight(), mActivityList);

    }

    private void showActivities() {
        ArrayList<String> media_list = MyActivityUtil.deserializeMediaInfoFromCSV(activity_filename);
        if (media_list != null) {
            for (int i = 0; i < media_list.size(); i++) Log.d(TAG, "-- " + media_list.get(i));
        }
        ArrayList<MyActivity> mActivityList = MyActivityUtil.deserialize(activity_filename);
        MyActivity lastActivity = null;
        if (mActivityList == null) return;
        if (mActivityList.size() == 0) {
            Toast.makeText(_ctx, "No activities!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            lastActivity = mActivityList.get(mActivityList.size() - 1);
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
        TextView tv_rank = findViewById(R.id.tv_rank);
        TextView tv_rank_range = (TextView) findViewById(R.id.tv_rank_range);

        ActivityStat activityStat = MyActivityUtil.getActivityStat(mActivityList);
        if (activityStat != null) {
            name.setText(activityStat.name);
            date_str.setText(activityStat.date_str);
            distancekm.setText("" + String.format("%.1f", activityStat.distanceKm));
            duration.setText(activityStat.duration);
            calories.setText("" + activityStat.calories);
            minperkm.setText("" + String.format("%.1f", activityStat.minperKm));
            memo.setText(activityStat.memo);
            weather.setText(activityStat.weather);
            co_runner.setText(activityStat.co_runner);
            try {
                //int rank = MyActiviySummary.getInstance(_ctx).rank(activityStat.minperKm);
                int rank = MyActiviySummary.getInstance(_ctx).rank(activityStat.minperKm, activityStat.distanceKm);
                tv_rank.setText("" + rank + "번째로 빠릅니다.");
                String range[] = MyActiviySummary.getInstance(_ctx).getStringRange_by_dist(activityStat.distanceKm);
                tv_rank_range.setText(range[0] + "-" + range[1] + "KM 운동을 비교해 보세요.");
            }catch(Exception e) {
                tv_rank.setText("-" + "번째로 빠릅니다.");
                tv_rank_range.setText("전체 운동을 비교해 보세요.");
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG, sw.toString());
            }
        }
        MapView mv = findViewById(R.id.mapView);
        MapUtil.DRAW(_ctx, googleMap, mv.getWidth(), mv.getHeight(), mActivityList);
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
            googleMap.setMyLocationEnabled(C.LocationButton);
            googleMap.getUiSettings().setMyLocationButtonEnabled(C.LocationButton);
            googleMap.getUiSettings().setCompassEnabled(C.Compass);
            googleMap.getUiSettings().setZoomControlsEnabled(C.ZoomControl);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _ctx = this;
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        activity_filename = intent.getExtras().getString("activity_file_name");
        initialize_views(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.tv_rank:
            case R.id.tv_rank_range:
                Log.e(TAG, "-- " + activity_filename);
                ArrayList <MyActivity> mal = MyActivityUtil.deserialize(activity_filename);
                ActivityStat as = ActivityStat.getActivityStat(mal);
                double distanceKm = as.distanceKm;
                ArrayList <ActivitySummary> asl = MyActiviySummary.getInstance(_ctx).query_rank_speed_by_dist(distanceKm);
                for(int i=0;i<asl.size();i++) Log.d("TAG", "-- " + i + ":" + asl.get(i).toString());


                AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
                LayoutInflater factory = LayoutInflater.from(this);
                final View view = factory.inflate(R.layout.layout_scroll_linearlayout, null);
                LinearLayout ll = view.findViewById(R.id.linearLayout);

                final TextView[] tvs = new TextView[asl.size()];
                for(int i=0;i<asl.size();i++) {
                    final TextView tv = new TextView(this);
                    tv.setText(asl.get(i).toString());
                    ll.addView(tv);
                    tvs[i] = tv;
                }

                alertadd.setView(view);
                alertadd.setNeutralButton("??!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int sumthin) {
                    }
                });
                alertadd.show();
                break;
            case R.id.tv_activity_progress:
                Log.e(TAG, "-- " + activity_filename);
                break;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
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
//        googleMap.setMyLocationEnabled(C.LocationButton);
        googleMap.getUiSettings().setMyLocationButtonEnabled(C.LocationButton);
        googleMap.getUiSettings().setCompassEnabled(C.Compass);
        googleMap.getUiSettings().setZoomControlsEnabled(C.ZoomControl);
        showActivities();
    }
}