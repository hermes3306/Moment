package com.ssing;

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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class MapUtil {
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
    public static int _marker_icon = R.drawable.draw_bike_48;
    public static float _marker_alpha = 0.9f;
    public static boolean _satellite = false;

    public static ArrayList<Marker> markers = null;
    public static void initialize() {
        markers = new ArrayList<Marker>();
    }
    public static void moveCamera(GoogleMap googleMap, LatLng loc, float _zoom) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(_zoom).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public static void drawMarker(GoogleMap gmap, String title, String snippet, LatLng ll, float color) {
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(true)
                .visible(true)
                .snippet(snippet));
        markers.add(marker);
    }

    public static void drawAllMarkers(GoogleMap gmap, ArrayList<Ssing> list) {
        for(int i=0; i < list.size(); i++) {
            LatLng ll = new LatLng(list.get(i).latitude, list.get(i).longitude);
            String title = (list.get(i)).getTitle();

            Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                    .icon(BitmapDescriptorFactory.fromResource(_marker_icon))
                    .draggable(true)
                    .visible(true)
                    .alpha(_marker_alpha).title(title)
                    .snippet(title));
            markers.add(marker);
        }
    }


    public static void DRAW(Context _ctx, GoogleMap googleMap, int width, int height, ArrayList<Ssing>ssList) {
        MapUtil.initialize();   //MapUtil은 사용하기 전에 반드시 초기화를 해서 마크정보 초기화
        googleMap.clear();
        Ssing ssingAtCenter=null;
        if(ssList.size()==0) {
            Toast.makeText(_ctx,"No activities!", Toast.LENGTH_SHORT).show();
        } else {
            ssingAtCenter = ssList.get( (ssList.size()-1) / 2 );
        }
        MapUtil.drawAllMarkers(googleMap,ssList);

        if(!_satellite) googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        ArrayList<Marker> _markers = new ArrayList<>();
        for(int i=0;i<ssList.size();i++) {
            Marker marker = googleMap.addMarker(
                    new MarkerOptions().position(ssList.get(i).toLatLng()).title("").visible(false));
            _markers.add(marker);
        }
        findBestBound(googleMap, _markers, width, height, ssingAtCenter);
    }

    public static void moveCamera(GoogleMap googleMap, Ssing ssing, float _zoom) {
        LatLng _loc = ssing.toLatLng();
        CameraPosition cameraPosition = new CameraPosition.Builder().target(_loc).zoom(_zoom).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public static void findBestBound(GoogleMap googleMap, ArrayList<Marker> _markers, int w, int h, Ssing ss) {
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
            if(ss!=null) MapUtil.moveCamera(googleMap, ss, myzoom);
        }
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

