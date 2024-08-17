package com.jason.moment.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import androidx.browser.customtabs.CustomTabsIntent;
import net.openid.appauth.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class StravaUploader {
    private static final String TAG = "StravaUploader";
    private static final String CLIENT_ID = "67174";
    private static final String CLIENT_SECRET = "11deb64d5fc70d28aed865992a6792f28edce3c6";
    private static final String AUTHORIZATION_ENDPOINT = "https://www.strava.com/oauth/mobile/authorize";
    private static final String TOKEN_ENDPOINT = "https://www.strava.com/oauth/token";
    private static final String UPLOAD_URL = "https://www.strava.com/api/v3/uploads";
    private static final String SCOPE = "activity:write";
    private static final String REDIRECT_URI = "com.jason.moment://oauth2callback";
    private Context context;
    private AuthorizationService authService;

    public StravaUploader(Context context) {
        this.context = context;
        this.authService = new AuthorizationService(context);
    }

    public void authenticate() {
        AuthorizationServiceConfiguration serviceConfiguration =
                new AuthorizationServiceConfiguration(
                        Uri.parse(AUTHORIZATION_ENDPOINT),
                        Uri.parse(TOKEN_ENDPOINT)
                );

        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfiguration,
                        CLIENT_ID,
                        ResponseTypeValues.CODE,
                        Uri.parse(REDIRECT_URI)
                )
                        .setScopes(SCOPE);

        AuthorizationRequest authRequest = authRequestBuilder.build();

        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();

        Intent authIntent = authService.getAuthorizationRequestIntent(authRequest, customTabsIntent);
        ((Activity) context).startActivityForResult(authIntent, 1001);
    }

    public void handleAuthorizationResponse(Intent data) {
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
        AuthorizationException ex = AuthorizationException.fromIntent(data);

        if (resp != null) {
            exchangeAuthorizationCode(resp.authorizationCode);
        } else {
            // Handle error
        }
    }
    private void exchangeAuthorizationCode(String authCode) {
        AuthorizationServiceConfiguration serviceConfiguration =
                new AuthorizationServiceConfiguration(
                        Uri.parse(AUTHORIZATION_ENDPOINT),
                        Uri.parse(TOKEN_ENDPOINT)
                );

        TokenRequest tokenRequest = new TokenRequest.Builder(
                serviceConfiguration,
                CLIENT_ID
        )
                .setGrantType(GrantTypeValues.AUTHORIZATION_CODE)
                .setAuthorizationCode(authCode)
                .setRedirectUri(Uri.parse(REDIRECT_URI))
                .build();

        authService.performTokenRequest(
                tokenRequest,
                (response, ex) -> {
                    if (response != null) {
                        String accessToken = response.accessToken;
                        // Store this token securely and use it for uploads
                    } else {
                        // Handle error
                    }
                });
    }

    public void uploadActivity(File gpxFile, String name, String description, String activityType, String accessToken) {
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

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Activity uploaded successfully to Strava");
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Activity uploaded successfully", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Log.e(TAG, "Failed to upload activity: " + responseCode);
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Failed to upload activity", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (IOException e) {
                Log.e(TAG, "Error during activity upload", e);
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error uploading activity", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}