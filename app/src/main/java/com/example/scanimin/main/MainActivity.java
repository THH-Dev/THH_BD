package com.example.scanimin.main;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.scanimin.File.MinioHelper;
import com.example.scanimin.Qrcode.UsbPermission;
import com.example.scanimin.R;
import com.example.scanimin.ScanImin.Scanner;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Newdata.NewCustomer;
import com.example.scanimin.function.JsonUtils;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CallApi callApi;
    private SQLLite sqlLite;

    private UsbPermission usbPermission;

    private MinioHelper minIOHelper;
    private JsonUtils jsonUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callApi = new CallApi();
        sqlLite = new SQLLite(this);
        minIOHelper = new MinioHelper();
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{CAMERA,WRITE_EXTERNAL_STORAGE,
                            READ_EXTERNAL_STORAGE}, 0);
        } else {
            intentActivity();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestPermissionsUsb();
        }
        this.finish();
    }

    private void intentActivity(){
        startActivity(new Intent(this, Scanner.class));
        this.finish();
    }

    private void getData(){
        callApi.getCustomer(this);
    }

    private void requestPermissionsUsb(){
        usbPermission = new UsbPermission(this);
        usbPermission.initAndStart();
        startActivity(new Intent(this, Scanner.class));
    }
}