package com.jason.moment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.jason.moment.util.ActivityStat;
import com.jason.moment.util.ActivitySummary;
import com.jason.moment.util.AlertDialogUtil;
import com.jason.moment.util.C;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.PermissionUtil;
import com.jason.moment.util.Progress;
import com.jason.moment.util.db.MyActiviySummary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class MyReportActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener {
    String TAG = "MyReportActivity";
    String activity_filename = null;

    ArrayList<MyActivity> mActivityList = null;
    ArrayList<String> media_list = null;
    ActivityStat activityStat = null;

    Context _ctx = null;
    private GoogleMap _googleMap;
    boolean satellite = false;
    boolean hide_arrow = true;


    private void showActivities() {
        // media_list checkup
        media_list = MyActivityUtil.deserializeMediaInfoFromCSV(activity_filename);
        if (media_list == null) {
            media_list = null;
        } else if(media_list.size()==0) {
            media_list = null;
        }else {
            for (int i = 0; i < media_list.size(); i++) Log.d(TAG, "-- MEDIA " + media_list.get(i));
        }

        mActivityList = MyActivityUtil.deserialize(activity_filename);
        MyActivity lastActivity = null;
        if (mActivityList == null) return;
        if (mActivityList.size() == 0) {
            Toast.makeText(_ctx, "No activities!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            lastActivity = mActivityList.get(mActivityList.size() - 1);
        }

        final TextView tv_activity_name = (TextView)findViewById(R.id.name);
        final TextView tv_date_str = (TextView)findViewById(R.id.date_str);
        final TextView memo = findViewById(R.id.memo);
        final TextView weather = findViewById(R.id.weather);
        final TextView tv_co_runner = findViewById(R.id.tv_co_runner);
        final TextView tv_file_name = findViewById(R.id.tv_file_name);

        final ImageButton imbt_prev = (ImageButton) findViewById(R.id.imbt_prev);
        final ImageButton imbt_next = (ImageButton) findViewById(R.id.imbt_next);
        final TextView tv_distance = (TextView) findViewById(R.id.tv_distance);
        final TextView tv_duration = (TextView) findViewById(R.id.tv_duration);
        final TextView tv_minperkm = (TextView) findViewById(R.id.tv_minperkm);
        final TextView tv_calories = (TextView) findViewById(R.id.tv_carolies);
        final TextView tv_rank = (TextView) findViewById(R.id.tv_rank);
        final TextView tv_rank_range = (TextView) findViewById(R.id.tv_rank_range);
        final TextView tv_white_km = (TextView) findViewById(R.id.tv_white_km);
        final TextView tv_white_avg = (TextView) findViewById(R.id.tv_white_avg);
        final TextView tv_white_duration = (TextView) findViewById(R.id.tv_white_duration);
        final ImageButton imbt_hide_arrow = (ImageButton) findViewById(R.id.imbt_hide_arrow);
        final ImageButton imbt_marker = (ImageButton) findViewById(R.id.imbt_marker);
        final ImageButton imbt_navi = (ImageButton) findViewById(R.id.imbt_navi);
        final ImageButton imbt_trash = (ImageButton) findViewById(R.id.imbt_trash);
        final ImageButton imbt_pop_menu = (ImageButton) findViewById(R.id.imbt_pop_menu);
        final ImageButton imbt_up = (ImageButton) findViewById(R.id.imbt_up);
        final TextView tv_medias = (TextView) findViewById(R.id.medias);

        if(media_list!=null) {
            tv_medias.setText("" + media_list.size() + "의 사진/동영상이 있습니다.");
        }else {
            tv_medias.setText("사진/동영상이 없습니다.");
        }

        final MapView mMapView = findViewById(R.id.mapView);
        activityStat = MyActivityUtil.getActivityStat(mActivityList);
        if (activityStat != null) {
            tv_activity_name.setText(activityStat.name);
            tv_date_str.setText(activityStat.date_str);
            tv_distance.setText("" + String.format("%.1f", activityStat.distanceKm));
            tv_duration.setText(activityStat.duration);
            tv_calories.setText("" + activityStat.calories);
            tv_minperkm.setText("" + String.format("%.1f", activityStat.minperKm));
            memo.setText(activityStat.memo);
            weather.setText(activityStat.weather);
            tv_co_runner.setText(MyActivityUtil.getRunnerInfo(activity_filename));
            tv_file_name.setText(activity_filename);

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

        imbt_marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imbt_marker.setVisibility(View.GONE);
                imbt_navi.setVisibility(View.GONE);
                imbt_trash.setVisibility(View.GONE);
                imbt_up.setVisibility(View.GONE);
                imbt_hide_arrow.setVisibility(View.GONE);
                imbt_pop_menu.setVisibility(View.VISIBLE);
                C.nomarkers = !C.nomarkers;
                int width = mMapView.getWidth();
                int height = mMapView.getHeight();
                MapUtil.DRAW(_ctx,_googleMap,width,height,mActivityList);
            }
        });

        imbt_hide_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imbt_marker.setVisibility(View.GONE);
                imbt_navi.setVisibility(View.GONE);
                imbt_trash.setVisibility(View.GONE);
                imbt_up.setVisibility(View.GONE);
                imbt_hide_arrow.setVisibility(View.GONE);
                imbt_pop_menu.setVisibility(View.VISIBLE);
                hide_arrow = !hide_arrow;
                if(hide_arrow) {
                    imbt_next.setVisibility(View.GONE);
                    imbt_prev.setVisibility(View.GONE);
                }else{
                    imbt_next.setVisibility(View.VISIBLE);
                    imbt_prev.setVisibility(View.VISIBLE);
                    imbt_prev.setVisibility(View.VISIBLE);
                }
            }
        });

        imbt_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imbt_marker.setVisibility(View.GONE);
                imbt_navi.setVisibility(View.GONE);
                imbt_trash.setVisibility(View.GONE);
                imbt_up.setVisibility(View.GONE);
                imbt_hide_arrow.setVisibility(View.GONE);
                imbt_pop_menu.setVisibility(View.VISIBLE);
                C.notrack = !C.notrack;
                int width = mMapView.getWidth();
                int height = mMapView.getHeight();
                MapUtil.DRAW(_ctx,_googleMap,width,height,mActivityList);
            }
        });

        imbt_pop_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imbt_marker.setVisibility(View.VISIBLE);
                imbt_navi.setVisibility(View.VISIBLE);
                imbt_trash.setVisibility(View.GONE);
                imbt_up.setVisibility(View.VISIBLE);
                imbt_pop_menu.setVisibility(View.VISIBLE);
                imbt_hide_arrow.setVisibility(View.VISIBLE);
            }
        });

        imbt_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imbt_marker.setVisibility(View.GONE);
                imbt_navi.setVisibility(View.GONE);
                imbt_trash.setVisibility(View.GONE);
                imbt_up.setVisibility(View.GONE);
                imbt_hide_arrow.setVisibility(View.GONE);
                imbt_pop_menu.setVisibility(View.VISIBLE);
                CloudUtil.getInstance().Upload(activity_filename);
            }
        });
        MapUtil.DRAW(_ctx, _googleMap, mMapView.getWidth(), mMapView.getHeight(), mActivityList);
    }

    protected void initialize_views(Bundle savedInstanceState) {
        setContentView(R.layout.activity_file);
        MapView mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        // mMap is null, when it created
        if (_googleMap != null) {
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

            //C.setGoogleMap(googleMap);
            _googleMap.setMyLocationEnabled(C.LocationButton);
            _googleMap.getUiSettings().setMyLocationButtonEnabled(C.LocationButton);
            _googleMap.getUiSettings().setCompassEnabled(C.Compass);
            _googleMap.getUiSettings().setZoomControlsEnabled(C.ZoomControl);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PermissionUtil.getInstance().setPermission(this);
        _ctx = this;
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        activity_filename = intent.getExtras().getString("activity_file_name");
        initialize_views(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        ImageButton imbt_prev = (ImageButton) findViewById(R.id.imbt_prev);
        ImageButton imbt_next = (ImageButton) findViewById(R.id.imbt_next);
        ImageButton imbt_satellite_off = (ImageButton)findViewById(R.id.imbt_satellite_off);
        ImageButton imbt_satellite_on = (ImageButton)findViewById(R.id.imbt_satellite_on);
        MapView mapView = (MapView)findViewById(R.id.mapView);
        LinearLayout ll_stat01 = (LinearLayout) findViewById(R.id.ll_stat01);
        LinearLayout ll_stat02 = (LinearLayout) findViewById(R.id.ll_stat02);
        LinearLayout ll_dashboard01 = (LinearLayout) findViewById(R.id.ll_dashboard01);
        LinearLayout ll_dashboard02 = (LinearLayout) findViewById(R.id.ll_dashboard02);

        switch(v.getId()) {
            case R.id.tv_rank:
            case R.id.tv_rank_range:
                Log.e(TAG, "-- " + activity_filename);
                ArrayList <MyActivity> mal = MyActivityUtil.deserialize(activity_filename);
                ActivityStat as = ActivityStat.getActivityStat(mal);
                double distanceKm = as.distanceKm;
                AlertDialogUtil.getInstance().chooseRank(_ctx, distanceKm);
                break;
            case R.id.tv_activity_progress:
                Log.e(TAG, "-- " + activity_filename);
                mal = MyActivityUtil.deserialize(activity_filename);
                ArrayList<Progress> plist = MyActivityUtil.getProgress(mal);
                for(int i=0;i<plist.size();i++) {
                    Log.d(TAG, "-- " + plist.get(i));
                }
                AlertDialogUtil.getInstance().showProgress(_ctx, plist);
                break;
            case R.id.media_information:
            case R.id.medias:
                AlertDialogUtil.getInstance().showMedias(_ctx,media_list,0);
                break;
            case R.id.tv_file_information:
            case R.id.tv_file_name:
                Intent detailMaps = new Intent(MyReportActivity.this, DetailMapsActivity.class);
                detailMaps.putExtra("activity_filename", activity_filename);
                startActivity(detailMaps);
                break;
            case R.id.imbt_satellite_on:
                C.satellite = false;
                _googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                v.setVisibility(View.GONE);
                imbt_satellite_off.setVisibility(View.VISIBLE);
                imbt_satellite_off.setVisibility(View.VISIBLE);
                ll_stat01.setVisibility(View.VISIBLE);
                ll_stat02.setVisibility(View.VISIBLE);
                ll_dashboard01.setVisibility(View.GONE);
                ll_dashboard02.setVisibility(View.GONE);
                break;
            case R.id.imbt_satellite_off:
                C.satellite= true;
                _googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                v.setVisibility(View.GONE);
                imbt_satellite_on.setVisibility(View.VISIBLE);
                ll_stat01.setVisibility(View.GONE);
                ll_stat02.setVisibility(View.GONE);
                ll_dashboard01.setVisibility(View.VISIBLE);
                ll_dashboard02.setVisibility(View.VISIBLE);
                break;

            case R.id.imbt_next:
                ArrayList<ActivitySummary> asl =
                        MyActiviySummary.getInstance(_ctx).all_activitySummary_by_ranks(activityStat.minperKm, activityStat.distanceKm);
                int rank = MyActiviySummary.getInstance(_ctx).rank(activityStat.minperKm, activityStat.distanceKm) -1;
                Intent intent1 = new Intent(_ctx, MyReportActivity.class);
                String t_file_name;
                if(rank+1 < asl.size()) t_file_name = asl.get(rank+1).name;
                else t_file_name = asl.get(0).name;
                intent1.putExtra("activity_file_name", t_file_name);
                Log.d(TAG, "-- activity_file_name: " + t_file_name);
                _ctx.startActivity(intent1);
                finish();
                break;
            case R.id.imbt_prev:
                asl = MyActiviySummary.getInstance(_ctx).all_activitySummary_by_ranks(activityStat.minperKm, activityStat.distanceKm);
                rank = MyActiviySummary.getInstance(_ctx).rank(activityStat.minperKm, activityStat.distanceKm)-1;
                Intent intent2 = new Intent(_ctx, MyReportActivity.class);

                if(rank > 0) t_file_name = asl.get(rank-1).name;
                else t_file_name = asl.get(asl.size()-1).name;
                intent2.putExtra("activity_file_name", t_file_name);
                Log.d(TAG, "-- activity_file_name: " + t_file_name);
                _ctx.startActivity(intent2);
                finish();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this._googleMap = googleMap;
        C.getInstance().setGoogleMap(_ctx, googleMap);
        showActivities();
    }
}