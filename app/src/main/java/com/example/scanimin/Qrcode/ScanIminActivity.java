package com.example.scanimin.Qrcode;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;

import com.example.scanimin.R;
import com.example.scanimin.data.Customer;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.databinding.ActivityMainBinding;
import com.example.scanimin.databinding.ScanActivityBinding;
import com.example.scanimin.function.JsonUtils;
import com.example.scanimin.popup.PopupCompare;
import com.imin.scan.Result;
import com.imin.scan.ScanUtils;
import com.imin.scan.Symbol;

import java.util.Objects;

public class ScanIminActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private ScanActivityBinding binding;
    private ScanUtils scanUtils;
    private SurfaceHolder mHolder;
    private Handler autoFocusHandler;
    public int decode_count = 0;
    public boolean use_auto_focus=true;
    private static final String TAG = "MainActivity";
    private Camera mCamera;
    public static int previewSize_width=640;
    public static int previewSize_height=480;
    StringBuilder sb = new StringBuilder();

    private Customer customer;
    private PopupCompare popupCompare;
    String content="";
    private SQLLite dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ScanActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        init();
    }

    private void sendData(String data){
        Intent intent = new Intent(ScanIminActivity.this, TakeAPhotoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("dataScan", data);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( scanUtils!= null) {
            scanUtils.destroy();
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
    private void init() {
        mHolder = binding.surfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);

        scanUtils = ScanUtils.getInstance(this);
        scanUtils.initScan();
        scanUtils.initBeepSound(true,R.raw.beep);
        scanUtils.setConfig(Symbol.ALL_FORMATS);

        if(use_auto_focus)
            autoFocusHandler = new Handler();
        decode_count=0;
        dbHelper = new SQLLite(this);
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
        try{
            mCamera.stopPreview();
        } catch (Exception e) {
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(previewSize_width, previewSize_height);
            if(use_auto_focus)
                parameters.setFocusMode(parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d("DBG", "Error starting camera preview: " + e.getMessage());
        }
    }

    PreviewCallback previewCallback = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Result rawResult = scanUtils.getScanResult(data,previewSize_width,previewSize_height);
                if (scanUtils.getNsyms() != 0 && rawResult != null) {
                    sb.append(rawResult);
                    customer = new Customer();
                    customer = JsonUtils.parseJson(sb.toString());
                    if (customer != null && !customer.getStatus()){
                        Boolean checkIn = false;
                        for (Customer customerSave : dbHelper.getAllPersons()) {
                            if (Objects.equals(customerSave.getQrcode(), customer.getQrcode()) && customer != null) {
                                sendData(rawResult.toString());
                                checkIn = true;
                                break;
                            }
                            if (mCamera != null) {
                                mCamera.stopPreview();
                                sb.delete(0, sb.length());
                            }
                        }
                        if (!checkIn) showPopupCheckin();
                    }else {
                        if (mCamera != null) {
                            mCamera.stopPreview();
                            sb.delete(0, sb.length());
                            showPopupCheckin();
                        }
                    }
                }
                sb.delete(0,sb.length());
        }
    };
    private void showPopupCheckin(){
        popupCompare = new PopupCompare(ScanIminActivity.this, new PopupCompare.PopupCompareListener() {
            @Override
            public void onCompareUpdated() {
                if (mCamera != null){
                    popupCompare.dismiss1();
                    mCamera.startPreview();
                }
            }
        });
        popupCompare.show();
    }

    AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (null == mCamera || null == autoFocusCallback) {
                return;
            }
            mCamera.autoFocus(autoFocusCallback);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try	{
            int df = Camera.getNumberOfCameras();
            mCamera = Camera.open(/*Camera.CameraInfo.CAMERA_FACING_BACK*/);
        } catch (Exception e){
            Log.d(TAG,"Exception=="+e.getMessage());
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
