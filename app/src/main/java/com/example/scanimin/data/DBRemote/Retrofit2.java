package com.example.scanimin.data.DBRemote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit2 {

    private static String urlInternet = "http://192.168.3.69:5000/";

    private static String urlLocal = "http://192.168.3.20:5000/";
    private static final String BASE_URL = urlInternet;
    private static Retrofit2 instance;
    private ApiInterface apiInterface;

    private Retrofit2() {
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
