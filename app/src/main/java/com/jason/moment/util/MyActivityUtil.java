package com.jason.moment.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
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

import static com.jason.moment.util.Config.mediaStorageDir4csv;

public class MyActivityUtil {
    private static String TAG = "MyActivityUtil";
    private static File mediaStorageDir;
    private static File mediaStorageDir4csv;
    private static int  _default_ext;
    private static String _default_extension;
    private static boolean _default_reverse_order;

    static {
        initialize();
    }

    public static void initialize() {
        mediaStorageDir = Config.mediaStorageDir;
        mediaStorageDir4csv = Config.mediaStorageDir4csv;
        _default_ext = Config._default_ext;
        _default_extension = (_default_ext==Config._csv)? ".csv" : ".mnt";
        _default_reverse_order = true;
    }

    public static ArrayList<MyActivity> deserializeFromCSV(File file) {
        ArrayList<MyActivity> mal = new ArrayList<MyActivity>();
        try(BufferedReader in = new BufferedReader(new FileReader(file))) {
            String str;
            String head = in.readLine();

            while ((str = in.readLine()) != null) {
                System.out.println(str);
                String[] tokens = str.split(",");

                double latitude = Double.parseDouble(tokens[0]);
                double longitude = Double.parseDouble(tokens[1]);
                String cr_date = tokens[2];
                String cr_time = tokens[3];

                MyActivity ma = new MyActivity(latitude, longitude, cr_date, cr_time);
                mal.add(ma);
            }
        }
        catch (IOException e) {
            System.out.println("File Read Error");
        }
        return mal;
    }

    public static ArrayList<MyActivity> deserializeFromCSV(String fileName) {
        return deserializeFromCSV(new File(mediaStorageDir4csv, fileName));
    }

    public static Date getActivityTime(MyActivity ma) {
        return ma.toDate();
    }

    public static void serializeIntoCSV(ArrayList<MyActivity> list, String fileName) {
        if(list == null) return;
        if(list.size()==0) return;
        if(!mediaStorageDir4csv.exists()) mediaStorageDir4csv.mkdirs();

        try {
            File f = new File(mediaStorageDir4csv, fileName);
            FileWriter file = new FileWriter(f);
            BufferedWriter output = new BufferedWriter(file);
            Log.e(TAG, "-- **** CSV Activity file: " + fileName);

            System.out.println(f.getAbsolutePath());

            output.write("x,y,d,t\n");
            for(int i=0;i<list.size();i++ ) {
                MyActivity a = list.get(i);
                output.write("" + a.latitude);
                output.write("," + a.longitude);

                Date d = getActivityTime(a);
                String crd = com.jason.moment.util.StringUtil.DateToString(d, "yyyy/MM/dd");
                String crt = com.jason.moment.util.StringUtil.DateToString(d, "HH:mm:ss");
                output.write("," + crd + "," + crt + "\n");
                output.flush();
            }
            output.close();
        }catch(Exception e) {
            e.getStackTrace();
        }
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

    public static ArrayList<MyActivity> deserializeFromJason(String fileName) {
        return null;
    }

    public static void serialize(ArrayList<MyActivity> list, String _filename) {
        String filename = _filename;
        if(filename.endsWith(".mnt") || filename.endsWith(".csv")) {
            filename =  filename.substring(0,filename.length()-4);
        }
        if(_default_ext == Config._csv) {
            serializeIntoCSV(list,filename + ".csv");
        }else if(_default_ext == Config._ser) {
            serializeIntoMnt(list,filename + ".mnt");
        }
    }

    public static void serializeIntoMnt(ArrayList<MyActivity> list, String fileName) {
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

    public static ArrayList<MyActivity> deserialize(File file) {
        if(_default_ext == Config._csv) {
            return deserializeFromCSV(file);
        }else if(_default_ext == Config._ser) {
            return deserializeActivityFromMnt(file);
        } else return null;
    }

    public static ArrayList<MyActivity> deserializeActivityFromMnt(File file) {
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
                    Log.d(TAG, "-- File ("+ file.getAbsolutePath() +") corrupted !!!!");
                    file.delete();
                    Log.d(TAG, "-- File ("+ file.getAbsolutePath() +") deleted  !!!!");
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
        File[] flist=null;
        if(_default_ext==Config._csv) flist = mediaStorageDir4csv.listFiles(fnf);
        else if(_default_ext==Config._ser) flist = mediaStorageDir.listFiles(fnf);

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
        File[] flist  = (_default_ext==Config._ser)? mediaStorageDir.listFiles(fnf) : mediaStorageDir4csv.listFiles(fnf);
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

        File[] files  = null;
        files = (_default_ext==Config._ser)? mediaStorageDir.listFiles(fnf) : mediaStorageDir4csv.listFiles(fnf);
        if (files == null) return null;
        if(reverse_order) Arrays.sort(files, Collections.reverseOrder());
        else Arrays.sort(files);
        return files;
    }

    public static File[] getOnlyActivityFiles() {
        File files[] = getAllFiles();
        if (files==null) return null;
        if(files.length==0) return null;

        ArrayList<File> afiles = new ArrayList();
        for(int i=0;i<files.length;i++) {
            if(files[i].getName().contains("_")) afiles.add(files[i]);
        }
        if(afiles.size()>0) {
            File result[] = new File[afiles.size()];
            for(int i=0;i<afiles.size();i++) result[i] = afiles.get(i);
            return result;
        }
        return null;
    }

    public static File[] getOnlyDayFiles() {
        File files[] = getAllFiles();
        ArrayList<File> afiles = new ArrayList();
        for(int i=0;i<files.length;i++) {
            if(!files[i].getName().contains("_")) afiles.add(files[i]);
        }
        if(afiles.size()>0) {
            File result[] = new File[afiles.size()];
            for(int i=0;i<afiles.size();i++) result[i] = afiles.get(i);
            return result;
        }
        return null;
    }


    public static File[] getAllFiles() {
        return getFiles(_default_extension, _default_reverse_order);
    }

    public static String renameExt(File _file, String ext) {
        return _file.getName().substring(0, _file.getName().length()-4) + "." + ext;
    }

    public static File[] getFiles(int type) {
        if(type == Config._file_type_activity) return getOnlyActivityFiles();
        else if(type == Config._file_type_day) return getOnlyDayFiles();
        else return getAllFiles();
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

    public static double MinPerKm(ArrayList<MyActivity> list) {
        return MinPerMeasure(list,Config.perKM);
    }

    public static double MinPerMile(ArrayList<MyActivity> list) {
        return MinPerMeasure(list,Config.perMile);
    }

    public static double MinPer1Km(ArrayList<MyActivity> list) {
        return MinPerMeasure(list,Config.per1KM);
    }

    public static double MinPer1Mile(ArrayList<MyActivity> list) {
        return MinPerMeasure(list,Config.per1Mile);
    }

    public static double getMinPerKm(Date start, Date end, double km) {
        long dur_sec = (end.getTime() - start.getTime())/1000;
        long dur_min = dur_sec/60;
        double minpk = (double)(dur_min / km);
        return minpk;
    }

    public static double getMinPerKm(MyActivity start, MyActivity end, double dist) {
        return getMinPerKm(start.toDate(), end.toDate(), dist/1000);
    }

    public static double getMinPerKm(ArrayList<MyActivity> list) {
        if(list==null) return 0;
        if(list.size() == 0 || list.size() == 1) return 0;
        double dist = getTotalDistanceInDouble(list);
        return getMinPerKm(list.get(0), list.get(list.size()-1), dist);
    }

    public static double MinPerMeasure(ArrayList<MyActivity> list, int type) {
        if(list==null) return 0;
        if(list.size() == 0 || list.size() == 1) return 0;

        double targetDist = 0;
        double unitMeasure = 1000;  //km
        switch(type) {
            case Config.perKM:
                targetDist = Double.MAX_VALUE;
                break;
            case Config.perMile:
                targetDist = Double.MAX_VALUE;
                unitMeasure = 1609.344;
                break;
            case Config.per1KM:
                targetDist = 1000;
                break;
            case Config.per1Mile:
                targetDist = 1609.344;
                unitMeasure = 1609.344;
                break;
        }

        double dist_meter = 0;
        int pos=0;
        for(int i=list.size()-1; i> 0; i--) {
            double bef_lat = list.get(i-1).latitude;
            double bef_lon = list.get(i-1).longitude;
            double aft_lat = list.get(i).latitude;
            double aft_lon = list.get(i).longitude;

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
            pos = i-1;
            if (dist_meter >= targetDist ) {
                break;
            }
        }
        long t1 = list.get(list.size()-1).toDate().getTime();
        long t2 = list.get(pos).toDate().getTime();
        return( (double)(t1-t2)/1000/60.0    /  (double)(dist_meter/unitMeasure) );
    }

    public static long durationInSeconds(MyActivity before, MyActivity after) {
        return (after.toDate().getTime() - before.toDate().getTime()) / 1000;
    }

    public static int durationInSeconds(ArrayList<MyActivity> list) {
        if(list==null) return 0;
        if(list.size() == 0 || list.size() == 1) return 0;
        MyActivity after = list.get(list.size()-1);
        MyActivity before = list.get(0);
        return((int)durationInSeconds(before, after));
    }

    public static double getTotalDistanceInDouble(ArrayList<MyActivity> list) {
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

}
