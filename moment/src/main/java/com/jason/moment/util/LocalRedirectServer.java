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
                        response = "HTTP/1.1 200 OK\r\n\r\nAuthorization successful! You can close this window and return to the app.";
                    } else if (params.containsKey("error")) {
                        String error = params.get("error");
                        Log.e(TAG, "Authorization error: " + error);
                        callback.onError(error);
                        response = "HTTP/1.1 400 Bad Request\r\n\r\nAuthorization failed. Error: " + error;
                    } else {
                        response = "HTTP/1.1 400 Bad Request\r\n\r\nInvalid request";
                    }

                    out.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error handling client: " + e.getMessage());
        }
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