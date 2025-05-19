package com.example.scanimin.Qrcode;


import static android.os.Looper.getMainLooper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.scanimin.File.ConverFile;
import com.example.scanimin.R;
import com.example.scanimin.function.JsonUtils;
import com.example.scanimin.function.MyApplication;
import com.google.android.material.button.MaterialButton;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class CameraFragment extends Fragment implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {
    private static String TAG = "TuanNA-PrinterFragment";
    private static CameraFragment instance;
    public static final String DIRECTORY_NAME = "MyUSBApp";

    public CameraFragment() {
        Log.d(TAG, "PrinterFragment constructor");
    }

    public static CameraFragment getInstance() {
        if (instance == null) {
            instance = new CameraFragment();
        }
        return instance;
    }
    UVCCameraTextureView mTextureView;

    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private boolean isRequest = false;
    private boolean isPreview = false;
    private VideoView videoView;
    //View
    private View mView;
    private Activity mActivity;

    private JsonUtils jsonUtils;
    private boolean isCameraInitialized = false;


    public interface OnUriCapturedListener {
        void onUriCaptured(File file) throws IOException;
    }

    private OnUriCapturedListener callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnUriCapturedListener) {
            callback = (OnUriCapturedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUriCapturedListener");
        }
    }


    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            // request open permission
            Log.d(TAG, "onAttachDev==========");
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    Log.d(TAG, "mCameraHelper != null, request Permission, index 0");
                    mCameraHelper.requestPermission(0);
                }
                else {
                    Log.d(TAG, "mCameraHelper == null");
                }
            }
            else {
                Log.d(TAG, "isRequest = true, do nothing");
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            Log.d(TAG, "onDettachDev ==========");
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            Log.d(TAG, "onConnecDev: " + device.getDeviceName() + ", isConnected = " + isConnected);
            if (!isConnected) {
                isPreview = false;
            } else {
                isPreview = true;
            }
        }
        @Override
        public void onDisConnectDev(UsbDevice device) {
            Log.d(TAG, "device disconnect");
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Printer Fragment");
        mView = inflater.inflate(R.layout.fragment_camera, container, false);
        mActivity = getActivity();

        //cam
        mTextureView = mView.findViewById(R.id.camera_view);
        videoView = mView.findViewById(R.id.video_countdown);
        mTextureView.updateFps();

        //cam
        return mView;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isCameraInitialized) {
            initCamera();
            isCameraInitialized = true;
        }
    }

    private void initCamera(){
        // step.1 initialize UVCCameraHelper
        mCameraHelper = UVCCameraHelper.getInstance();
        try {
            mCameraHelper.release();
            mCameraHelper.unregisterUSB();
            mCameraHelper.setDefaultPreviewSize(1280,720);
            mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
            mUVCCameraView = (CameraViewInterface) mTextureView;
            mUVCCameraView.setCallback(this);
            mCameraHelper.initUSBMonitor(mActivity, mUVCCameraView, listener);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public void CaptureImageAndSendUri() {
        File imageFile = JsonUtils.createTempFile(requireContext());
        String picPath = imageFile.getAbsolutePath();

        mCameraHelper.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {
            @Override
            public void onCaptureResult(String path) {
                if(TextUtils.isEmpty(path)) {
                    return;
                }
                File file = new File(path);
                if (callback != null) {
                    new Handler(getMainLooper()).post(() -> {
                        try {
                            callback.onUriCaptured(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        });
    }

    public void moveCameraToNewView(UVCCameraTextureView newView) {
        if (mCameraHelper != null && mCameraHelper.isCameraOpened()) {
            // 1. Stop preview ở view cũ
            mCameraHelper.stopPreview();

            // 2. Gán view mới làm camera surface
            mUVCCameraView = newView;
            mUVCCameraView.setCallback(this);

            // 3. Bắt đầu lại preview
            mCameraHelper.startPreview(mUVCCameraView);
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }



    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            Log.d(TAG, "mCameraHelper.registerUSB=============");
            mCameraHelper.registerUSB();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            Log.d(TAG, "mCameraHelper.unregisterUSB=============");
            mCameraHelper.unregisterUSB();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            try {
                mCameraHelper.unregisterUSB();
                mCameraHelper.release();
            } catch (Exception e) {
                Log.e(TAG, "Error during camera release", e);
            }
            mCameraHelper = null;
        }
        isCameraInitialized = false;
    }

    @Override
    public USBMonitor getUSBMonitor() {
        mCameraHelper.setDefaultPreviewSize(1280,720);
        Log.i(TAG, "getUSBMonitor===========");
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        Log.i(TAG, "onDialogResult");
    }

    private void showShortMsg(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        Log.i(TAG, "onSurfaceCreated==========");
        try {
            if (!isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.startPreview(mUVCCameraView);
                isPreview = true;
                Log.i(TAG, "onSurfaceCreated start preview ==========");
            }
            else
            {
                Log.i(TAG, "onSurfaceCreated can not start preview ==========");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void startPreview() {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
        }
    }

    public void stopPreview() {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
        Log.i(TAG, "onSurfaceChanged=============");
        if (mUVCCameraView == null){
            mCameraHelper.stopPreview();
        }else{
            mCameraHelper.startPreview(mUVCCameraView);
        }
    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        Log.i(TAG, "onSurfaceDestroy=============");
        if (isPreview && mCameraHelper.isCameraOpened()&& mUVCCameraView == null) {
            mCameraHelper.stopPreview();
            isPreview = false;
            Log.i(TAG, "onSurfaceDestroy stop preview ==========");
        }
        else {
            Log.i(TAG, "onSurfaceDestroy can not stop preview ==========");
        }
    }
    private Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

}
