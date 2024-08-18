package com.jason.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalRedirectServer {
    private static final String TAG = "CustomHttpServer";
    private static final int PORT = 8000;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private ExecutorService executorService;
    private final AuthorizationCallback callback;

    public interface AuthorizationCallback {
        void onAuthorizationCodeReceived(String code);
        void onError(String error);
    }

    public LocalRedirectServer(AuthorizationCallback callback) {
        this.callback = callback;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void startServer() {
        executorService.submit(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                isRunning = true;
                Log.i(TAG, "Server started on port " + PORT);

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error starting server: " + e.getMessage());
                callback.onError("Failed to start server: " + e.getMessage());
            }
        });
    }

    public void stopServer() {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing server: " + e.getMessage());
            }
        }
        executorService.shutdown();
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
        ) {
            String request = in.readLine();
            if (request != null) {
                String[] parts = request.split(" ");
                if (parts.length > 1 && parts[1].startsWith("/callback")) {
                    Map<String, String> params = parseQueryString(parts[1]);
                    String response;

                    if (params.containsKey("code")) {
                        String code = params.get("code");
                        Log.d(TAG, "Authorization code received: " + code);
                        callback.onAuthorizationCodeReceived(code);
                        response = generateSuccessHtml(code, params.get("scope"), params.get("state"));
                    } else if (params.containsKey("error")) {
                        String error = params.get("error");
                        String errorDescription = params.get("error_description");
                        Log.e(TAG, "Authorization error: " + error + " - " + errorDescription);
                        callback.onError(error);
                        response = generateErrorHtml(error, errorDescription);
                    } else {
                        response = generateErrorHtml("Invalid Request", "The request does not contain expected parameters.");
                    }

                    out.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error handling client: " + e.getMessage());
        }
    }

    private String generateSuccessHtml(String code, String scope, String state) {
        return "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" +
                "<!DOCTYPE html><html><head><title>Authorization Successful</title>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; padding: 20px; font-size: 18px; }" +
                "h1 { color: #4CAF50; font-size: 28px; }" +
                "h2 { font-size: 24px; }" +
                "pre { background-color: #f4f4f4; padding: 15px; border-radius: 5px; font-size: 20px; white-space: pre-wrap; word-break: break-all; }" +
                ".info-label { font-weight: bold; }" +
                ".info-value { margin-left: 10px; }" +
                "</style></head><body>" +
                "<h1>Authorization Successful!</h1>" +
                "<p style='font-size: 22px;'>You can now close this window and return to the app.</p>" +
                "<h2>Authorization Details:</h2>" +
                "<div style='font-size: 20px;'>" +
                "<p><span class='info-label'>Authorization Code:</span><span class='info-value'>" + code + "</span></p>" +
                "<p><span class='info-label'>Scope:</span><span class='info-value'>" + (scope != null ? scope : "N/A") + "</span></p>" +
                "<p><span class='info-label'>State:</span><span class='info-value'>" + (state != null ? state : "N/A") + "</span></p>" +
                "</div>" +
                "</body></html>";
    }


    private String generateErrorHtml(String error, String errorDescription) {
        return "HTTP/1.1 400 Bad Request\r\nContent-Type: text/html\r\n\r\n" +
                "<!DOCTYPE html><html><head><title>Authorization Failed</title>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; padding: 20px; font-size: 18px; }" +
                "h1 { color: #D32F2F; font-size: 28px; }" +
                "h2 { font-size: 24px; }" +
                "pre { background-color: #f4f4f4; padding: 15px; border-radius: 5px; font-size: 20px; white-space: pre-wrap; word-break: break-all; }" +
                ".info-label { font-weight: bold; }" +
                ".info-value { margin-left: 10px; }" +
                "</style></head><body>" +
                "<h1>Authorization Failed</h1>" +
                "<h2>Error Details:</h2>" +
                "<div style='font-size: 20px;'>" +
                "<p><span class='info-label'>Error:</span><span class='info-value'>" + error + "</span></p>" +
                "<p><span class='info-label'>Description:</span><span class='info-value'>" + (errorDescription != null ? errorDescription : "No description provided") + "</span></p>" +
                "</div>" +
                "</body></html>";
    }


    private Map<String, String> parseQueryString(String url) {
        Map<String, String> params = new HashMap<>();
        int queryStart = url.indexOf('?');
        if (queryStart != -1) {
            String[] pairs = url.substring(queryStart + 1).split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    params.put(key, value);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing query parameter: " + pair);
                }
            }
        }
        return params;
    }
}