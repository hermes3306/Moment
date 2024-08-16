package com.jason.moment.util;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.jason.moment.util.db.MyLoc;

public class LocationUtil {
    private final String TAG="LocationUtil";
    private static boolean first_called=true;
    private static Location last_location=null;
    private static long last_pk=-1;

    private static LocationUtil locationUtil=null;

    public long get_last_pk() {
        return last_pk;
    }

    public static LocationUtil getInstance() {
        if(locationUtil==null) locationUtil= new LocationUtil();
        return locationUtil;
    }

    public Location last_location() {
        return last_location;
    }

    public LatLng getLast_location(Context _ctx) {
        if(first_called) {
            MyActivity last = MyLoc.getInstance(_ctx).lastActivity();
            if(last !=null) return new LatLng(last.latitude, last.longitude);
            else return null;
        } else if(last_location != null) return new LatLng(last_location.getLatitude(), last_location.getLongitude());
        else return null;
    }

    public void onLocationChanged(Context context, Location location) {
        double dist;
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        if(first_called || last_location==null) {
            dist = 0;
            last_location = location;
            last_pk = MyLoc.getInstance(context).ins(location.getLatitude(), location.getLongitude());

            // found critical bug for first_called processing
            if(first_called) first_called = false;
            return;
        }else {
            dist = CalDistance.dist(last_location.getLatitude(), last_location.getLongitude(), location.getLatitude(), location.getLongitude());
            if(dist < Config._loc_distance) return;
            last_pk = MyLoc.getInstance(context).ins(location.getLatitude(), location.getLongitude());
            last_location = location;
        }
    }
}
