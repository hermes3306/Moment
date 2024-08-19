package com.jason.moment.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CloudUtil {
    static String TAG = "CloudUtil";
    private static final int BUFFER_SIZE = 4096;

    private static CloudUtil _instance = null;
    public static CloudUtil getInstance() {
        if(_instance == null) _instance = new CloudUtil();
        return _instance;
    }

    public static String getUrlContent(String urlstr) throws IOException {
        Log.d(TAG, "-- urlstr to download:" + urlstr);
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlstr);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        return sb.toString();
    }

    private static File getSaveDirForFile(String fileURL) {
        String fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        switch(extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return Config.PIC_SAVE_DIR;
            case "mp4":
            case "avi":
            case "mov":
                return Config.MOV_SAVE_DIR;
            case "mp3":
            case "wav":
                return Config.MP3_SAVE_DIR;
            case "csv":
                return Config.CSV_SAVE_DIR;
            case "mnt":
            case "ser":
                return Config.MNT_SAVE_DIR;
            case "apk":
                return Config.APK_SAVE_DIR;
            case "json":
            case "jsn":
                return Config.JSN_SAVE_DIR;
            default:
                return (Config._default_ext == Config._csv) ? Config.CSV_SAVE_DIR : Config.MNT_SAVE_DIR;
        }
    }

    private static void _download(String fileURL, File saveDir) {
        if(!C.cloud_dn) return;
        Log.e(TAG, "-- Download URL:" + fileURL);
        String fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);

        File actualSaveDir = getSaveDirForFile(fileURL);
        File f = new File(actualSaveDir, fileName);

        if(!Config._overwrite_when_exist && f.exists()) {
            Log.e(TAG, "-- File ("+ fileName +") exists already!");
            if(fileName.startsWith(DateUtil.today())) {
                Log.e(TAG, "-- File ("+ fileName +") over written!");
            } else return;
        }

        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String disposition = httpConn.getHeaderField("Content-Disposition");
                String contentType = httpConn.getContentType();
                int contentLength = httpConn.getContentLength();

                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                    }
                }

                Log.e(TAG, "--Content-Type = " + contentType);
                Log.e(TAG, "--Content-Disposition = " + disposition);
                Log.e(TAG, "--Content-Length = " + contentLength);
                Log.e(TAG, "--fileName = " + fileName);

                InputStream inputStream = httpConn.getInputStream();
                File savefile = new File(actualSaveDir, fileName);

                savefile.getParentFile().mkdirs();

                FileOutputStream outputStream = new FileOutputStream(savefile);

                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                Log.e(TAG, "--File downloaded to: " + savefile.getAbsolutePath());
            } else {
                Log.e(TAG, "--No file to download. Server replied HTTP code: " + responseCode);
            }
            httpConn.disconnect();
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG, "ERR:" + sw.toString());
        }
    }

    public void DownloadAll(final Context context, final int ftype) {
        if(!C.cloud_dn) return;
        new AsyncTask<Void,Integer,Void>() {
            List<String> allFileURLs = new ArrayList<>();
            final ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Downloading...");
                asyncDialog.setCancelable(false);
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if(ftype == Config._all) {
                        downloadAllTypes();
                    } else {
                        downloadSpecificType(ftype);
                    }

                    asyncDialog.setMax(allFileURLs.size());
                    for(int i = 0; i < allFileURLs.size(); i++) {
                        String fileURL = allFileURLs.get(i);
                        File saveDir = getSaveDirForFile(fileURL);
                        _download(fileURL, saveDir);
                        publishProgress(i + 1);
                    }
                } catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e(TAG,"Err:" + sw.toString());
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                asyncDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();
                Toast.makeText(context, "Download completed", Toast.LENGTH_LONG).show();
            }

            private void downloadAllTypes() throws Exception {
                downloadFilesForType(Config._csv, Config._listCSVFiles);
                downloadFilesForType(Config._ser, Config._listSerFiles);
                downloadFilesForType(Config._img, Config._listImageFiles);
                downloadFilesForType(Config._mov, Config._listMovFiles);
                downloadFilesForType(Config._mp3, Config._listMP3Files);
            }

            private void downloadSpecificType(int type) throws Exception {
                String listUrl = getListUrlForType(type);
                downloadFilesForType(type, listUrl);
            }

            private void downloadFilesForType(int type, String listUrl) throws Exception {
                String listOfFiles = getUrlContent(listUrl);
                String[] linesOfFiles = listOfFiles.split("<br>");
                for(String line : linesOfFiles) {
                    String fileURL = Config._serverURL + Config._serverFolder + "/" + line;
                    allFileURLs.add(fileURL);
                    Log.d(TAG, "-- fileUrl:" + fileURL);
                }
            }

            private String getListUrlForType(int type) {
                switch(type) {
                    case Config._csv: return Config._listCSVFiles;
                    case Config._ser: return Config._listSerFiles;
                    case Config._img: return Config._listImageFiles;
                    case Config._mov: return Config._listMovFiles;
                    case Config._mp3: return Config._listMP3Files;
                    default: return "";
                }
            }
        }.execute();
    }

    public void DownloadAllSilent(final Context context, final int ftype) {
        if(!C.cloud_dn) return;
        new AsyncTask<Void,Void,Void>() {
            String listUrl = null;
            String listOfFiles = null;
            String[] linesOfFiles = null;
            String[] fileURL = null;
            File saveDir = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if(ftype==Config._csv) {
                        listUrl = Config._listCSVFiles;
                    } else if(ftype==Config._ser) {
                        listUrl = Config._listSerFiles;
                    }else if(ftype==Config._img) {
                        listUrl = Config._listImageFiles;
                    }else if(ftype==Config._mov) {
                        listUrl = Config._listMovFiles;
                    }else if(ftype==Config._mp3) {
                        listUrl = Config._listMP3Files;
                    }
                    Log.d(TAG,"-- list url:" + listUrl);

                    try {
                        listOfFiles = getUrlContent(listUrl);
                    }catch(Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        Log.e(TAG,"Err:" + sw.toString());
                    }
                    Log.d(TAG,"-- listOfFiles:" + listOfFiles);

                    linesOfFiles = listOfFiles.split("<br>");
                    fileURL = new String[linesOfFiles.length] ;
                    for(int i=0;i< linesOfFiles.length;i++) {
                        fileURL[i] = Config._serverURL + Config._serverFolder + "/" + linesOfFiles[i];
                        Log.d(TAG, "-- fileUrl:" + fileURL[i]);
                    }

                    if(ftype==Config._img) saveDir = Config.mediaStorageDir4pic;
                    else if(ftype==Config._mov) saveDir = Config.mediaStorageDir4mov;
                    else if(ftype==Config._mp3) saveDir = Config.mediaStorageDir4mp3;
                    else saveDir = (Config._default_ext==Config._csv)? Config.mediaStorageDir4csv : Config.mediaStorageDir4mnt;

                    for(int i=0;i<fileURL.length;i++) {
                        _download(fileURL[i], saveDir);
                    }
                }catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e(TAG,"Err:" + sw.toString());
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Download success", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    public void Download(final File saveDir, final String fileURL) {
        if(!C.cloud_dn) return;
        Log.d(TAG,"-- DownloadAync!!");
        new AsyncTask<Void,Void,Void>() {
            String listOfFiles = null;
            String[] linesOfFiles = null;
            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG,"-- doInBackground/DownloadAync!!");
                try {
                    _download(fileURL, saveDir);
                }catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e(TAG,"Err:" + sw.toString());
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void addFilesToList(List<File> list, File[] files) {
        if (files != null) {
            list.addAll(Arrays.asList(files));
        }
    }

    public void UploadAll(final Context context, int ftype) {
        if(!C.cloud_up) return;
        final String _serverUrl = Config._uploadURL;
        new AsyncTask<Void,Void,Void>() {
            final ProgressDialog asyncDialog = new ProgressDialog(context);
            HttpURLConnection urlConnection = null;
            String attachmentName = null;
            String attachmentFileName = null;
            final String crlf = "\r\n";
            final String twoHyphens = "--";
            final String boundary =  "*****";

            @Override
            protected Void doInBackground(Void... voids) {
                List<File> fileList = new ArrayList<>();

                if (ftype == Config._all) {
                    addFilesToList(fileList, Config.mediaStorageDir4csv.listFiles());
                    addFilesToList(fileList, Config.mediaStorageDir4mnt.listFiles());
                    addFilesToList(fileList, Config.PIC_SAVE_DIR.listFiles());
                    addFilesToList(fileList, Config.MOV_SAVE_DIR.listFiles());
                    addFilesToList(fileList, Config.MP3_SAVE_DIR.listFiles());
                } else {
                    File[] tempFiles = null;
                    switch (ftype) {
                        case Config._csv:
                            tempFiles = Config.mediaStorageDir4csv.listFiles();
                            break;
                        case Config._ser:
                            tempFiles = Config.mediaStorageDir4mnt.listFiles();
                            break;
                        case Config._img:
                            tempFiles = Config.PIC_SAVE_DIR.listFiles();
                            break;
                        case Config._mov:
                            tempFiles = Config.MOV_SAVE_DIR.listFiles();
                            break;
                        case Config._mp3:
                            tempFiles = Config.MP3_SAVE_DIR.listFiles();
                            break;
                    }
                    addFilesToList(fileList, tempFiles);
                }

                File[] flist = fileList.toArray(new File[0]);

                asyncDialog.setMax(flist.length);

                for (int i = 0; i < flist.length; i++) {
                    File file = flist[i];
                    attachmentName = attachmentFileName = flist[i].getName();

                    try {
                        URL serverUrl = new URL(_serverUrl);
                        urlConnection = (HttpURLConnection) serverUrl.openConnection();

                        HttpURLConnection httpUrlConnection = (HttpURLConnection) serverUrl.openConnection();
                        httpUrlConnection.setUseCaches(false);
                        httpUrlConnection.setDoInput(true);
                        httpUrlConnection.setDoOutput(true);
                        httpUrlConnection.setConnectTimeout(15000);

                        httpUrlConnection.setRequestMethod("POST");
                        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                        httpUrlConnection.setRequestProperty(
                                "Content-Type", "multipart/form-data;boundary=" + this.boundary);

                        DataOutputStream request = new DataOutputStream(
                                httpUrlConnection.getOutputStream());

                        request.writeBytes("--" + boundary + this.crlf);
                        request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + this.crlf);
                        request.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName()) + this.crlf);
                        request.writeBytes("Content-Transfer-Encoding: binary" + this.crlf);
                        request.writeBytes(this.crlf);
                        request.flush();

                        OutputStream out = httpUrlConnection.getOutputStream();
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int readcount = 0;
                        while ((readcount = fis.read(buffer)) != -1) {
                            out.write(buffer, 0, readcount);
                        }
                        out.flush();

                        request.writeBytes(this.crlf);
                        request.writeBytes(this.twoHyphens + this.boundary +
                                this.twoHyphens + this.crlf);

                        request.flush();
                        request.close();

                        Log.d(TAG,"-- end of write " + attachmentFileName + " to web server");

                        InputStream is = httpUrlConnection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        StringBuffer sbResult = new StringBuffer();
                        String str = "";
                        while ((str = br.readLine()) != null) {
                            Log.d(TAG, "-- RESPONSE:" + str);
                            sbResult.append(str);
                        }

                        asyncDialog.setProgress(i);

                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        Log.e(TAG,"Err:" + sw.toString());
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Uploading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Uploading success", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    public void Upload(String filepath) {
        if(!C.cloud_up) return;

        final String _serverUrl = Config._uploadURL;
        new AsyncTask<Void,Void,Void>() {
            HttpURLConnection urlConnection = null;
            final String attachmentName = null;
            final String attachmentFileName = null;
            final String crlf = "\r\n";
            final String twoHyphens = "--";
            final String boundary =  "*****";

            @Override
            protected Void doInBackground(Void... voids) {
                File file;
                File folder = null;
                if(filepath.endsWith(Config._csv_ext)) folder = Config.CSV_SAVE_DIR;
                if(filepath.endsWith(Config._mnt_ext)) folder = Config.MNT_SAVE_DIR;
                if(filepath.endsWith(Config._pic_ext)) folder = Config.PIC_SAVE_DIR;
                if(filepath.endsWith(Config._mov_ext)) folder = Config.MOV_SAVE_DIR;
                file = new File(folder, filepath);
                if(!file.exists()) {
                    Log.e(TAG, "Upload File " + filepath + " not found!");
                    return null;
                }
                Log.d(TAG, "Upload File name:" + filepath);

                try {
                    URL serverUrl = new URL(_serverUrl);
                    urlConnection = (HttpURLConnection) serverUrl.openConnection();

                    HttpURLConnection httpUrlConnection = (HttpURLConnection) serverUrl.openConnection();
                    httpUrlConnection.setUseCaches(false);
                    httpUrlConnection.setDoInput(true);
                    httpUrlConnection.setDoOutput(true);
                    httpUrlConnection.setConnectTimeout(15000);

                    httpUrlConnection.setRequestMethod("POST");
                    httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                    httpUrlConnection.setRequestProperty(
                            "Content-Type", "multipart/form-data;boundary=" + this.boundary);

                    DataOutputStream request = new DataOutputStream(
                            httpUrlConnection.getOutputStream());

                    request.writeBytes("--" + boundary + this.crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + this.crlf);
                    request.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName()) + this.crlf);
                    request.writeBytes("Content-Transfer-Encoding: binary" + this.crlf);
                    request.writeBytes(this.crlf);
                    request.flush();

                    OutputStream out = httpUrlConnection.getOutputStream();
                    FileInputStream fis = new FileInputStream(file);

                    byte[] buffer = new byte[1024];
                    int readcount = 0;
                    int i=1;int readcountSum=0;
                    while ((readcount = fis.read(buffer)) != -1) {
                        readcountSum += readcount;
                        out.write(buffer, 0, readcount);
                        float progress = (readcount / file.length()) * 100;
                        Log.d(TAG,"-- file.length(): " + file.length());
                        Log.d(TAG,"-- progress(i): " + progress);
                        Log.d(TAG,"-- progress(i): " + (int)progress);
                        i++;
                    }
                    out.flush();

                    request.writeBytes(this.crlf);
                    request.writeBytes(this.twoHyphens + this.boundary +
                            this.twoHyphens + this.crlf);

                    request.flush();
                    request.close();

                    Log.d(TAG,"-- end of write " + attachmentFileName + " to web server");

                    InputStream is = httpUrlConnection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuffer sbResult = new StringBuffer();
                    String str = "";
                    while ((str = br.readLine()) != null) {
                        Log.d(TAG, "-- RESPONSE:" + str);
                        sbResult.append(str);
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e(TAG,"Err:" + sw.toString());
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();
    }
}