package com.jason.moment.util;

import android.util.Log;

public class MyMediaInfo {
    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCr_datetime() {
        return cr_datetime;
    }

    public String getMo_datetime() {
        return mo_datetime;
    }

    String name;

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    long key;
    String memo;
    double latitude;
    double longitude;
    String cr_datetime;
    String mo_datetime;

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    String place;
    String grade;
    String address;

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCr_datetime(String cr_datetime) {
        this.cr_datetime = cr_datetime;
    }

    public void setMo_datetime(String mo_datetime) {
        this.mo_datetime = mo_datetime;
    }

    public void print() {
        Log.d(TAG, "Key:" + getKey());
        Log.d(TAG, "Name:" + getName());
        Log.d(TAG, "Memo:" + getMemo());
        Log.d(TAG, "Address:" + getAddress());
        Log.d(TAG, "Place:" + getPlace());
        Log.d(TAG, "Grade:" + getGrade());
        Log.d(TAG, "Latitude:" + getLatitude());
        Log.d(TAG, "Longitude:" + getLongitude());
        Log.d(TAG, "Created:" + getCr_datetime());
        Log.d(TAG, "Modified:" + getMo_datetime());
    }

    String TAG = "MyMediaInfo";


}
