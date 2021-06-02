package com.jason.moment;

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
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
import com.jason.moment.util.AddressUtil;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CalcTime;
import com.jason.moment.util.CaloryUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.MP3;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.StringUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileActivity extends AppCompatActivity implements View.OnClickListener{
    public static String TAG = "FileActivity";
    Context _ctx=null;
    GoogleMap _googleMap;

    public static int position = 0;
    public static int filetype = -1;

    public static ArrayList<Marker> markers = null;
    public static ArrayList<MyActivity> mActivityList = new ArrayList<MyActivity>();
    public static String add1 = null;
    public static String add2 = null;
    public static boolean tog_add = true;

    public static int marker_pos = 0;
    public static int marker_pos_prev =0;
    public static Polyline line_prev = null;
    public float myzoom = 16f;
    public static Marker last_marker=null;
    public static Marker bef_last_marker=null;

    public static final int REQUEST_ACTIVITY_FILE_LIST = 0x0001;
    public static boolean nomarker = false;
    public static boolean notrack = false;
    public static boolean satellite = false;

    File _file_list[] = null;
    File _file = null;
    MyActivity lastActivity = null;


    private void initializeContentViews(int layout) {
        setContentView(layout);
    }

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

            final ImageButton imbt_marker = (ImageButton) findViewById(R.id.imbt_marker);
            final ImageButton imbt_navi = (ImageButton) findViewById(R.id.imbt_navi);
            final ImageButton imbt_trash = (ImageButton) findViewById(R.id.imbt_trash);
            final ImageButton imbt_pop_menu = (ImageButton) findViewById(R.id.imbt_pop_menu);
            final File flist[] = MyActivityUtil.getFiles(filetype);

            public void GO(final GoogleMap googleMap, File myfile) {
                Log.e(TAG, "-- filename to see: " + myfile.getAbsolutePath());
                googleMap.clear();
                markers = new ArrayList<Marker>();
                ActivityStat activityStat = null;

                if(myfile != null) mActivityList = MyActivityUtil.deserialize(myfile);

                if(mActivityList==null) {
                    Log.e(TAG, "-- " + myfile + " failed to be deserialized");
                    return;
                } else if(mActivityList.size()==0) {
                    Log.e(TAG, "-- " + myfile + " serialized successfully but the size is 0");
                } else {
                        Log.d(TAG, "-- " + myfile + " is deserialized successfully! with # of " + mActivityList.size());
                }

                if(mActivityList.size()>1) {
                    add1 = AddressUtil.getAddress(_ctx, mActivityList.get(0));
                    add2 = AddressUtil.getAddress(_ctx, mActivityList.get(mActivityList.size()-1));
                    marker_pos = mActivityList.size()-1;
                }

                Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(mActivityList.get(0).latitude, mActivityList.get(0).longitude,1);
                }catch(Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }

                String addinfo = null;
                if(addresses == null || addresses.size() ==0) {
                    Log.e(TAG, "No Addresses found !!");
                }else {
                    addinfo = addresses.get(0).getAddressLine(0).toString();
                }

                //tv_file.setText(myfile.getName().substring(0, myfile.getName().length()-4));

                if(mActivityList.size()==0) return;

                MyActivity ta = mActivityList.get(0);
                String date_str = ta.cr_date + " " + ta.cr_time;
                Log.d(TAG, "-- FileActivity, getStartTime: " + date_str);

                activityStat= getActivityStat(mActivityList);

                if(activityStat !=null) {
                    String _minDist = String.format("%.2f", activityStat.distanceKm);
                    String sinfo = "" + date_str;

                    tv_activity_name.setText(activityStat.name);
                    tv_date_str.setText(activityStat.date_str);

                    tv_distance.setText(_minDist);
                    tv_duration.setText(activityStat.duration);
                    tv_minperkm.setText(String.format("  %.2f",activityStat.minperKm));
                    tv_carolies.setText("   " + activityStat.calories);
                } else {
                    Toast.makeText(getApplicationContext(), "ERR: No Statistics Information !", Toast.LENGTH_LONG).show();
                    String _minDist = String.format("-");
                    String sinfo = "" + date_str + "  (" + _minDist + "Km)";
                    tv_distance.setText(_minDist);
                    tv_duration.setText("-");
                    tv_minperkm.setText("-");
                    tv_carolies.setText("-");
                }

                DRAW(googleMap);

                Display display = getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics( metrics );
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;

                boolean got_bound_wo_error = false;
                int try_cnt = 0;

                do {
                    try {
                        Log.e(TAG, "Tying to get Bound with width:" + width + ", height:" + height);
                        doBoundBuild(googleMap, width, height);
                        got_bound_wo_error = true;
                    } catch (Exception e) {
                        try_cnt++;
                        Log.e(TAG, e.toString() + "Trying to get again... (try_cnt:" +try_cnt+")");
                    }
                }while(!got_bound_wo_error && try_cnt < 3);
                if(!got_bound_wo_error) { myzoom = 16; moveCamera(googleMap, myzoom); }
            }

            public void alertDeleteDialog(File file) {
                AlertDialog.Builder builder = new AlertDialog.Builder(_ctx);
                builder.setTitle("파일을 삭제하시겠습니까?");
                builder.setMessage("파일을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                file.delete();
                                File flist[] = MyActivityUtil.getFiles(filetype);
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

            public void DRAW(GoogleMap googleMap) {
                MapUtil.initialize(); //MapUtil은 사용하기 전에 반드시 초기화를 해서 마크정도 초기화
                googleMap.clear();
                if(mActivityList.size()==0) {
                    Toast.makeText(_ctx,"No activities!", Toast.LENGTH_SHORT).show();
                } else {
                    lastActivity = mActivityList.get(mActivityList.size()-1);
                }

                if(!nomarker) MapUtil.drawMarkers(googleMap,mActivityList);
                if(!notrack) MapUtil.drawTrack(_ctx,googleMap,mActivityList);
                if(!satellite) googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                else googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                if(nomarker || notrack) {
                    MapUtil.drawStartMarker(googleMap,mActivityList);
                    MapUtil.drawEndMarker(googleMap,mActivityList);
                }

                Display display = getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics( metrics );
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;

                boolean got_bound_wo_error = false;
                int try_cnt = 0;

                Log.d(TAG,"-- before add all marker to do do Bound build!");
                ArrayList<Marker> _markers = new ArrayList<>();
                for(int i=0;i<mActivityList.size();i++) {
                    Marker marker = googleMap.addMarker(
                            new MarkerOptions().position(mActivityList.get(i).toLatLng()).title("").visible(false));
                    _markers.add(marker);
                }
                Log.d(TAG,"-- after add all marker to do do Bound build!");

                do {
                    try {
                        MapUtil.doBoundBuild(googleMap, _markers, width, height);
                        got_bound_wo_error = true;
                    } catch (Exception e) {
                        try_cnt++;
                    }
                }while(!got_bound_wo_error && try_cnt < 3);
                if(!got_bound_wo_error) {
                    int myzoom = 16;
                    if(lastActivity!=null) MapUtil.moveCamera(googleMap, lastActivity, myzoom);
                }
            }

            @Override
            public void onMapReady(final GoogleMap googleMap) {
                _googleMap = googleMap;
                GO(googleMap, _file);

                imbt_prev.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {

                        File flist[] = MyActivityUtil.getFiles(filetype);
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
                        File flist[] = MyActivityUtil.getFiles(filetype);
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
                        imbt_pop_menu.setVisibility(View.VISIBLE);
                        nomarker = !nomarker;
                        DRAW(googleMap);
                    }
                });

                imbt_navi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.GONE);
                        imbt_navi.setVisibility(View.GONE);
                        imbt_trash.setVisibility(View.GONE);
                        imbt_pop_menu.setVisibility(View.VISIBLE);
                        notrack = !notrack;
                        DRAW(googleMap);
                    }
                });

                imbt_pop_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.VISIBLE);
                        imbt_navi.setVisibility(View.VISIBLE);
                        imbt_trash.setVisibility(View.VISIBLE);
                        imbt_pop_menu.setVisibility(View.GONE);
                    }
                });

                imbt_trash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.GONE);
                        imbt_navi.setVisibility(View.GONE);
                        imbt_trash.setVisibility(View.GONE);
                        imbt_pop_menu.setVisibility(View.VISIBLE);

                        File flist[] = MyActivityUtil.getFiles(filetype);
                        try {
                            alertDeleteDialog(flist[position]);
                            flist = MyActivityUtil.getFiles(filetype);
                            if (flist.length > 1) position=position;
                            else position=0;
                        }catch(Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "-- file delete err: " + e.toString());
                        }
                        GO(googleMap, flist[position]);
                    }
                });

            } /* on  MapReady */
        });
    } /* onCreate */

    public void moveCamera(GoogleMap googleMap, float _zoom) {
        if(mActivityList==null) return;
        if(mActivityList.size()==0) return;

        LatLng curloc = new LatLng(mActivityList.get(mActivityList.size()-1).latitude,
                mActivityList.get(mActivityList.size()-1).longitude);
        myzoom = _zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(curloc).zoom(_zoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void moveCamera(GoogleMap googleMap) {
        if(mActivityList==null) return;
        if(mActivityList.size()==0) return;

        myzoom = googleMap.getCameraPosition().zoom;
        LatLng curloc = new LatLng(mActivityList.get(mActivityList.size()-1).latitude,
                mActivityList.get(mActivityList.size()-1).longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(curloc).zoom(myzoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    public void moveCamera(GoogleMap googleMap, LatLng loc, float _zoom) {
        myzoom = _zoom;
        myzoom = googleMap.getCameraPosition().zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(myzoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    public void moveCamera(GoogleMap googleMap, LatLng loc) {
        myzoom = googleMap.getCameraPosition().zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(myzoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    public static ActivityStat getActivityStat(ArrayList <MyActivity> list) {
        if(list == null) {
            Log.e(TAG,"Activity List null");
            return null;
        }
        if(list.size() < 2) {
            Log.e(TAG,"Activity size < 2");
            return null;
        }

        MyActivity start, stop;
        start = list.get(0);
        stop = list.get(list.size()-1);

        Date start_date, stop_date;
        start_date = StringUtil.StringToDate(start);
        stop_date = StringUtil.StringToDate(stop);

        Log.d(TAG, "-- start_date:" + start_date);
        Log.d(TAG, "-- stop_date:" + stop_date);

        Log.e(TAG, "-- 출발:" + start.toString());
        Log.e(TAG, "-- 종료:" + stop.toString());

        String duration = StringUtil.elapsedStr(start_date, stop_date); // <- Error code
        Log.e(TAG, duration);

        double total_distM = MyActivityUtil.getTotalDistanceInDouble(list);  // <-
        double total_distKm = total_distM / 1000f;
        double minpk = MyActivityUtil.getMinPerKm(start_date, stop_date, total_distKm); // <-

        float burntkCal;
        int durationInSeconds = MyActivityUtil.durationInSeconds(list);
        int stepsTaken = (int) (total_distM / Config._strideLengthInMeters);
        burntkCal = CaloryUtil.calculateEnergyExpenditure((float)total_distM / 1000f, durationInSeconds);
        ActivityStat as = new ActivityStat(start_date, stop_date, duration, total_distM, total_distKm, minpk, (int)burntkCal);
        return as;
    }

    public static void doBoundBuild(GoogleMap gmap, int width, int height) throws Exception {
        if(markers.size()==0) return;

        LatLngBounds.Builder builder= new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        boolean berr = false;
        try {
            Log.e(TAG, "newLatLngBounds(bounds):" + bounds);
            Log.e(TAG, "newLatLngBounds(padding):" + padding);

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            gmap.moveCamera(cu);

        }catch(Exception e) {
            berr = true;
            Log.e(TAG,"ERR] BoundBuild:" + e.toString());
            throw e;
        }
    }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

            case R.id.file_activity2:
                Log.d(TAG,"-- FileActivity2!");
                Intent fileactivity2 = new Intent(this, FileActivity2.class);
                fileactivity2.putExtra("1", 1);
                startActivityForResult(fileactivity2, Config.CALL_FILE_ACTIVITY);
                return true;

            case R.id.ReportActivity:
                Log.d(TAG,"-- Report Activity!");
                Intent reportActivity = new Intent(this, MyReportActivity.class);
                reportActivity.putExtra("activity_file_name", "20210522_110818");
                startActivityForResult(reportActivity, Config.CALL_REPORT_ACTIVITY);
                return true;

            case R.id.quote_activity:
                Log.d(TAG,"-- Quote Activity!");
                Intent quoteIntent = new Intent(this, QuoteActivity.class);
                quoteIntent.putExtra("1", 1);
                startActivityForResult(quoteIntent, Config.CALL_QUOTE_ACTIVITY);
                return true;

            case R.id.scrollpic_activity:
                Log.d(TAG,"-- Scroll Pic Activity!");
                Intent scrollPicIntent = new Intent(this, ScrollPicActivity.class);
                startActivityForResult(scrollPicIntent, Config.CALL_SCROLL_PIC_ACTIVITY);

                return true;

            case R.id.scrollAllpic_activity:
                Log.d(TAG,"-- Scroll Pic Activity!");
                Intent scrollAllPicIntent = new Intent(this, ScrollAllPicActivity.class);
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
