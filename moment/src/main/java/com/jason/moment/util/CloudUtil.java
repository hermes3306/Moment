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
import java.io.FileNotFoundException;
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

public class CloudUtil {
    static String TAG = "CloudUtil";
    private static final int BUFFER_SIZE = 4096;

    private static CloudUtil _instance=null;
    public static CloudUtil getInstance() {
        if(_instance==null) _instance = new CloudUtil();
        return _instance;
    }

    public static String getUrlContent(String urlstr) throws IOException {
        Log.d(TAG, "-- urlstr to download:" + urlstr);
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlstr);

            BufferedReader in;
            in = new BufferedReader(
                    new InputStreamReader(
                            url.openStream()));

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

    private static void _download(String fileURL, File saveDir) {
        if(!C.cloud_dn) return;
        Log.e(TAG, "-- Download URL:" + fileURL);
        String fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);
        File f = new File(saveDir, fileName);
        if(f.exists()) {
            Log.e(TAG, "-- File ("+ fileName +") exists already!");
            if(fileName.startsWith(DateUtil.today())) {
                Log.e(TAG, "-- File ("+ fileName +") over written!");
            } else return;
        }

        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String disposition = httpConn.getHeaderField("Content-Disposition");
                String contentType = httpConn.getContentType();
                int contentLength = httpConn.getContentLength();

                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10,
                                disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1
                    );
                }

                Log.e(TAG, "--Content-Type = " + contentType);
                Log.e(TAG, "--Content-Disposition = " + disposition);
                Log.e(TAG, "--Content-Length = " + contentLength);
                Log.e(TAG, "--fileName = " + fileName);

                // opens input stream from the HTTP connection
                InputStream inputStream = httpConn.getInputStream();
                File savefile = new File(saveDir, fileName);
                // opens an output stream to save into file
                FileOutputStream outputStream = new FileOutputStream(savefile);

                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                Log.e(TAG, "--File downloaded");
            } else {
                Log.e(TAG, "--No file to download. Server replied HTTP code: " + responseCode);
            }
            httpConn.disconnect();
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG, "ERR:" + sw.toString());
        }
    }

    public void DownloadAll(final Context context, final int ftype) {
        if(!C.cloud_dn) return;
        new AsyncTask<Void,Void,Void>() {
            String listUrl = null;
            String listOfFiles = null;
            String[] linesOfFiles = null;
            String[] fileURL = null;
            File saveDir = null;

            final ProgressDialog asyncDialog = new ProgressDialog(context);
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

                    asyncDialog.setMax(fileURL.length);
                    for(int i=0;i<fileURL.length;i++) {
                        _download(fileURL[i], saveDir);
                        asyncDialog.setProgress(i);
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
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Downloading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Download success", Toast.LENGTH_LONG).show();
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

    public void UploadAll(final Context context, int ftype) {
        if(!C.cloud_up) return;
        final String _serverUrl = Config._uploadURL;
        // Pop Up a Dialog
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
                File[] flist = null;
                if (ftype == Config._csv) {
                    flist = Config.mediaStorageDir4csv.listFiles();
                } else if (ftype == Config._ser) {
                    flist = Config.mediaStorageDir4mnt.listFiles();
                } else if (ftype == Config._img) {
                    flist = Config.PIC_SAVE_DIR.listFiles();
                } else if (ftype == Config._mov) {
                    flist = Config.MOV_SAVE_DIR.listFiles();
                } else if (ftype == Config._mp3) {
                    flist = Config.MP3_SAVE_DIR.listFiles();
                }

                if (flist == null || flist.length == 0) {
                    Log.e(TAG, "No files found to upload");
                    return null;
                }

                asyncDialog.setMax(flist.length);

                for (int i = 0; i < flist.length; i++) {
                    File file = flist[i];
                    attachmentName = attachmentFileName = file.getName();

                    HttpURLConnection connection = null;
                    DataOutputStream outputStream = null;
                    InputStream inputStream = null;

                    try {
                        URL url = new URL(_serverUrl);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setUseCaches(false);
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setConnectTimeout(15000);
                        connection.setReadTimeout(15000);
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Connection", "Keep-Alive");
                        connection.setRequestProperty("Cache-Control", "no-cache");
                        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                        outputStream = new DataOutputStream(connection.getOutputStream());

                        outputStream.writeBytes(twoHyphens + boundary + crlf);
                        outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + crlf);
                        outputStream.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName()) + crlf);
                        outputStream.writeBytes("Content-Transfer-Encoding: binary" + crlf);
                        outputStream.writeBytes(crlf);

                        FileInputStream fileInputStream = new FileInputStream(file);
                        int bytesRead;
                        byte[] buffer = new byte[4096];
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        fileInputStream.close();

                        outputStream.writeBytes(crlf);
                        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                        outputStream.flush();

                        int responseCode = connection.getResponseCode();
                        Log.d(TAG, "Server response code: " + responseCode);

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            inputStream = connection.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            String line;
                            StringBuilder response = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            Log.d(TAG, "Server response: " + response.toString());
                        } else {
                            Log.e(TAG, "Server error response: " + connection.getResponseMessage());
                        }

                        asyncDialog.setProgress(i + 1);
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        Log.e(TAG, "Error uploading file " + file.getName() + ": " + sw.toString());
                    } finally {
                        try {
                            if (outputStream != null) outputStream.close();
                            if (inputStream != null) inputStream.close();
                        } catch (IOException ignored) {}
                        if (connection != null) connection.disconnect();
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
        // Pop Up a Dialog
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

                    // request 준비
                    HttpURLConnection httpUrlConnection = null;
                    URL url = new URL(_serverUrl);
                    httpUrlConnection = (HttpURLConnection) url.openConnection();
                    httpUrlConnection.setUseCaches(false);
                    httpUrlConnection.setDoInput(true);
                    httpUrlConnection.setDoOutput(true);
                    httpUrlConnection.setConnectTimeout(15000);

                    httpUrlConnection.setRequestMethod("POST");
                    httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                    httpUrlConnection.setRequestProperty(
                            "Content-Type", "multipart/form-data;boundary=" + this.boundary);


                    // content wrapper시작
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
                        //Log.e(TAG, "readcount:" + readcount);
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

                    //==============받기===============
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
