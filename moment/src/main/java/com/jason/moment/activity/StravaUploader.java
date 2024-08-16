package com.jason.moment.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class StravaUploader {
    private static final String TAG = "StravaUploader";
    private static final String CLIENT_ID = "67174";
    private static final String CLIENT_SECRET = "e6b45fcea5836d356bb3c81908b5dbdaa363b1ed";
    public static final String REDIRECT_URI = "http://localhost:8000";
    private static final String SCOPE = "activity:write,activity:read_all";
    private static final String AUTH_URL = "https://www.strava.com/oauth/authorize?client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=code&scope=" + SCOPE;
    private static final String TOKEN_URL = "https://www.strava.com/oauth/token";
    private static final String UPLOAD_URL = "https://www.strava.com/api/v3/uploads";

    private Context context;
    private SharedPreferences sharedPreferences;

    public StravaUploader(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("StravaPrefs", Context.MODE_PRIVATE);
    }

    public void authenticateAndUpload(File gpxFile, String name, String description, String activityType) {
        String accessToken = sharedPreferences.getString("strava_access_token", null);
        if (accessToken == null) {
            initiateAuth();
        } else {
            uploadActivity(gpxFile, name, description, activityType, accessToken);
        }
    }

    private void initiateAuth() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL));
        context.startActivity(intent);
    }

    public void handleAuthResponse(Uri responseUri) {
        String code = responseUri.getQueryParameter("code");
        if (code != null) {
            exchangeCodeForToken(code);
        }
    }

    private void exchangeCodeForToken(String code) {
        new Thread(() -> {
            try {
                URL url = new URL(TOKEN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String postData = "client_id=" + CLIENT_ID +
                        "&client_secret=" + CLIENT_SECRET +
                        "&code=" + code +
                        "&grant_type=authorization_code";

                try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                    byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        String accessToken = jsonResponse.getString("access_token");
                        sharedPreferences.edit().putString("strava_access_token", accessToken).apply();
                        Log.d(TAG, "Successfully obtained Strava access token");
                    }
                } else {
                    Log.e(TAG, "Failed to exchange code for token: " + conn.getResponseCode());
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error during token exchange", e);
            }
        }).start();
    }

    private void uploadActivity(File gpxFile, String name, String description, String activityType, String accessToken) {
        new Thread(() -> {
            try {
                String boundary = "*****" + System.currentTimeMillis() + "*****";
                URL url = new URL(UPLOAD_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);

                try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                    os.writeBytes("--" + boundary + "\r\n");
                    os.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + gpxFile.getName() + "\"\r\n");
                    os.writeBytes("Content-Type: application/gpx+xml\r\n\r\n");

                    FileInputStream fileInputStream = new FileInputStream(gpxFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    fileInputStream.close();

                    os.writeBytes("\r\n--" + boundary + "\r\n");
                    os.writeBytes("Content-Disposition: form-data; name=\"data_type\"\r\n\r\n");
                    os.writeBytes("gpx\r\n");

                    os.writeBytes("--" + boundary + "\r\n");
                    os.writeBytes("Content-Disposition: form-data; name=\"name\"\r\n\r\n");
                    os.writeBytes(name + "\r\n");

                    os.writeBytes("--" + boundary + "\r\n");
                    os.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n");
                    os.writeBytes(description + "\r\n");

                    os.writeBytes("--" + boundary + "\r\n");
                    os.writeBytes("Content-Disposition: form-data; name=\"activity_type\"\r\n\r\n");
                    os.writeBytes(activityType + "\r\n");

                    os.writeBytes("--" + boundary + "--\r\n");
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Activity uploaded successfully to Strava");
                } else {
                    Log.e(TAG, "Failed to upload activity: " + conn.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error during activity upload", e);
            }
        }).start();
    }
}