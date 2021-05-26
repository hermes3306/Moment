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
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
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
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileActivity2 extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener{
    public static String TAG = "FileActivity2";
    private     Context _ctx=null;
    private     GoogleMap googleMap;
    TextView    tv_cursor;
    TextView    tv_cursor2;
    TextView    tv_file;
    TextView    tv_heading;
    ImageButton imbt_prev;
    ImageButton imbt_next;
    TextView    tv_distance;
    TextView    tv_duration;
    TextView    tv_minperkm;
    TextView    tv_carolies;
    TextView    tv_address;
    SeekBar     seekBar;
    ImageButton imbt_marker;
    ImageButton imbt_navi;
    ImageButton imbt_trash;
    File flist[];

    String _layout_names[] = {"White","Night","Custome"};
    int _layout[] = {
            R.layout.activity_file1,
            R.layout.activity_file2,
            R.layout.activity_file3
    };

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
    public static boolean nomarker = true;
    public static boolean notrack = false;
    public static boolean satellite = false;

    File _file_list[] = null;
    File _file = null;

    Bundle _savedInstanceState=null;
    private void initializeContentViews(int layout) {
        initializeContentViews(_savedInstanceState,  layout);
    }

    private void initializeContentViews(Bundle savedInstanceState, int layout) {
        if(_savedInstanceState==null) _savedInstanceState = savedInstanceState;
        setContentView(layout);
        MapView mapView = findViewById(R.id.mapView);
        MapsInitializer.initialize(this);
        mapView.onCreate(savedInstanceState);  // check required ....
        mapView.onResume();
        mapView.getMapAsync(this);
        tv_cursor   = (TextView) findViewById(R.id.tv_cursor);
        tv_cursor2  = (TextView) findViewById(R.id.tv_cursor2);
        tv_file     = (TextView) findViewById(R.id.tv_file);
        tv_heading  = (TextView) findViewById(R.id.tv_heading);
        imbt_prev   = (ImageButton) findViewById(R.id.imbt_prev);
        imbt_next   = (ImageButton) findViewById(R.id.imbt_next);
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        tv_duration = (TextView) findViewById(R.id.tv_duration);
        tv_minperkm = (TextView) findViewById(R.id.tv_minperkm);
        tv_carolies = (TextView) findViewById(R.id.tv_carolies);
        tv_address  = (TextView) findViewById(R.id.tv_address);
        seekBar     = (SeekBar) findViewById(R.id.seekBar);
        imbt_marker = (ImageButton) findViewById(R.id.imbt_marker);
        imbt_navi   = (ImageButton) findViewById(R.id.imbt_navi);
        imbt_trash  = (ImageButton) findViewById(R.id.imbt_trash);
        flist       = MyActivityUtil.getFiles(filetype);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) { }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mActivityList == null) return;
                marker_pos_prev = marker_pos;
                marker_pos = seekBar.getProgress();

                LatLng nextpos = new LatLng(mActivityList.get(marker_pos).latitude,
                        mActivityList.get(marker_pos).longitude);
                LatLng prevpos = new LatLng(mActivityList.get(0).latitude,
                        mActivityList.get(0).longitude);

                CalDistance cd =  new CalDistance(prevpos, nextpos);
                double dist = cd.getDistance();

                String diststr = null;
                String elapsedstr=null;
                if(dist > 1000.0f) diststr = cd.getDistanceKmStr();
                else diststr = cd.getDistanceMStr();

                CalcTime ct = new CalcTime(mActivityList.get(marker_pos_prev), mActivityList.get(marker_pos));
                long elapsed = ct.getElapsed();

                if(elapsed > 60*60000) elapsedstr = ct.getElapsedHourStr();
                else if(elapsed > 60000) elapsedstr = ct.getElapsedMinStr();
                else elapsedstr = ct.getElapsedSecStr();

                moveCamera(googleMap, nextpos);
                String addr_Dong="";
                float color = (marker_pos==0? BitmapDescriptorFactory.HUE_ROSE:BitmapDescriptorFactory.HUE_CYAN);
                try {
                    addr_Dong = AddressUtil.getAddressDong(_ctx, mActivityList.get(marker_pos));
                }catch(Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "-- "+e);
                }
                Marker marker = googleMap.addMarker(new MarkerOptions().position(nextpos).title(addr_Dong)
                        .icon(BitmapDescriptorFactory.defaultMarker(color))
                        .draggable(true)
                        .visible(true)
                        .snippet(elapsedstr + " ("+diststr+")"));

                if(bef_last_marker!=null) bef_last_marker.remove();
                if(last_marker!=null) last_marker.remove();
                last_marker = marker;

                marker.showInfoWindow();
                MapUtil.drawTrackInRange(_ctx,googleMap,mActivityList,marker_pos_prev, marker_pos);
                //
                tv_heading.setText(MyActivityUtil.getTimeStr(mActivityList, marker_pos));
                tv_address.setText(AddressUtil.getAddress(_ctx, mActivityList.get(marker_pos)));

                String inx_str= "" + seekBar.getProgress() + "/" + seekBar.getMax();
                String inx_str2= "" + (position+1)  + "/" + (flist.length);
                tv_cursor.setText(inx_str);
                tv_cursor2.setText(inx_str2);
                tv_file.setText(_file.getName().substring(0, _file.getName().length()-4));
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.initialize(_ctx);
        super.onCreate(savedInstanceState);
        int inx = Config.getIntPreference(this,"file_screen");
        initializeContentViews(_layout[inx]);
    }

    public void alertDeleteDialog(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_ctx);
        builder.setTitle("파일을 삭제하시겠습니까?");
        builder.setMessage("파일을 삭제하시겠습니까?");
        builder.setPositiveButton("삭제",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        file.delete();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }


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

    public static void drawStartMarker(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list.size()==0) return;
        LatLng ll = new LatLng(list.get(0).latitude, list.get(0).longitude);
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("출발")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .draggable(true)
                .visible(true)
                .snippet("출발"));
        markers.add(marker);
    }

    public static void drawEndMarker(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list.size()==0) return;
        LatLng ll = new LatLng(list.get(list.size()-1).latitude, list.get(list.size()-1).longitude);
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("종료")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(true)
                .visible(true)
                .snippet("종료"));
        markers.add(marker);
    }

    public static void drawMarkers(GoogleMap gmap, ArrayList<MyActivity> list) {
        double tot_distance = MyActivityUtil.getTotalDistanceInDouble(list);

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

            String title = StringUtil.getDateTimeString(list.get(i));
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
                    //Log.e(TAG, "" + interval + unitstr);
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
        File flist[] = null;
        switch (v.getId()) {
            case R.id.tv_address:
                if(tog_add) {
                    seekBar.setProgress(0,true);
                }else {
                    seekBar.setProgress(mActivityList.size()-1, true);
                }
                tog_add = !tog_add;
            case R.id.tv_cursor:
            case R.id.tv_cursor2:
                flist = MyActivityUtil.getFiles(filetype);
                if (position >= 0 && position < flist.length-1) position++;
                else position=0;
                GO(googleMap, flist[position]);
                break;
            case R.id.imb_prev:
                flist = MyActivityUtil.getFiles(filetype);
                if(flist==null) {
                    Toast.makeText(getApplicationContext(),"No more files!",Toast.LENGTH_LONG).show();
                    return;
                }
                if (position > 0 && position <= flist.length) position--;
                else position=flist.length-1;
                GO(googleMap, flist[position]);
                break;
            case R.id.imb_next:
                flist = MyActivityUtil.getFiles(filetype);
                if(flist==null) {
                    Toast.makeText(getApplicationContext(),"No more files!",Toast.LENGTH_LONG).show();
                    return;
                }
                if (position >= 0 && position < flist.length-1) position++;
                else position=0;
                GO(googleMap, flist[position]);
                break;
            case R.id.imbt_marker:
                nomarker = !nomarker;
                googleMap.clear();
                if(!nomarker) drawMarkers(googleMap,mActivityList);
                if(!notrack) MapUtil.drawTrack(_ctx,googleMap,mActivityList);
                if(!satellite) googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                else googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                if(nomarker || notrack) {
                    drawStartMarker(googleMap,mActivityList);
                    drawEndMarker(googleMap,mActivityList);
                }
                break;
            case R.id.imbt_navi:
                notrack = !notrack;
                googleMap.clear();
                if(!nomarker) drawMarkers(googleMap,mActivityList);
                if(!notrack) MapUtil.drawTrack(_ctx,googleMap,mActivityList);
                if(!satellite) googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                else googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                if(nomarker || notrack) {
                    drawStartMarker(googleMap,mActivityList);
                    drawEndMarker(googleMap,mActivityList);
                }
                break;
            case R.id.imbt_trash:
                flist = MyActivityUtil.getFiles(filetype);
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
                break;
            case R.id.imSetting:
                Log.d(TAG, "-- Setting Activities!");
                Intent configIntent = new Intent(FileActivity2.this, ConfigActivity.class);
                configIntent.putExtra("1", 1);
                startActivityForResult(configIntent, Config.CALL_SETTING_ACTIVITY);
                break;
            case R.id.imLayout:
                Resources r = getResources();
                AlertDialog.Builder builder = new AlertDialog.Builder(FileActivity2.this )
                        .setItems(_layout_names, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                initializeContentViews(_layout[i]);
                                //Toast.makeText(getApplicationContext(),screen_layout[i], Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setTitle("Choose a layout")
                        .setPositiveButton("OK",null)
                        .setNegativeButton("Cancel",null);
                AlertDialog mSportSelectDialog = builder.create();
                mSportSelectDialog.show();
                break;
            default:
                break;
        }
    }

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
            try {
                add1 = AddressUtil.getAddress(_ctx, mActivityList.get(0));
                add2 = AddressUtil.getAddress(_ctx, mActivityList.get(mActivityList.size() - 1));
            }catch(Exception e) {
                e.printStackTrace();
                Log.e(TAG, "-- " + e);
            }
            marker_pos = mActivityList.size()-1;
            seekBar.setMax(mActivityList.size()-1);
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

        String inx_str= "" + seekBar.getProgress() + "/" + seekBar.getMax();
        String inx_str2= "" + (position+1)  + "/" + (flist.length);

        tv_cursor.setText(inx_str);
        tv_cursor2.setText(inx_str2);
        //tv_file.setText(myfile.getName().substring(0, myfile.getName().length()-4));
        tv_file.setText(myfile.getName());


        Log.d(TAG, "-- FileActivity, Tot # of Activity: " + inx_str);

        if(mActivityList.size()==0) return;

        MyActivity ta = mActivityList.get(0);
        String date_str = ta.cr_date + " " + ta.cr_time;
        Log.d(TAG, "-- FileActivity, getStartTime: " + date_str);

        activityStat= getActivityStat(mActivityList);

        if(activityStat !=null) {
            String _minDist = String.format("%.2f", activityStat.distanceKm);
            String sinfo = "" + date_str;

            tv_heading.setText(sinfo);
            tv_address.setText(addinfo);
            tv_distance.setText(_minDist);
            tv_duration.setText(activityStat.duration);
            tv_minperkm.setText(String.format("  %.2f",activityStat.minperKm));
            tv_carolies.setText("   " + activityStat.calories);
        } else {
            Toast.makeText(getApplicationContext(), "ERR: No Statistics Information !", Toast.LENGTH_LONG).show();
            String _minDist = String.format("-");
            String sinfo = "" + date_str + "  (" + _minDist + "Km)";
            tv_heading.setText(sinfo);
            tv_distance.setText(_minDist);
            tv_duration.setText("-");
            tv_minperkm.setText("-");
            tv_carolies.setText("-");
        }

        if(!nomarker) drawMarkers(googleMap,mActivityList);
        if(!notrack) MapUtil.drawTrack(_ctx,googleMap,mActivityList);
        if(!satellite) googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        if(nomarker || notrack) {
            drawStartMarker(googleMap,mActivityList);
            drawEndMarker(googleMap,mActivityList);
        }

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
                MapUtil.doBoundBuild(googleMap, width, height);
                got_bound_wo_error = true;
            } catch (Exception e) {
                try_cnt++;
                Log.e(TAG, e.toString() + "Trying to get again... (try_cnt:" +try_cnt+")");
            }
        }while(!got_bound_wo_error && try_cnt < 3);
        if(!got_bound_wo_error) { myzoom = 16; moveCamera(googleMap, myzoom); }
    } /* end of GO */

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.e(TAG,"-- onMapReady called.........................");
        if(_file==null) {
            _file_list = MyActivityUtil.getAllFiles();
            if (_file_list == null) {
                Toast.makeText(getApplicationContext(), "No files found!", Toast.LENGTH_LONG).show();
                finish();
                return;
            } else if (_file_list.length == 0) {
                Toast.makeText(getApplicationContext(), "No files found!", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            _file = _file_list[0];
            int inx = Config.getIntPreference(this,"file_screen");
            initializeContentViews(_layout[inx]);
        }
        GO(googleMap, _file);
    }



}
