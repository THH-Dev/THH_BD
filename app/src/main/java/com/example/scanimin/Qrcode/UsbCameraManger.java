package com.example.scanimin.Qrcode;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.scanimin.R;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;

public class UsbCameraManger  implements
            View.OnClickListener,
            CameraViewInterface.Callback {
    private final String TAG = "USBCameraManager";

    private Activity mActivity;
    private View mRootView;
    private UsbManager usbManager;
    private UVCCameraTextureView mTextureView;
    private CameraViewInterface mCameraView;
    private CameraViewInterface mUVCCameraView;
    private EditText edtBrightness, edtContrast;

    private UVCCameraHelper mCameraHelper;
    private boolean isPreview = false;
    private boolean isRequest = false;
    public static final String DIRECTORY_NAME = "MyUSBApp";

    private void showShortMsg(String msg) {
        Toast.makeText(mActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public UsbCameraManger(Activity activity, View rootView) {
        this.mActivity = activity;
        this.mRootView = rootView;
        initViews();
        initCamera();
    }

    private void initViews() {
        mTextureView = mRootView.findViewById(R.id.camera_view);
        mCameraView = mTextureView;
        mCameraView.setCallback(this);
    }

    private void initCamera() {
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(this);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
        mCameraHelper.initUSBMonitor(mActivity, mCameraView, listener);

        mCameraHelper.setOnPreviewFrameListener(nv21Yuv ->
                Log.d(TAG, "Preview frame size: " + nv21Yuv.length));
    }

    private final UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(UsbDevice device) {
            for (UsbDevice deviceCheck : usbManager.getDeviceList().values()) {
//                if (deviceCheck.getVendorId() == 1423 && device.getProductId() == 14401) {
//                    if (deviceCheck == device){
                        if (!isRequest && mCameraHelper != null) {
                            isRequest = true;
                            mCameraHelper.requestPermission(0);
                            showShortMsg(device.getDeviceName() + " is in");
                            showToast("USB camera attached: " + device.getDeviceName());
                        }
//                    } else {
//                        showToast("Đã gắn thiết bị USB không phải camera: " + device.getDeviceName());
//                    }
//                }
            }
        }


        @Override
        public void onDettachDev(UsbDevice device) {
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showToast("USB camera detached");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showToast("Failed to connect camera");
                isPreview = false;
            } else {
                isPreview = true;
                showToast("Camera connected");
                new Thread(() -> {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Looper.prepare();
                    if (mCameraHelper.isCameraOpened()) {
                        Log.i(TAG, "Brightness: " + mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
                        Log.i(TAG, "Contrast: " + mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));
                    }
                    Looper.loop();
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showToast("Camera disconnected");
        }
    };

    public void startCamera() {
        if (!mCameraHelper.isCameraOpened()) {
            mCameraHelper.registerUSB();
            mCameraHelper.startPreview(mCameraView);
            isPreview = true;
            showToast("Started preview");
        }
    }

    public void saveFile() {
        String picPath = FileUtils.ROOT_PATH + DIRECTORY_NAME +
                "/images/" + System.currentTimeMillis() + UVCCameraHelper.SUFFIX_JPEG;

        mCameraHelper.capturePicture(picPath, path -> {
            if (!TextUtils.isEmpty(path)) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(mActivity, "Saved: " + path, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
    }

    public void onStart() {
        mCameraHelper.registerUSB();
    }

    public void onStop() {
        mCameraHelper.unregisterUSB();
    }

    public void onDestroy() {
        FileUtils.releaseFile();
        mCameraHelper.release();
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        Log.i(TAG, "onSurfaceCreated==========");
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
            Log.i(TAG, "onSurfaceCreated start preview ==========");
        }
        else
        {
            Log.i(TAG, "onSurfaceCreated can not start preview ==========");
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
        Log.i(TAG, "onSurfaceChanged=============");
    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }

    private void showToast(String msg) {
        Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }
}

