package com.jason.moment.util;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MyActivityUtil {
    private static String TAG = "MyActivityUtil";
    private static File mediaStorageDir = null;

    static {
        mediaStorageDir = Config.mediaStorageDir;
    }

    public static void serializeIntoJason(ArrayList<MyActivity> list, int start, int end, String fileName) {
        if(start <0 || end >= list.size()) return;
        if(!mediaStorageDir.exists()) mediaStorageDir.mkdirs();
        File file = new File(mediaStorageDir, fileName);
        Log.d(TAG, " -- **** Activity Jason file: " + file.toString());
        try {
            JSONArray jsonArr = new JSONArray();
            for(int i=start;i<= end;i++) {
                MyActivity ma = list.get(i);
                JSONObject obj = new JSONObject();
                obj.put("lat", ma.latitude);
                obj.put("lon", ma.longitude);
                obj.put("crdate", ma.cr_date);
                obj.put("crtime", ma.cr_time);
                jsonArr.put(obj);
            }
            JSONObject jobj = new JSONObject();
            jobj.put("activities", jsonArr);
            FileWriter fwriter = new FileWriter(new File(mediaStorageDir,fileName));
            fwriter.write(jobj.toString());
            Log.d(TAG, jobj.toString());
        }catch(Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }

    public static void serialize(ArrayList<MyActivity> list, String fileName) {
        if(list == null) return;
        if(!mediaStorageDir.exists()) mediaStorageDir.mkdirs();

        File file = new File(mediaStorageDir, fileName);
        Log.e(TAG, "-- **** Activity file: " + file.toString());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(bos);

            for(int i=0;i<= list.size();i++) {
                MyActivity ma = list.get(i);
                out.writeObject(ma);
            }
            out.close();
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    public static ArrayList<MyActivity> deserializeActivity(File file) {
        if(file == null)  return null;
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
                    if(list != null) return list;
                }
            } while(ma != null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (bis !=null) in.close();
                if (fis !=null) fis.close();

                if(list.size()==0) {
                    Log.d(TAG, "File ("+ file.getAbsolutePath() +") corrupted !!!!");
                    file.delete();
                    Log.d(TAG, "File ("+ file.getAbsolutePath() +") deleted  !!!!");
                }
            }catch(Exception e) {}
        }
        return list;
    }



}
