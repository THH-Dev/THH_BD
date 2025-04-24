package com.example.scanimin.ScanImin;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scanimin.R;

import java.util.HashMap;

public class Scanner extends AppCompatActivity {
    public static final String DEVICE_CONNECTION = "com.imin.scanner.api.DEVICE_CONNECTION";
    public static final String DEVICE_DISCONNECTION = "com.imin.scanner.api.DEVICE_DISCONNECTION";
    public static final String RESULT_ACTION = "com.imin.scanner.api.RESULT_ACTION";
    public static final String CONNECTION_BACK_ACTION = "com.imin.scanner.api.CONNECTION_RESULT";
    public static final String CONNECTION_STATUS_ACTION = "com.imin.scanner.api.DEVICE_IS_CONNECTION";
    public static final String LABEL_TYPE = "com.imin.scanner.api.label_type";
    public static final String EXTRA_DECODE_DATA = "decode_data";
    public static final String EXTRA_DECODE_DATA_STR = "decode_data_str";
    public static final String CONNECTION_TYPE = "com.imin.scanner.api.status";

    private ScannerReceiver scannerReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerScannerBroadcast();
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
        for (UsbDevice usbDevice : usbDeviceList.values()) {
            int vid = usbDevice.getVendorId();
            int pid = usbDevice.getProductId();

            // print VID and PID
            Log.d("USB Info", "Vendor ID (VID): " + vid);
            Log.d("USB Info", "Product ID (PID): " + pid);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerScannerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DEVICE_CONNECTION);
        intentFilter.addAction(DEVICE_DISCONNECTION);
        intentFilter.addAction(RESULT_ACTION);
        intentFilter.addAction(CONNECTION_BACK_ACTION);

        scannerReceiver = new ScannerReceiver();
        registerReceiver(scannerReceiver, intentFilter);
    }

    public class ScannerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DEVICE_CONNECTION.equals(action)) {
                Log.d("ScannerReceiver", "USB Connected");
            } else if (DEVICE_DISCONNECTION.equals(action)) {
                Log.d("ScannerReceiver", "USB Disconnected");
            } else if (RESULT_ACTION.equals(action)) {
                String strData = intent.getStringExtra(EXTRA_DECODE_DATA_STR);
                // xử lý khi nhận được dữ liệu

            } else if (CONNECTION_BACK_ACTION.equals(action)) {
                int type = intent.getIntExtra(CONNECTION_TYPE, 0);
                Log.d("ScannerReceiver", "Scanner connection status: " + (type == 1));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scannerReceiver != null) {
            unregisterReceiver(scannerReceiver);
        }
    }
}