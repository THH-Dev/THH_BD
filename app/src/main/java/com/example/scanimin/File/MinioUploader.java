package com.example.scanimin.File;

import android.util.Base64;
import android.util.Log;

import java.io.File;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MinioUploader {

    //"http://192.168.3.69:9000";
    //"CLR1bkuA5m9VT5DQtBms";
    //"yM1yfUIbTxeFcdXzx8gCf6xFHNhWbWpJ1lPVax8w";
    //"image";
    private static String ENDPOINT;
    private static String ACCESS_KEY;
    private static String SECRET_KEY;
    private static String BUCKET_NAME;

    public static void getEndpoint(String endpoint) {
        ENDPOINT = endpoint;
    }
    public static void getAccessKey(String accessKey) {
        ACCESS_KEY = accessKey;
    }
    public static void getSecretKey(String secretKey) {
        SECRET_KEY = secretKey;
    }
    public static void getBucketName(String bucketName) {
        BUCKET_NAME = bucketName;
    }

    public static void uploadImage(File imageFile, String objectName) {
        if (!imageFile.exists()) {
            Log.e("MinioUploader", "File does not exist: " + imageFile.getAbsolutePath());
            return;
        }
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("image/jpeg");

        RequestBody body = RequestBody.create(mediaType, imageFile);

        // PUT URL format: http://endpoint/bucket-name/object-name
        String url = ENDPOINT + "/" + BUCKET_NAME + "/" + objectName;

        // Basic Auth header
        String credentials = ACCESS_KEY + ":" + SECRET_KEY;
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Content-Type", "image/jpeg")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                Log.e("MinioUploader", "Upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    Log.i("MinioUploader", "Upload successful");
                } else {
                    Log.e("MinioUploader", "Upload failed: " + response.code() + " - " + response.message());
                }
            }
        });
    }
}
