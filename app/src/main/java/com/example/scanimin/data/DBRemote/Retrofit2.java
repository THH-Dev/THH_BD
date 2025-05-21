package com.example.scanimin.data.DBRemote;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit2 {
    public static String urlInternet = "http://192.168.3.69:5000/";
    private static String urlLocal = "http://192.168.3.20:5000/";
    private static String BASE_URL;
    private static Retrofit2 instance;
    private ApiInterface apiInterface;
    public static void getBaseUrl(String url) {
        BASE_URL = url;
    }

    private Retrofit2() {
        if (BASE_URL == null || BASE_URL.isEmpty()){
            BASE_URL = urlInternet;
        }
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
    }

    public static synchronized Retrofit2 getInstance() {
        if (instance == null) {
            instance = new Retrofit2();
        }
        return instance;
    }

    public ApiInterface getApiInterface() {
        return apiInterface;
    }
}
