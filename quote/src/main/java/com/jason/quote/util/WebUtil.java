package com.jason.quote.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtil {
    private static final String TAG = "WebUtil";
    private static final int BUFFER_SIZE = 4096;

    public static String getUrlContent(String urlstr) throws IOException {
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


    public static void download(String fileURL, File saveDir) throws IOException{
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
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1
                );
            }

            Log.e(TAG,"--Content-Type = " + contentType);
            Log.e(TAG,"--Content-Disposition = " + disposition);
            Log.e(TAG,"--Content-Length = " + contentLength);
            Log.e(TAG,"--fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            File outfile = new File(saveDir, fileName);
            FileOutputStream outputStream = new FileOutputStream(outfile);

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

    public static void download(String fileURL, String saveDir)
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
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1
                );
            }

            Log.e(TAG,"--Content-Type = " + contentType);
            Log.e(TAG,"--Content-Disposition = " + disposition);
            Log.e(TAG,"--Content-Length = " + contentLength);
            Log.e(TAG,"--fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

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


    public static void downloadFileAsync(final Context ctx, final String[] fileURL, final String saveDir) {
        AsyncTask aTask = new AsyncTask<String, Void, Boolean>() {
            final ProgressDialog asyncDialog = new ProgressDialog(ctx);
            @Override
            protected Boolean doInBackground(String... url) {
                try {
                    asyncDialog.setMax(fileURL.length);
                    for(int i=0;i<fileURL.length;i++) {
                        download(url[i], saveDir);
                        asyncDialog.setProgress(i);
                    }
                }catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e(TAG,"Err:" + sw.toString());
                    return false;
                }
                return true;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Downloading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                asyncDialog.dismiss();
                super.onPostExecute(result);
                Toast.makeText(ctx, "file download " + result + "!!", Toast.LENGTH_SHORT).show();
            }
        }.execute(fileURL);

        int countdown = 10;
        while (aTask.getStatus() != AsyncTask.Status.FINISHED && countdown >0) {
            try {
                Log.e(TAG, "--waiting for file download....");
                Thread.sleep(100); //0.1초 기다림
                countdown--;
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG,"Err:" + sw.toString());
            }
        }
    }

    public static void downloadFileAsync2(final Context ctx, final String[] fileURL) {
        File saveDir = (Config._default_ext==Config._csv)? Config.mediaStorageDir4csv : Config.mediaStorageDir4mnt;
        AsyncTask aTask = new AsyncTask<String, Void, Boolean>() {
            final ProgressDialog asyncDialog = new ProgressDialog(ctx);
            @Override
            protected Boolean doInBackground(String... url) {
                try {
                    asyncDialog.setMax(fileURL.length);
                    for(int i=0;i<fileURL.length;i++) {
                        download(url[i], saveDir);
                        asyncDialog.setProgress(i);
                    }
                }catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e(TAG,"Err:" + sw.toString());
                    return false;
                }
                return true;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Downloading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                asyncDialog.dismiss();
                super.onPostExecute(result);
                Toast.makeText(ctx, "file download " + result + "!!", Toast.LENGTH_SHORT).show();
            }
        }.execute(fileURL);

        int countdown = 10;
        while (aTask.getStatus() != AsyncTask.Status.FINISHED && countdown >0) {
            try {
                Log.e(TAG, "--waiting for file download....");
                Thread.sleep(100); //0.1초 기다림
                countdown--;
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.e(TAG,"Err:" + sw.toString());
            }
        }
    }


}
