package com.jason.moment.util;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

public class FileUtil {
    public static File[] getFilesStartsWith(File folder, final String prefix, boolean reverserorder) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().startsWith(prefix);
            }
        };
        File[] flist  = folder.listFiles(fnf);
        if(flist==null) return null;
        if(flist.length==0) return flist;

        if(reverserorder) Arrays.sort(flist, Collections.reverseOrder());
        else Arrays.sort(flist);
        return flist;
    }

    public static File[] getFilesEndsWith(File folder, final String postfix, boolean reverserorder) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(postfix);
            }
        };
        File[] flist  = folder.listFiles(fnf);
        if(reverserorder) Arrays.sort(flist, Collections.reverseOrder());
        else Arrays.sort(flist);
        return flist;
    }

    public static File[] getFiles(File folder, final String extension, boolean reverse_order) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(extension);
            }
        };

        File[] files  = folder.listFiles(fnf);
        if (files == null) return null;
        if(reverse_order) Arrays.sort(files, Collections.reverseOrder());
        else Arrays.sort(files);

        return files;
    }

    public static void deleteFile(String file) {
        File f = new File(file);
        try {
           f.delete();
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.d("FileUtil","Err:" + sw.toString());
        }
    }

}

