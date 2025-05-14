package com.example.scanimin.ScanImin;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.scanimin.ListCustomer.ListCustomerActivity;
import com.example.scanimin.Qrcode.TakeAPhotoActivity;
import com.example.scanimin.R;
import com.example.scanimin.Register.RegisterActivity;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.databinding.ScanLayoutBinding;
import com.example.scanimin.function.LanguageManager;
import com.example.scanimin.popup.PopupCompare;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Scanner extends AppCompatActivity{
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
    private Customer customer;

    private SQLLite dbHelper;

    private PopupCompare popupCompare;

    private ScanLayoutBinding binding;

    private LanguageManager languageManager;
    private long backPressedTime = 0;

    private boolean isShowPopup = false;
    private CallApi callApi;

    private static boolean checkScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ScanLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getLanguage().equals("vi")){
            binding.imgLanguage.setImageResource(R.drawable.vietnam);
        }else {
            binding.imgLanguage.setImageResource(R.drawable.united_kingdom);
        }
        getdata();
        init();

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
    public void onBackPressed() {
        if (backPressedTime  > System.currentTimeMillis()) {
            super.onBackPressed();
        }
        backPressedTime = System.currentTimeMillis();
    }

    private void getdata(){
        callApi = new CallApi();
        callApi.getCustomer(this);
    }

    private void init(){
        dbHelper = new SQLLite(this);
        languageManager = new LanguageManager(this);
        String title = binding.hd.getText().toString();
        SpannableString spannable = new SpannableString(binding.hd.getText().toString());
        int start = title.indexOf("quẹt");
        int end = title.lastIndexOf("đ");
        if (end != -1) {
            end = end;
        } else {
            end = title.length();
        }
        spannable.setSpan(
                new ForegroundColorSpan(Color.RED),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        binding.hd.setText(spannable);
        startGif();
        binding.lnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Scanner.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
        binding.imgLanguage.setOnClickListener(v -> {
            if (getLanguage().equals("vi")) {
                languageManager.changeLanguage("en");
            } else {
                languageManager.changeLanguage("vi");
            }
        });
        binding.imgLogo.setOnClickListener(v -> {
            Intent intent = new Intent(Scanner.this, ListCustomerActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private String getLanguage(){
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        Locale currentLocale = configuration.locale;
        return currentLocale.getLanguage();
    }

    private void startGif(){
        Glide.with(Scanner.this)
                .asGif()
                .load(R.raw.quetthe) // có thể là URL, asset, hoặc file
                .into(binding.imageView);
    }
    public void onScanSuccess() {
        if (binding.videoView != null) {
            binding.videoView.stopPlayback(); // Stop the GIF
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
                Boolean checkIn = false;
                if (!isShowPopup){
                    for (Customer customerSave : dbHelper.getAllPersons()) {
                        if (Objects.equals(customerSave.getQrcode(), strData)) {
                            if (customerSave.getImage() == null){
                                onScanSuccess();
                                sendData(customerSave);
                                checkIn = true;
                                isShowPopup = true;
                                break;
                            }else{
                                isShowPopup = true;
                                showPopupCheckin(getResources().getString(R.string.you_are_checked), R.drawable.thank_you, customerSave);
                            }
                        }
                    }
                    if (!checkIn && !isShowPopup) {
                        isShowPopup = true;
                        showPopupCheckin(getResources().getString(R.string.qrerror), R.drawable.cancel, null);
                    }
                }
            }else {
                if (scannerReceiver != null) {
                    unregisterReceiver(scannerReceiver);
                }
                if (!isShowPopup) {
                    registerScannerBroadcast();
                }
            }
        }
    }
    private void sendData(Customer customer){
        Bundle bundle = new Bundle();
        bundle.putString("name", customer.getData().getName());
        bundle.putString("table", String.valueOf(customer.getData().getTable()));
        bundle.putString("company", customer.getData().getCompany());
        bundle.putString("position", customer.getData().getPosition());
        bundle.putString("qrcode", customer.getQrcode());
        if (customer.getData().getName() == null){
            Intent intent = new Intent(Scanner.this, RegisterActivity.class);
            intent.putExtra("customer_new", bundle);
            startActivity(intent);
            finish();
        }else {
            Intent intent = new Intent(Scanner.this, TakeAPhotoActivity.class);
            intent.putExtra("customer_new", bundle);
            startActivity(intent);
            finish();
        }
    }

    private void showPopupCheckin(String string, int url, Customer customer){
        popupCompare = new PopupCompare(string, url, Scanner.this, customer, new PopupCompare.PopupCompareListener() {
            @Override
            public void onCompareUpdated() {
                isShowPopup = false;
                checkScan = true;
                //bat lại scan
            }
        });
        popupCompare.setCanceledOnTouchOutside(false);
        popupCompare.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scannerReceiver != null) {
            unregisterReceiver(scannerReceiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        binding.videoView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getdata();
    }
}