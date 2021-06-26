package com.jason.moment.util;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class    MapUtil {
    private static final String TAG = "MapUtil";
    static int[] colors = {
            Color.RED,
            Color.CYAN,
            Color.BLUE,
            Color.WHITE,
            Color.BLACK,
            Color.YELLOW,
            Color.DKGRAY,
            Color.GREEN,
            Color.LTGRAY
    };

    public static void toggleNoMarker() {
        C.nomarkers = !C.nomarkers;
    }
    public static void toggleNoTrack() {
        C.notrack = !C.notrack;
    }

    public static ArrayList<Marker> markers = null;
    public static void initialize() {
        markers = new ArrayList<Marker>();
    }

    public static void moveCamera(GoogleMap googleMap, MyMediaInfo mm, float _zoom) {
        LatLng _loc = mm.toLatLng();
        CameraPosition cameraPosition = new CameraPosition.Builder().target(_loc).zoom(_zoom).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public static void moveCamera(GoogleMap googleMap, MyActivity myactivity, float _zoom) {
        LatLng _loc = myactivity.toLatLng();
        CameraPosition cameraPosition = new CameraPosition.Builder().target(_loc).zoom(_zoom).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public static void drawMarker(GoogleMap gmap, String title, String snippet, LatLng ll) {
        float color =  Config._marker_start_color;
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(true)
                .visible(true)
                .snippet(snippet));
        markers.add(marker);
    }

    public static void drawMarker(GoogleMap gmap, MyMediaInfo mm) {
        LatLng ll = new LatLng(mm.latitude, mm.longitude);
        float color =  Config._marker_start_color;
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(mm.memo)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(true)
                .visible(true)
                .snippet(mm.cr_datetime));
        markers.add(marker);
    }

    public static void drawStartMarker(GoogleMap gmap, @NotNull ArrayList<MyActivity> list) {
        if(list.size()==0) return;
        float color =  Config._marker_start_color;
        LatLng ll = new LatLng(list.get(0).latitude, list.get(0).longitude);

        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("Start")
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(true)
                .visible(true)
                .snippet(list.get(0).cr_time));
        markers.add(marker);
    }

    public static void drawEndMarker(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list.size()<=1) return;
        float color =  Config._marker_end_color;
        LatLng ll = new LatLng(list.get(list.size()-1).latitude, list.get(list.size()-1).longitude);
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("End")
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(true)
                .visible(true)
                .snippet(list.get(list.size()-1).cr_time));
        markers.add(marker);
    }

    public static void drawAllMarkers(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(C.nomarkers) return;

        double tot_distance = MyActivityUtil.getTotalDistanceInDouble(list);
        int disunit = (int)(tot_distance / 10);

        double t_distance = 0;
        double t_lap = disunit;
        for(int i=0; i < list.size(); i++) {
            LatLng ll = new LatLng(list.get(i).latitude, list.get(i).longitude);
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
                    t_lap += disunit;
                    String _title;
                    if (t_distance < 1000) _title = String.format("%.0f", t_distance) +"m";
                    else _title = String.format("%.2f", t_distance/1000) +"km";

                    Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                            .icon(BitmapDescriptorFactory.fromResource(Config._marker_icon))
                            .draggable(true)
                            .visible(true)
                            .alpha(Config._marker_alpha).title(_title)
                            .snippet(list.get(i).cr_time));
                    markers.add(marker);
                }
            }
        }
    }

    public static void drawTrackInRange(Context context, GoogleMap map, ArrayList<MyActivity> latLngArrayList, int start, int end) {
        if(C.nomarkers) return;
        if(latLngArrayList == null) return;
        ArrayList<LatLng> latLngArrayListInRange = new ArrayList<>();
        for(int i=start; i < end; i++) {
            latLngArrayListInRange.add(new LatLng(latLngArrayList.get(i).latitude, latLngArrayList.get(i).longitude));
        }

        int color_inx = 0;
        int width = 20;
        try {
            color_inx = Config.getIntPreference(context, "track_color");
            width = Config.getIntPreference(context, "track_width");
        }catch(Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        int color = colors[color_inx];
        drawTrack(map,latLngArrayListInRange,color,width);
    }

    public static void drawTrack(Context context, GoogleMap map, ArrayList<MyActivity> myActivityArrayList) {
        if(C.notrack) return;
        if (myActivityArrayList == null) return;
        int color_inx = Config.getIntPreference(context, "track_color");
        int width = Config.getIntPreference(context, "track_width");
        int color = colors[color_inx];

        ArrayList<LatLng> latLngArrayListInRange = new ArrayList<>();
        for(int i=0; i < myActivityArrayList.size(); i++) {
            latLngArrayListInRange.add(myActivityArrayList.get(i).toLatLng());
        }
        drawTrack(map,latLngArrayListInRange,color, width);
    }

    public static void drawTrack(GoogleMap map, ArrayList<LatLng> latLngArrayList,int color, int width) {
        if(C.notrack) return;
        if(latLngArrayList == null) return;
        PolylineOptions plo = new PolylineOptions();


        boolean right_color=false;
        int col_inx=-1;
        for(int i=0;i<colors.length;i++) {
            if(color == colors[i]) { right_color = true;col_inx=i;}
            //Log.d(TAG, "-- colors["+i+"] = " + colors[i]);
        }
        if(!right_color) color=colors[0];

//        Log.d(TAG,"-- track color index is: " + col_inx);
//        Log.d(TAG,"-- track color is: " + colors[col_inx]);

        if(width<0) width=10;
        plo.color((int)color);
        Polyline polyLine = map.addPolyline(plo);
        polyLine.setWidth(width);
        polyLine.setPoints(latLngArrayList);
    }

    public static void DRAW(Context _ctx, GoogleMap googleMap, int width, int height, ArrayList<MyActivity>mActivityList) {
        MapUtil.initialize();   //MapUtil은 사용하기 전에 반드시 초기화를 해서 마크정보 초기화
        googleMap.clear();
        MyActivity ActivityAtCenter=null;
        if(mActivityList.size()==0) {
            Toast.makeText(_ctx,"No activities!", Toast.LENGTH_SHORT).show();
        } else {
            ActivityAtCenter = mActivityList.get( (mActivityList.size()-1) / 2 );
        }

        MapUtil.drawAllMarkers(googleMap,mActivityList);
        MapUtil.drawTrack(_ctx.getApplicationContext(), googleMap,mActivityList);
        if(!C.satellite) googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if(C.nomarkers) {
            MapUtil.drawStartMarker(googleMap,mActivityList);
            MapUtil.drawEndMarker(googleMap,mActivityList);
        }
        ArrayList<Marker> _markers = new ArrayList<>();
        for(int i=0;i<mActivityList.size();i++) {
            Marker marker = googleMap.addMarker(
                    new MarkerOptions().position(mActivityList.get(i).toLatLng()).title("").visible(false));
            _markers.add(marker);
        }
        findBestBound(googleMap, _markers, width, height, ActivityAtCenter);
    }

    public static void findBestBound(GoogleMap googleMap, ArrayList<Marker> _markers, int w, int h, MyActivity lastAct) {
        boolean got_bound_wo_error = false;
        int try_cnt = 0;

        do {
            try {
                MapUtil.doBoundBuild(googleMap, _markers, w, h);
                got_bound_wo_error = true;
            } catch (Exception e) {
                try_cnt++;
            }
        }while(!got_bound_wo_error && try_cnt < 2);
        if(!got_bound_wo_error) {
            int myzoom = 14;
            if(lastAct!=null) MapUtil.moveCamera(googleMap, lastAct, myzoom);
        }
    }

    public static void DRAW(Context _ctx, GoogleMap googleMap, Display display, ArrayList<MyActivity>mActivityList) {
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics( metrics );
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        DRAW(_ctx, googleMap, width, height, mActivityList);
    }


    public static void doBoundBuild(GoogleMap gmap, ArrayList<Marker> _markers, int width, int height) throws Exception {
        if(_markers.size()==0) return;

        LatLngBounds.Builder builder= new LatLngBounds.Builder();
        for (Marker marker : _markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = (int) (width * 0.15); // offset from edges of the map 10% of screen

        boolean berr = false;
        try {
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            gmap.moveCamera(cu);
        }catch(Exception e) {
            berr = true;
            Log.e(TAG,"-- ERR] BoundBuild:" + e.toString());
            throw e;
        }
    }
}
