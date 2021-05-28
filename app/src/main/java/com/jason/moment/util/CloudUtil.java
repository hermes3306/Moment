package com.jason.moment.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
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
import java.net.HttpURLConnection;
import java.net.URL;

public class CloudUtil {
    static String TAG = "CloudUtil";
    private static final int BUFFER_SIZE = 4096;

    ////// ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
    /////  no return
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
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void download(String fileURL, File saveDir)
            throws IOException {
        Log.e(TAG, "-- Download URL:" + fileURL);
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
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
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            Log.e(TAG,"--Content-Type = " + contentType);
            Log.e(TAG,"--Content-Disposition = " + disposition);
            Log.e(TAG,"--Content-Length = " + contentLength);
            Log.e(TAG,"--fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            File savefile = new File(saveDir, fileName );
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(savefile);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            Log.e(TAG,"--File downloaded");
        } else {
            Log.e(TAG,"--No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    public void DownloadAll(final Context context, final int ftype) {
        new AsyncTask<Void,Void,Void>() {
            String listUrl = null;
            String listOfFiles = null;
            String linesOfFiles[] = null;
            String fileURL[] = null;
            File saveDir = null;

            ProgressDialog asyncDialog = new ProgressDialog(context);
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if(ftype==Config._csv) {
                        listUrl = Config._listCSVFiles;
                    } else if(ftype==Config._ser) {
                        listUrl = Config._listSerFiles;
                    }else if(ftype==Config._img) {
                        listUrl = Config._listImageFiles;
                    }
                    Log.d(TAG,"-- list url:" + listUrl);

                    try {
                        listOfFiles = getUrlContent(listUrl);
                    }catch(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,"-- ERR:" +e.toString());
                    }
                    Log.d(TAG,"-- listOfFiles:" + listOfFiles);

                    linesOfFiles = listOfFiles.split("<br>");
                    fileURL = new String[linesOfFiles.length] ;
                    for(int i=0;i< linesOfFiles.length;i++) {
                        fileURL[i] = Config._serverURL + Config._serverFolder + "/" + linesOfFiles[i];
                        Log.d(TAG, "-- fileUrl:" + fileURL[i]);
                    }

                    if(ftype==Config._img) saveDir = Config.mediaStorageDir4pic;
                    else saveDir = (Config._default_ext==Config._csv)? Config.mediaStorageDir4csv : Config.mediaStorageDir4mnt;

                    asyncDialog.setMax(fileURL.length);
                    for(int i=0;i<fileURL.length;i++) {
                        download(fileURL[i], saveDir);
                        asyncDialog.setProgress(i);
                    }
                }catch(Exception e) {
                    Log.e(TAG, "--" + e.toString());
                    e.printStackTrace();
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

    public void UploadAll(final Context context, int ftype) {
        final String _serverUrl = Config._uploadURL;
        // Pop Up a Dialog
        new AsyncTask<Void,Void,Void>() {
            ProgressDialog asyncDialog = new ProgressDialog(context);
            HttpURLConnection urlConnection = null;
            String attachmentName = null;
            String attachmentFileName = null;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            @Override
            protected Void doInBackground(Void... voids) {
                File flist[] = null;
                if(ftype==Config._csv) {
                    flist = Config.mediaStorageDir4csv.listFiles();
                } else if(ftype==Config._ser) {
                    flist = Config.mediaStorageDir4mnt.listFiles();
                }else if(ftype==Config._img) {
                    flist = Config.PIC_SAVE_DIR.listFiles();
                }

                asyncDialog.setMax(flist.length);

                for (int i = 0; i < flist.length; i++) {

                    File file = flist[i];
                    attachmentName = attachmentFileName = flist[i].getName();

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
                        request.writeBytes("Content-Type: " + httpUrlConnection.guessContentTypeFromName(file.getName()) + this.crlf);
                        request.writeBytes("Content-Transfer-Encoding: binary" + this.crlf);
                        request.writeBytes(this.crlf);
                        request.flush();



                        OutputStream out = httpUrlConnection.getOutputStream();
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int readcount = 0;
                        while ((readcount = fis.read(buffer)) != -1) {
                            //Log.e(TAG, "readcount:" + readcount);
                            out.write(buffer, 0, readcount);
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

                        asyncDialog.setProgress(i);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
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

    public void Upload(final Context context, String filename) {
        final String _serverUrl = Config._uploadURL;
        // Pop Up a Dialog
        new AsyncTask<Void,Void,Void>() {
            ProgressDialog asyncDialog = new ProgressDialog(context);
            HttpURLConnection urlConnection = null;
            String attachmentName = null;
            String attachmentFileName = null;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            @Override
            protected Void doInBackground(Void... voids) {
                asyncDialog.setMax(100);
                File file = null;
                file = new File(Config.PIC_SAVE_DIR, filename);
                Log.d(TAG, "Picture File name:" + filename);

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
                    request.writeBytes("Content-Type: " + httpUrlConnection.guessContentTypeFromName(file.getName()) + this.crlf);
                    request.writeBytes("Content-Transfer-Encoding: binary" + this.crlf);
                    request.writeBytes(this.crlf);
                    request.flush();

                    OutputStream out = httpUrlConnection.getOutputStream();
                    FileInputStream fis = new FileInputStream(file);

                    //asyncDialog.setMax( (int) (file.length() / 1024));
                    asyncDialog.setMax( 100 );
                    byte[] buffer = new byte[1024];
                    int readcount = 0;
                    int i=1;
                    while ((readcount = fis.read(buffer)) != -1) {
                        //Log.e(TAG, "readcount:" + readcount);
                        out.write(buffer, 0, readcount);
                        float progress = ((1024 * i) / file.length()) * 100;
                        asyncDialog.setProgress((int)progress);
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
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
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



}
