package com.example.scanimin.Qrcode;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class UsbPermission {
    private static final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";
    private final Context context;
    private final UsbManager usbManager;
    private UsbDeviceConnection connection;
    private UsbEndpoint videoEndpoint;
    private UsbInterface streamingInterface;

    public UsbPermission(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        ContextCompat.registerReceiver(context, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }
    public void initAndStart() {
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            for (int i = 0; i < device.getInterfaceCount(); i++) {
                UsbInterface iface = device.getInterface(i);
                if (iface.getInterfaceClass() == UsbConstants.USB_CLASS_VIDEO) {
                    Log.d("USB", "Found camera interface on device: " + device.getDeviceName());

                    if (usbManager.hasPermission(device)) {
                        Log.d("USB", "Permission already granted for device: " + device.getDeviceName());
                    } else {
                        PendingIntent permissionIntent = PendingIntent.getBroadcast(
                                context,
                                0,
                                new Intent(ACTION_USB_PERMISSION),
                                PendingIntent.FLAG_IMMUTABLE
                        );
                        usbManager.requestPermission(device, permissionIntent);
                        Log.d("USB", "Requesting permission for device: " + device.getDeviceName());
                    }
                }
            }
        }
    }
    public boolean hasPermission() {
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (usbManager.hasPermission(device)) {
                return true;
            }
        }
        return false;
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null) {
                    Log.d("USB", "Permission granted");
                }
            }
        }
    };
}
