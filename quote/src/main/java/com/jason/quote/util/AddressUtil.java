package com.jason.quote.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressUtil {
    public static String TAG = "AddressUtil";
    public static String getAddress(final Context _ctx, LatLng ll) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ll.latitude, ll.longitude,1);
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }

        String addinfo = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            addinfo = addresses.get(0).getAddressLine(0);
            if(addinfo != null) addinfo = addinfo.replace("대한민국", "");
//            Log.d(TAG, "--getAddress[0] = " + addinfo);
//            String dong = addresses.get(0).getSubThoroughfare() ;
//            Log.d(TAG, "--getSubThroughFare = " + dong);
//            Log.d(TAG, "--Phone = " + addresses.get(0).getPhone());
//            Log.d(TAG, "--Url = " + addresses.get(0).getUrl());
//            Log.d(TAG, "--getPremises = " + addresses.get(0).getPremises());
        }
        return addinfo;
    }

    public static String getAddress(final Context _ctx, Location loc) {
        LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
        return getAddress(_ctx, ll);
    }

    public static LatLng getLatLangFromAddress(Context _ctx, String strAddress){
        Geocoder coder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address == null) {
                return new LatLng(-10000, -10000);
            }
            Address location = address.get(0);
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            return new LatLng(-10000, -10000);
        }
    }


    public static String getAddress(final Context _ctx, double latitude, double longitude) {
        LatLng ll = new LatLng(latitude, longitude);
        return getAddress(_ctx, ll);
    }

    public static String getAddress(final Context _ctx, MyActivity ma) {
        LatLng ll = new LatLng(ma.latitude, ma.longitude);
        return getAddress(_ctx, ll);
    }

    public static String getAddressDong(final Context _ctx, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude,1);
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }

        String addinfoDong = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "-- No Addresses found !!");
            return "";
        }else {
            addinfoDong = addresses.get(0).getThoroughfare() +
                    (addresses.get(0).getPremises()==null?"":" " + addresses.get(0).getPremises());
        }
        if(addinfoDong.contains("null")) {
            addinfoDong = addresses.get(0).getAddressLine(0);
        }
        return addinfoDong;
    }

    public static String getAddressDong(final Context _ctx, MyActivity ma) {
        return getAddressDong(_ctx, ma.latitude, ma.longitude);
    }

    public static ArrayList<String> getAllAddresses(final Context _ctx, MyActivity ma) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ma.latitude, ma.longitude,1);
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }

        String addinfo = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            ArrayList<String> list = new ArrayList<String>();
            for(int i=0;i<addresses.size();i++) {
                list.add(addresses.get(i).getAddressLine(0));
            }
            return list;
        }
        return null;
    }

}
