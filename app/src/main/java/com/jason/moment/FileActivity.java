package com.jason.moment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Printer;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.jason.moment.util.ActivityStat;
import com.jason.moment.util.ActivitySummary;
import com.jason.moment.util.AddressUtil;
import com.jason.moment.util.AlertDialogUtil;
import com.jason.moment.util.C;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CalcTime;
import com.jason.moment.util.CaloryUtil;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.MP3;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.Progress;
import com.jason.moment.util.StringUtil;
import com.jason.moment.util.db.MyActiviySummary;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileActivity extends AppCompatActivity implements View.OnClickListener{
    public static String TAG = "FileActivity";
    Context _ctx=null;
    GoogleMap _googleMap;
    ArrayList<String> media_list = null;
    public String activity_filename = null;

    public static int position = 0;
    public static int filetype = -1;

    public static ArrayList<Marker> markers = null;
    public static ArrayList<MyActivity> mActivityList = new ArrayList<MyActivity>();
    public static boolean tog_add = true;

    public static int marker_pos = 0;
    public static int marker_pos_prev =0;
    public static Polyline line_prev = null;
    public float myzoom = 16f;
    public static Marker last_marker=null;
    public static Marker bef_last_marker=null;

    public static final int REQUEST_ACTIVITY_FILE_LIST = 0x0001;
    File[] _file_list = null;
    File _file = null;
    MyActivity lastActivity = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _ctx = this;
        setContentView(R.layout.activity_file);

        Intent intent = getIntent();
        position = intent.getExtras().getInt("pos");
        filetype = intent.getExtras().getInt("filetype");
        _file_list = MyActivityUtil.getFiles(filetype);

        if(_file_list == null) {
            Toast.makeText(getApplicationContext(),"No files found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else if(_file_list.length==0) {
            Toast.makeText(getApplicationContext(), "No files found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        _file = _file_list[0];

        final Context _ctx = this;
        final MapView mMapView = (MapView) findViewById(R.id.mapView);
        MapsInitializer.initialize(this);
        mMapView.onCreate(savedInstanceState);  // check required ....
        mMapView.onResume();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            final TextView tv_activity_name = (TextView)findViewById(R.id.name);
            final TextView tv_date_str = (TextView)findViewById(R.id.date_str);

            final TextView memo = findViewById(R.id.memo);
            final TextView weather = findViewById(R.id.weather);
            final TextView co_runner = findViewById(R.id.co_runner);
            final ImageButton imbt_prev = (ImageButton) findViewById(R.id.imbt_prev);
            final ImageButton imbt_next = (ImageButton) findViewById(R.id.imbt_next);
            final TextView tv_distance = (TextView) findViewById(R.id.tv_distance);
            final TextView tv_duration = (TextView) findViewById(R.id.tv_duration);
            final TextView tv_minperkm = (TextView) findViewById(R.id.tv_minperkm);
            final TextView tv_carolies = (TextView) findViewById(R.id.tv_carolies);
            final TextView tv_rank = (TextView) findViewById(R.id.tv_rank);
            final TextView tv_rank_range = (TextView) findViewById(R.id.tv_rank_range);
            final TextView tv_medias = (TextView) findViewById(R.id.medias);

            final TextView tv_white_km = (TextView) findViewById(R.id.tv_white_km);
            final TextView tv_white_avg = (TextView) findViewById(R.id.tv_white_avg);
            final TextView tv_white_duration = (TextView) findViewById(R.id.tv_white_duration);

            final ImageButton imbt_marker = (ImageButton) findViewById(R.id.imbt_marker);
            final ImageButton imbt_navi = (ImageButton) findViewById(R.id.imbt_navi);
            final ImageButton imbt_trash = (ImageButton) findViewById(R.id.imbt_trash);
            final ImageButton imbt_pop_menu = (ImageButton) findViewById(R.id.imbt_pop_menu);
            final ImageButton imbt_hide_arrow = (ImageButton) findViewById(R.id.imbt_hide_arrow);
            final ImageButton imbt_up = (ImageButton) findViewById(R.id.imbt_up);
            final File[] flist = MyActivityUtil.getFiles(filetype);

            public void GO(final GoogleMap googleMap, File myfile) {
                googleMap.clear();
                ActivityStat activityStat = null;
                if(myfile != null) {
                    mActivityList = MyActivityUtil.deserialize(myfile);
                    _file = myfile;
                    activity_filename = myfile.getName();

                    // media_list checkup
                    media_list = MyActivityUtil.deserializeMediaInfoFromCSV(activity_filename);
                    if (media_list == null) {
                        media_list = null;
                    } else if(media_list.size()==0) {
                        media_list = null;
                    }else {
                        for (int i = 0; i < media_list.size(); i++) Log.d(TAG, "-- MEDIA " + media_list.get(i));
                    }
                }

                if(mActivityList==null) {
                    Log.e(TAG, "-- " + myfile + " failed to be deserialized");
                    return;
                } else if(mActivityList.size()==0) {
                    Log.e(TAG, "-- " + myfile + " serialized successfully but the size is 0");
                    return;
                } else {
                        Log.d(TAG, "-- " + myfile + " is deserialized successfully! with # of " + mActivityList.size());
                }

                if(mActivityList.size()>1) {
                        marker_pos = mActivityList.size()-1;
                }

                MyActivity ta = mActivityList.get(0);
                String date_str = ta.cr_date + " " + ta.cr_time;
                activityStat= ActivityStat.getActivityStat(mActivityList);

                if(activityStat !=null) {
                    String _minDist = String.format("%.1f", activityStat.distanceKm);
                    String sinfo = "" + date_str;

                    tv_activity_name.setText(activityStat.name);
                    tv_date_str.setText(activityStat.date_str);

                    tv_distance.setText(_minDist);
                    tv_duration.setText(activityStat.durationM);
                    tv_minperkm.setText(activityStat.minperKms);
                    tv_carolies.setText("   " + activityStat.calories);

                    tv_white_km.setText(_minDist);
                    tv_white_avg.setText(activityStat.minperKms);
                    tv_white_duration.setText(activityStat.durationM);
                    if(media_list!=null) {
                        tv_medias.setText("" + media_list.size() + "의 사진/동영상이 있습니다.");
                    }else {
                        tv_medias.setText("사진/동영상이 없습니다.");
                    }

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
                } else {
                    myfile.delete();
                    if(position+1 < flist.length) {
                        GO(googleMap, flist[++position]);
                        return;
                    }
                }

                int width = mMapView.getWidth();
                int height = mMapView.getHeight();
                MapUtil.DRAW(_ctx,googleMap,width,height,mActivityList);
            }

            public void alertDeleteDialog(File file) {
                AlertDialog.Builder builder = new AlertDialog.Builder(_ctx);
                builder.setTitle("파일을 삭제하시겠습니까?");
                builder.setMessage("파일을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                file.delete();
                                File[] flist = MyActivityUtil.getFiles(filetype);
                                if(flist==null) {
                                    Toast.makeText(getApplicationContext(),"No more files!",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (position >= 0 && position < flist.length-1) {}
                                else position=0;
                                GO(_googleMap, flist[position]);
                            }
                        });
                builder.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.show();
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                _googleMap = googleMap;

//                googleMap.setMyLocationEnabled(C.LocationButton);
                googleMap.getUiSettings().setMyLocationButtonEnabled(C.LocationButton);
                googleMap.getUiSettings().setCompassEnabled(C.Compass);
                googleMap.getUiSettings().setZoomControlsEnabled(C.ZoomControl);

                GO(googleMap, _file);

                imbt_prev.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {

                        File[] flist = MyActivityUtil.getFiles(filetype);
                        if(flist==null) {
                            Toast.makeText(getApplicationContext(),"No more files!",Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (position > 0 && position <= flist.length) position--;
                        else position=flist.length-1;
                        GO(googleMap, flist[position]);
                    }
                });

                imbt_next.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        File[] flist = MyActivityUtil.getFiles(filetype);
                        if(flist==null) {
                            Toast.makeText(getApplicationContext(),"No more files!",Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (position >= 0 && position < flist.length-1) position++;
                        else position=0;
                        GO(googleMap, flist[position]);
                    }
                });

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
                        MapUtil.DRAW(_ctx,googleMap,width,height,mActivityList);
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
                        MapUtil.DRAW(_ctx,googleMap,width,height,mActivityList);
                    }
                });

                imbt_pop_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.VISIBLE);
                        imbt_navi.setVisibility(View.VISIBLE);
                        imbt_trash.setVisibility(View.VISIBLE);
                        imbt_up.setVisibility(View.VISIBLE);
                        imbt_hide_arrow.setVisibility(View.VISIBLE);
                        imbt_pop_menu.setVisibility(View.GONE);
                    }
                });

                imbt_trash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.GONE);
                        imbt_navi.setVisibility(View.GONE);
                        imbt_trash.setVisibility(View.GONE);
                        imbt_up.setVisibility(View.GONE);
                        imbt_hide_arrow.setVisibility(View.GONE);
                        imbt_pop_menu.setVisibility(View.VISIBLE);

                        File[] flist = MyActivityUtil.getFiles(filetype);
                        try {
                            alertDeleteDialog(flist[position]);
                            flist = MyActivityUtil.getFiles(filetype);
                            if (flist.length > 1) position=position;
                            else position=0;
                        }catch(Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            Log.e(TAG,"Err:" + sw.toString());
                        }
                        GO(googleMap, flist[position]);
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
                        CloudUtil.getInstance().Upload(_file_list[position].getName());
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
            } /* on  MapReady */

        });
    } /* onCreate */

    static boolean hide_arrow = false;
    public static Date getStartTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(list.get(0));
        return date;
    }

    public static Date getEndTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(list.get(list.size()-1));
        return date;
    }

    static int mapViewHeight;
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

        switch (v.getId()) {
            case R.id.tv_rank:
            case R.id.tv_rank_range:
                Log.e(TAG, "-- " + _file_list[position]);
                ArrayList <MyActivity> mal = MyActivityUtil.deserialize(_file_list[position]);
                ActivityStat as = ActivityStat.getActivityStat(mal);
                double distanceKm = as.distanceKm;
                AlertDialogUtil.getInstance().chooseRank(_ctx, distanceKm);
                break;

            case R.id.tv_activity_progress:
                Log.e(TAG, "-- " + _file_list[position]);
                mal = MyActivityUtil.deserialize(_file_list[position]);
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
            case R.id.imSetting:
                Log.d(TAG, "-- Setting Activities!");
                Intent configIntent = new Intent(FileActivity.this, ConfigActivity.class);
                configIntent.putExtra("1", 1);
                startActivityForResult(configIntent, Config.CALL_SETTING_ACTIVITY);
                break;
            default:
                break;
        }
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
            case R.id.mp3Player:
                MP3.showPlayer(_ctx);
                return true;
            case R.id.stopMp3:
                MP3.stop(_ctx);
                return true;
            case R.id.action_settings:
                Log.d(TAG,"-- Setting Activities!");
                Intent configIntent = new Intent(this, ConfigActivity.class);
                configIntent.putExtra("1", 1);
                startActivityForResult(configIntent, Config.CALL_SETTING_ACTIVITY);
                return true;

            case R.id.run_activity:
                Log.d(TAG,"-- Run Activity!");
                Intent runIntent = new Intent(this, RunActivity.class);
                runIntent.putExtra("1", 1);
                startActivityForResult(runIntent, Config.CALL_RUN_ACTIVITY);
                return true;

            case R.id.ReportActivity:
                Log.d(TAG,"-- Report Activity!");
                Intent reportActivity = new Intent(this, MyReportActivity.class);
                reportActivity.putExtra("activity_file_name", "20210522_110818");
                startActivityForResult(reportActivity, Config.CALL_REPORT_ACTIVITY);
                return true;

//            case R.id.quote_activity:
//                Log.d(TAG,"-- Quote Activity!");
//                Intent quoteIntent = new Intent(this, QuoteActivity.class);
//                quoteIntent.putExtra("1", 1);
//                startActivityForResult(quoteIntent, Config.CALL_QUOTE_ACTIVITY);
//                return true;

            case R.id.scrollpic_activity:
                Log.d(TAG,"-- Scroll Pic Activity!");
                Intent scrollPicIntent = new Intent(this, ScrollPicActivity.class);
                startActivityForResult(scrollPicIntent, Config.CALL_SCROLL_PIC_ACTIVITY);
                return true;

//            case R.id.scrollAllpic_activity:
//                Log.d(TAG,"-- Scroll Pic Activity!");
//                Intent scrollAllPicIntent = new Intent(this, ScrollAllPicActivity.class);
//                startActivityForResult(scrollAllPicIntent, Config.CALL_SCROLL_ALL_PIC_ACTIVITY);\
//                return true;

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
                Intent picIntent = new Intent(this, PicActivity.class);
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



}
