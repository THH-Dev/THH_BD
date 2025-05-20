package com.example.scanimin.function;

import android.app.Application;
import android.util.Log;


public class MyApplication extends Application {
    // File Directory in sd card
    public static final String DIRECTORY_NAME = "USBCamera";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TuanNA", "MyApplication ==================");
    }
}
