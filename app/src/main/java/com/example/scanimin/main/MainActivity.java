package com.example.scanimin.main;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.scanimin.Qrcode.CustomCaptureActivity;
import com.example.scanimin.Qrcode.ScanIminActivity;
import com.example.scanimin.R;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Local.SQLLite;

public class MainActivity extends AppCompatActivity {

    private CallApi callApi;
    private SQLLite sqlLite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callApi = new CallApi();
        sqlLite = new SQLLite(this);

        getData();


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
			if (Build.VERSION.SDK_INT >= 30) {
				startActivity(new Intent(this,CustomCaptureActivity.class));
			} else {
				startActivity(new Intent(this, ScanIminActivity.class));
			}
        }
        this.finish();
    }

    private void intentActivity(){
        if (Build.VERSION.SDK_INT >= 30) {
            startActivity(new Intent(this, CustomCaptureActivity.class));
        } else {
            startActivity(new Intent(this, ScanIminActivity.class));
        }
        this.finish();
    }

    private void getData(){
        callApi.getCustomer(this);
    }
}