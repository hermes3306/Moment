package com.jason.moment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.IBinder;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.jason.moment.util.ActivityStat;
import com.jason.moment.util.CalDistance;
import com.jason.moment.util.CalcTime;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.StringUtil;
import com.jason.moment.util.UI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FileActivity extends AppCompatActivity {
    public static String TAG = "FileActivity";
    public static int position = 0;
    public static ArrayList<Marker> markers = null;
    public static ArrayList<MyActivity> mActivityList = new ArrayList<MyActivity>();
    public static String add1 = null;
    public static String add2 = null;
    public static boolean tog_add = true;
    public static String fname = null;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        Intent intent = getIntent();
        fname = intent.getExtras().getString("file");
        position = intent.getExtras().getInt("pos");

        final Context _ctx = this;
        final File _file = new File(fname);

        final MapView mMapView = (MapView) findViewById(R.id.mapView);
        MapsInitializer.initialize(this);

        mMapView.onCreate(savedInstanceState);  // check required ....
        mMapView.onResume();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            final TextView tv_cursor = (TextView) findViewById(R.id.tv_cursor);
            final TextView tv_cursor2 = (TextView) findViewById(R.id.tv_cursor2);

            final TextView tv_file = (TextView) findViewById(R.id.tv_file);
            final TextView tv_heading = (TextView) findViewById(R.id.tv_heading);
            final ImageButton imbt_prev = (ImageButton) findViewById(R.id.imbt_prev);
            final ImageButton imbt_next = (ImageButton) findViewById(R.id.imbt_next);
            final TextView tv_distance = (TextView) findViewById(R.id.tv_distance);
            final TextView tv_duration = (TextView) findViewById(R.id.tv_duration);
            final TextView tv_minperkm = (TextView) findViewById(R.id.tv_minperkm);
            final TextView tv_carolies = (TextView) findViewById(R.id.tv_carolies);
            final TextView tv_address = (TextView) findViewById(R.id.tv_address);
            final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            final ImageButton imbt_marker = (ImageButton) findViewById(R.id.imbt_marker);
            final ImageButton imbt_navi = (ImageButton) findViewById(R.id.imbt_navi);

            final File flist[] = MyActivityUtil.getFiles(".mnt",true);

            public void GO(final GoogleMap googleMap, File myfile) {
                googleMap.clear();
                markers = new ArrayList<Marker>();
                ActivityStat activityStat = null;

                if(myfile != null) mActivityList = deserialize(myfile);
                if(mActivityList==null) {
                    Log.e(TAG, "" + myfile + " failed to be deserialized");
                    return;
                } else if(mActivityList.size()==0) {
                    Log.e(TAG, "" + myfile + " serialized successfully but the size is 0");
                } else {
                        Log.d(TAG, "" + myfile + " is deserialized successfully! with # of " + mActivityList.size());
                }

                if(mActivityList.size()>1) {
                    add1 = MyActivityUtil.getAddress(_ctx, mActivityList.get(0));
                    add2 = MyActivityUtil.getAddress(_ctx, mActivityList.get(mActivityList.size()-1));
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
                tv_file.setText(myfile.getName().substring(0, myfile.getName().length()-4));

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
                if(!notrack) drawTrack(googleMap,mActivityList);
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
                        doBoundBuild(googleMap, width, height);
                        got_bound_wo_error = true;
                    } catch (Exception e) {
                        try_cnt++;
                        Log.e(TAG, e.toString() + "Trying to get again... (try_cnt:" +try_cnt+")");
                    }
                }while(!got_bound_wo_error && try_cnt < 3);
                if(!got_bound_wo_error) { myzoom = 16; moveCamera(googleMap, myzoom); }
            }

            @Override
            public void onMapReady(final GoogleMap googleMap) {
                GO(googleMap, _file);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

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

                        float color = (marker_pos==0? BitmapDescriptorFactory.HUE_ROSE:BitmapDescriptorFactory.HUE_CYAN);
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(nextpos).title(MyActivityUtil.getAddressDong(_ctx, mActivityList.get(marker_pos)))
                                .icon(BitmapDescriptorFactory.defaultMarker(color))
                                .draggable(true)
                                .visible(true)
                                .snippet(elapsedstr + " ("+diststr+")"));

                        if(bef_last_marker!=null) bef_last_marker.remove();
                        if(last_marker!=null) last_marker.remove();
                        last_marker = marker;

                        marker.showInfoWindow();

                        drawTrack(googleMap,mActivityList,0,marker_pos);

                        //
                        tv_heading.setText(MyActivityUtil.getTimeStr(mActivityList, marker_pos));
                        tv_address.setText(MyActivityUtil.getAddress(_ctx, mActivityList.get(marker_pos)));

                        String inx_str= "" + seekBar.getProgress() + "/" + seekBar.getMax();
                        String inx_str2= "" + (position+1)  + "/" + (flist.length);
                        tv_cursor.setText(inx_str);
                        tv_cursor2.setText(inx_str2);
                        tv_file.setText(_file.getName().substring(0, _file.getName().length()-4));

                    }
                });

                imbt_prev.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {
                        File flist[] = MyActivityUtil.getFiles();
                        if (position > 0 && position <= flist.length) position--;
                        else position=flist.length-1;
                        GO(googleMap, flist[position]);
                    }
                });

                tv_cursor2.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {
                        File flist[] = MyActivityUtil.getFiles();
                        if (position >= 0 && position < flist.length-1) position++;
                        else position=0;
                        GO(googleMap, flist[position]);
                    }
                });


                imbt_next.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        File flist[] = MyActivityUtil.getFiles();
                        if (position >= 0 && position < flist.length-1) position++;
                        else position=0;
                        GO(googleMap, flist[position]);
                    }
                });

                imbt_marker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nomarker = !nomarker;
                        googleMap.clear();
                        if(!nomarker) drawMarkers(googleMap,mActivityList);
                        if(!notrack) drawTrack(googleMap,mActivityList);
                        if(!satellite) googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        else googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        if(nomarker || notrack) {
                            drawStartMarker(googleMap,mActivityList);
                            drawEndMarker(googleMap,mActivityList);
                        }
                    }
                });

                imbt_navi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notrack = !notrack;
                        googleMap.clear();
                        if(!nomarker) drawMarkers(googleMap,mActivityList);
                        if(!notrack) drawTrack(googleMap,mActivityList);
                        if(!satellite) googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        else googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        if(nomarker || notrack) {
                            drawStartMarker(googleMap,mActivityList);
                            drawEndMarker(googleMap,mActivityList);
                        }
                    }
                });

                tv_address.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {
                        if(tog_add) {
                            seekBar.setProgress(0,true);
                        }else {
                            seekBar.setProgress(mActivityList.size()-1, true);
                        }
                        tog_add = !tog_add;
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

        String duration = StringUtil.Duration(start_date, stop_date); // <- Error code
        Log.e(TAG, duration);

        double total_distM = getTotalDistanceDouble(list);  // <-
        double total_distKm = total_distM / 1000f;
        double minpk = getMinPerKm(start_date, stop_date, total_distKm); // <-

        ActivityStat as = new ActivityStat(start_date, stop_date, duration, total_distM, total_distKm, minpk, 0);
        return as;
    }

    public static double getMinPerKm(Date start, Date end, double km) {
        long dur_sec = (end.getTime() - start.getTime())/1000;
        long dur_min = dur_sec/60;

        double minpk = (double)(dur_min / km);
        return minpk;
    }

    public ArrayList<MyActivity> deserialize(File file) {
        if(file == null)  {
            Log.d(TAG, "-- No File to deserialized");
            return null;
        } else Log.d(TAG, "-- " + file.getAbsolutePath() + " to be deserialized");

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;

        ArrayList list = null;

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);

            list = new ArrayList<MyActivity>();
            MyActivity ma=null;

            do {
                try {
                    ma = (MyActivity) in.readObject();
                    list.add(ma);
                }catch(Exception ex) {
                    ex.printStackTrace();
                    Log.e(TAG, ex.toString());
                    break;
                }
            } while(ma != null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "--" + e.toString());
        } finally {
            try {
                if (in != null) in.close();
                if (bis !=null) in.close();
                if (fis !=null) fis.close();

                if(list.size()==0) {
                    Log.e(TAG, "File ("+ file.getAbsolutePath() +") corrupted !!!!");
                    file.delete();
                    Log.e(TAG, "File ("+ file.getAbsolutePath() +") deleted  !!!!");
                }
            }catch(Exception e) {}
        }

        if(list ==null) return null;
        return list;

    }

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
                Log.e(TAG, "Double.NaN between ("+bef_lat + ","+ bef_lon +") ~ ("+ aft_lat + ","+ aft_lon + ")" ) ;
                continue;
            } else if ( Double.isNaN(dist_meter + dist_2)) {
                Log.e(TAG, "Double.NaN between ("+bef_lat + ","+ bef_lon +") ~ ("+ aft_lat + ","+ aft_lon + ")" ) ;
                continue;
            }
            dist_meter = dist_meter + dist_2;
            //Log.e(TAG, "" + i + "]" +  list.get(i).added_on + dist_2 + " sum: " + dist_meter +  " ("+bef_lat + ","+ bef_lon +") ~ ("+ aft_lat + ","+ aft_lon + ")");
            //Log.e(TAG, "" + dist_2 + " sum: " + dist_meter);
        }
        return dist_meter;
    }

    public static void drawTrack(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list == null) return;
        ArrayList<LatLng> l = new ArrayList<>();
        for(int i=0; i<list.size();i++) {
            l.add(new LatLng(list.get(i).latitude, list.get(i).longitude));
        }

        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.RED);
        Polyline line = gmap.addPolyline(plo);
        line.setWidth(20);
        line.setPoints(l);
    }

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
}