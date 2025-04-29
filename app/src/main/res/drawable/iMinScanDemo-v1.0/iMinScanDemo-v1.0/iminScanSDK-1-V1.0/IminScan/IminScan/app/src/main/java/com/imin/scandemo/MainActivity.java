package com.imin.scandemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
		    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
							Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
		} else {
			if (Build.VERSION.SDK_INT >= 30) {
//				startActivity(new Intent(this,MainActivity3.class));
				startActivity(new Intent(this,CustomCaptureActivity.class));
			} else {
				startActivity(new Intent(this,MainActivity2.class));
			}
			finish();
		}
//		findViewById(R.id.textview).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (Build.VERSION.SDK_INT >= 30) {
////				startActivity(new Intent(this,MainActivity3.class));
//				startActivity(new Intent(MainActivity.this,MainActivity2.class));
//			} else {
//				startActivity(new Intent(MainActivity.this,MainActivity2.class));
//			}}
//		});


	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Log.e("MainActivity","lsy====onRequestPermissionsResult=");
       /* MyPrintService myPrintService = new MyPrintService();
        myPrintService.callPermissionCall();*/
		if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//			if (Build.VERSION.SDK_INT >= 30) {
////				startActivity(new Intent(this,MainActivity3.class));
//				startActivity(new Intent(this,CustomCaptureActivity.class));
//			} else {
//				startActivity(new Intent(this,MainActivity2.class));
//			}
		}
		this.finish();
	}
}
