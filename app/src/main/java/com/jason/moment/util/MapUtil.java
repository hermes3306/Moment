package com.jason.moment.util;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapUtil {
    private static String TAG = "MapUtil";

     static int colors[] = {
            Color.RED,
            Color.BLACK,
            Color.CYAN,
            Color.WHITE
    };



    public static void drawTrackInRange(Context context, GoogleMap map, ArrayList<MyActivity> latLngArrayList, int start, int end) {
        if(latLngArrayList == null) return;
        ArrayList<LatLng> latLngArrayListInRange = new ArrayList<>();
        for(int i=start; i < end; i++) {
            latLngArrayListInRange.add(new LatLng(latLngArrayList.get(i).latitude, latLngArrayList.get(i).longitude));
        }

        int color_inx = Config.getIntPreference(context, "track_color");
        int width = Config.getIntPreference(context, "track_width");
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



}
