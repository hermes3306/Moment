package com.jason.moment.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyActivityUtil {
    private static String TAG = "MyActivityUtil";
    private static File mediaStorageDir = null;
    private static String _default_extension = ".mnt";
    private static boolean _default_reverse_order = true;

    static {
        mediaStorageDir = Config.mediaStorageDir;
    }

    public static File getMediaStorageDirectory() {
        return mediaStorageDir;
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
                obj.put("latitude", ma.latitude);
                obj.put("longitude", ma.longitude);
                obj.put("crdate", ma.cr_date);
                obj.put("crtime", ma.cr_time);
                jsonArr.put(obj);
            }

            JSONObject jobj = new JSONObject();
            jobj.put("activities", jsonArr);

            Writer output = null;
            File jfile = new File(mediaStorageDir,fileName);
            output = new BufferedWriter(new FileWriter(jfile));
            output.write(jobj.toString());
            output.close();

            Log.d(TAG, jobj.toString());
        }catch(Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }

    public static void deserializeFromJason(ArrayList<MyActivity> list, int start, int end, String fileName) {
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

            for(int i=0;i< list.size();i++) {
                MyActivity ma = list.get(i);
                out.writeObject(ma);
            }
            Log.e(TAG, "-- **** Total " + list.size() + "activities saved in to " + file.toString());
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

    public static File[] getFilesStartsWith(final String prefix, boolean reverserorder) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().startsWith(prefix);
            }
        };
        File[] flist  = mediaStorageDir.listFiles(fnf);
        if(reverserorder) Arrays.sort(flist, Collections.<File>reverseOrder());
        else Arrays.sort(flist);
        return flist;
    }

    public static File[] getFilesEndsWith(final String postfix, boolean reverserorder) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(postfix);
            }
        };
        File[] flist  = mediaStorageDir.listFiles(fnf);
        if(reverserorder) Arrays.sort(flist, Collections.<File>reverseOrder());
        else Arrays.sort(flist);
        return flist;
    }

    public static File[] getFiles(final String extension, boolean reverse_order) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(extension);
            }
        };

        File[] files  = mediaStorageDir.listFiles(fnf);
        if (files == null) return null;
        if(reverse_order) Arrays.sort(files, Collections.reverseOrder());
        else Arrays.sort(files);

        return files;
    }

    public static File[] getFiles() {
        return getFiles(_default_extension, _default_reverse_order);
    }

    public static final Comparator<MyActivity> ALPHA_COMPARATOR  = new Comparator<MyActivity>() {
        private final Collator sCollator = Collator.getInstance();
        public int compare(MyActivity object1, MyActivity object2) {
            return sCollator.compare(StringUtil.getDateTimeString(object1), StringUtil.getDateTimeString(object1));
        }
    };

    public static String dateTimefmt(MyActivity a) {
        // yyyy/MM/dd_HH:mm:ss
        return a.cr_time + "_" + a.cr_time;
    }


    public static String getTimeStr(ArrayList<MyActivity> list, int pos) {
        if(list == null) return null;

        Date date = StringUtil.StringToDate(list.get(pos));
        Log.d(TAG, "-- Time of loc position["+ pos +"]:" + date);

        String date_str = StringUtil.DateToString(date, "M월 d일 (E) H시 m분");
        return date_str;
    }

    public static Date getStartTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(dateTimefmt(list.get(0)), "yyyy/MM/dd_HH:mm:ss");
        return date;
    }

    public static Date getEndTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(dateTimefmt(list.get(list.size()-1)), "yyyy/MM/dd_HH:mm:ss");
        return date;
    }

    public static String getEndTime(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()-1 <0) return null;

        Date date = StringUtil.StringToDate(dateTimefmt(list.get(list.size()-1)), "yyyy/MM/dd_HH:mm:ss");
        String date_str = StringUtil.DateToString(date, "M월d일(E)H시m분s초");
        return date_str;
    }


}
