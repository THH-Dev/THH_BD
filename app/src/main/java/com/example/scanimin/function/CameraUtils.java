package com.example.scanimin.function;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.scanimin.Qrcode.TakeAPhotoActivity;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraUtils {
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private PreviewView previewView;
    private Uri capturedImageUri;
    private String TAG = "CameraUtils";
    private Context context;
    private Camera camera;
    private boolean isCameraActive = false;
    private ProcessCameraProvider cameraProvider;
    private File photoFile;
    public CameraUtils(Context context) {
        this.context = context;
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void startCamera(PreviewView previewView, LifecycleOwner lifecycleOwner, int time) {
        if (isCameraActive) {
            return;
        }
        isCameraActive = true;
        this.previewView = previewView;
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                    imageCapture = new ImageCapture.Builder().build();
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            takePhoto();
                        }
                    }, time);
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Use case binding failed", e);
                }
                finally {
                    isCameraActive = false;
                }
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void takePhoto() {
        ImageCapture imageCapture = this.imageCapture;
        if (imageCapture == null) {
            return;
        }
        photoFile = createTempFile();
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(ImageCaptureException exc) {
                        Log.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
                    }
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults output) {
                        if (photoFile != null) {
                            destroyCamera();
                            ((TakeAPhotoActivity) context).handleCapturedImage(photoFile);
                        }
                    }
                }
        );
    }

    private File createTempFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getCacheDir();
        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    public void destroyCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
            cameraExecutor = null;
        }
        isCameraActive = false;
    }

}
