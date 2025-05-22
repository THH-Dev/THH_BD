package com.example.scanimin.Fragment;

import static android.Manifest.permission.CAMERA;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.scanimin.R;
import com.example.scanimin.function.FunctionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraMobileFragment extends Fragment {
    private static CameraMobileFragment instance;
    public static CameraMobileFragment getInstance() {
        if (instance == null) {
            instance = new CameraMobileFragment();
        }
        return instance;
    }
    public CameraMobileFragment() {
        Log.d("CameraMobileFragment", "PrinterFragment constructor");
    }
    public interface OnCaptureImageListener {
        void onCaptureImage(File file);
    }
    private OnCaptureImageListener onCaptureImageListener;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean isCameraRunning = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CameraMobileFragment.OnCaptureImageListener) {
            onCaptureImageListener = (CameraMobileFragment.OnCaptureImageListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUriCapturedListener");
        }
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_camera_fragment, container, false);

        surfaceView = view.findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        return view;
    }

    public void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(getContext(), CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    public void startCamera() {
        if (isCameraRunning) return;

        try {
            camera = Camera.open();
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            isCameraRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopCamera() {
        if (camera != null && isCameraRunning) {
            camera.stopPreview();
            camera.release();
            camera = null;
            isCameraRunning = false;
        }
    }

    public void captureImage() {
        if (camera != null && isCameraRunning) {
            camera.takePicture(null, null, (data, cam) -> {
                File imageFile = FunctionUtils.createTempFile(requireContext());
                String picPath = imageFile.getAbsolutePath();
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    fos.write(data);
                    Toast.makeText(getContext(), "Đã lưu ảnh", Toast.LENGTH_SHORT).show();
                    if (onCaptureImageListener != null) {
                        onCaptureImageListener.onCaptureImage(imageFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(getContext(), "Cần cấp quyền camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
