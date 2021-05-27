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
import com.jason.moment.R;

import java.util.ArrayList;

public class MapUtil {
    private static String TAG = "MapUtil";
    public static boolean nomarker = false;
    public static boolean notrack = false;
    public static boolean satellite = false;

     static int colors[] = {
            Color.RED,
            Color.BLUE,
            Color.CYAN,
            Color.WHITE,
            Color.BLACK,
            Color.YELLOW,
            Color.DKGRAY,
            Color.GREEN,
            Color.LTGRAY
    };

    public static ArrayList<Marker> markers = null;
    public static void initialize() {
        markers = new ArrayList<Marker>();
    }
    public static void moveCamera(GoogleMap googleMap, MyActivity myactivity, float _zoom) {
        LatLng cur_loc = myactivity.toLatLng();
        CameraPosition cameraPosition = new CameraPosition.Builder().target(cur_loc).zoom(_zoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public static void drawStartMarker(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list.size()==0) return;
        float color =  Config._marker_start_color;
        LatLng ll = new LatLng(list.get(0).latitude, list.get(0).longitude);
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("출발")
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .draggable(true)
                .visible(true)
                .snippet("출발"));
        markers.add(marker);
    }

    public static void drawEndMarker(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list.size()==0) return;
        float color =  Config._marker_end_color;
        LatLng ll = new LatLng(list.get(list.size()-1).latitude, list.get(list.size()-1).longitude);
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("종료")
                .icon(BitmapDescriptorFactory.defaultMarker(color))
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
            float color =  Config._marker_color;

            String title = StringUtil.getDateTimeString(list.get(i));
            /* drawMarkers 호출시 StartMarker/EndMarker별도로 호출함 */
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

//                    Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
//                            .icon(BitmapDescriptorFactory.defaultMarker(color))
//                            .draggable(true)
//                            .visible(true)
//                            .alpha(Config._marker_alpha)
//                            .snippet(""+interval + unitstr));

                    Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                            .icon(BitmapDescriptorFactory.fromResource(Config._marker_icon))
                            .draggable(true)
                            .visible(true)
                            .alpha(Config._marker_alpha)
                            .snippet(""+interval + unitstr));
                    markers.add(marker);
                }
            }
        }
    }



    public static void drawTrackInRange(Context context, GoogleMap map, ArrayList<MyActivity> latLngArrayList, int start, int end) {
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
            e.printStackTrace();
            Log.e(TAG,"--" + e);
        }
        int color = colors[color_inx];
        drawTrack(map,latLngArrayListInRange,color,width);
    }

    public static void drawTrack(Context context, GoogleMap map, ArrayList<MyActivity> myActivityArrayList) {
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

    static Polyline polyLine_previous = null;
    public static void drawTrack(GoogleMap map, ArrayList<LatLng> latLngArrayList,int color, int width) {
        if(latLngArrayList == null) return;

        if(polyLine_previous!=null) polyLine_previous.remove();


        Log.d(TAG,"-- color:" + color);
        Log.d(TAG,"-- width:" + width);

        PolylineOptions plo = new PolylineOptions();
        plo.color(color);
        Polyline polyLine = map.addPolyline(plo);
        if(width<0) width=10;
        polyLine.setWidth(width);
        polyLine.setPoints(latLngArrayList);

        polyLine_previous = polyLine;
    }

    public static void DRAW(Context _ctx, GoogleMap googleMap, ArrayList<Marker> _markers, Display display, MyActivity lastActivity, ArrayList<MyActivity>mActivityList) {
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
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics( metrics );
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        boolean got_bound_wo_error = false;
        int try_cnt = 0;

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


    public static void doBoundBuild(GoogleMap gmap, ArrayList<Marker> _markers, int width, int height) throws Exception {
        if(_markers.size()==0) return;

        LatLngBounds.Builder builder= new LatLngBounds.Builder();
        for (Marker marker : _markers) {
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



}
