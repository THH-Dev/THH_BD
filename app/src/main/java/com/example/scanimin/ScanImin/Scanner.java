package com.example.scanimin.ScanImin;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.scanimin.File.ConverFile;
import com.example.scanimin.File.MinioHelper;
import com.example.scanimin.File.MinioUploader;
import com.example.scanimin.Fragment.AllCustomer;
import com.example.scanimin.Fragment.CustomerChecked;
import com.example.scanimin.Fragment.CustomerNotChecked;
import com.example.scanimin.Fragment.Searchable;
import com.example.scanimin.Fragment.ViewPagerAdapter;
import com.example.scanimin.ListCustomer.CustomerAdapter;
import com.example.scanimin.ListCustomer.ListCustomerActivity;
import com.example.scanimin.Qrcode.CameraFragment;
import com.example.scanimin.Qrcode.TakeAPhotoActivity;
import com.example.scanimin.Qrcode.UsbCameraManger;
import com.example.scanimin.R;
import com.example.scanimin.Register.RegisterActivity;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Local.CRUD;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Object.Data;
import com.example.scanimin.data.Object.UpdateCustomer;
import com.example.scanimin.databinding.ListCustomerLayoutBinding;
import com.example.scanimin.databinding.ScanLayoutBinding;
import com.example.scanimin.function.JsonUtils;
import com.example.scanimin.function.LanguageManager;
import com.example.scanimin.popup.OverlayDialogFragment;
import com.example.scanimin.popup.PopupCompare;
import com.example.scanimin.popup.PopupThankYou;
import com.google.android.material.tabs.TabLayoutMediator;
import com.imin.scan.Result;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class Scanner extends AppCompatActivity implements CameraFragment.OnUriCapturedListener,
        AllCustomer.OnItemClickListener,
        CustomerNotChecked.OnItemClickListener,
        CustomerChecked.OnItemClickListener{
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

    private boolean isLayoutScan = false;
    private Customer customer;

    private SQLLite dbHelper;

    private PopupCompare popupCompare;

    private ScanLayoutBinding binding;

    private LanguageManager languageManager;
    private long backPressedTime = 0;

    private boolean isShowPopup = false;
    private CallApi callApi;

    private static boolean checkScan = false;

    String receivedData;
    private PopupThankYou popupThankYou;
    private PopupMenu popupMenu;
    private Result sb;
    private List<Customer> customerList;
    private MinioHelper minIOHelper;
    private CRUD crud;
    private File imageFileCustomer;
    private Boolean isPhoto = true;
    private OverlayDialogFragment overlayDialogFragment;
    private ExecutorService executorService;
    private UpdateCustomer updateCustomer;
    private Boolean isTakePhoto = false;
    private boolean  isStopCamera = false, isReTakePhoto = false, isNewCheck = true;
    private Handler handler = new Handler();
    private Runnable dismissRunnable, runnable;
    private ImageView videoView;
    private int count = 3;
    private Uri contentUri;

    private UsbCameraManger cameraManager;
    private static int time = 3000, time1;
    private File imageFiles;
    private JsonUtils jsonUtils;
    private CameraFragment cameraFragment;
    private Handler idleHandler = new Handler();
    private Runnable idleRunnable;
    private boolean isCamera = false;
    private Uri uri;
    private static final int IDLE_TIMEOUT = 30000;

    // listview
    private RecyclerView recyclerView;
    private CustomerAdapter customerAdapter;

    private ViewPagerAdapter adapter;
    private String currentQuery = "";

    private String isViewpage = "scan";

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

    private void getdata(){
        callApi = new CallApi();
        callApi.getCustomer(this);
    }

    private void init(){
        dbHelper = new SQLLite(this);
        customer = new Customer();
        languageManager = new LanguageManager(this);
        String title = binding.hd.getText().toString();
        setView(1,1,200);
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
        binding.lnLanguage.setOnClickListener(v -> {
//            if (getLanguage().equals("vi")) {
//                languageManager.changeLanguage("en");
//            } else {
//                languageManager.changeLanguage("vi");
//            }
        });
        binding.imgLogo.setOnClickListener(v -> {
            if (isViewpage == "scan") {
                isViewpage = "list";
                isShowPopup = true;
                binding.listViewCustomer.setVisibility(VISIBLE);
                binding.layoutScan.setVisibility(GONE);
                initList();
                getDataList();
            }
        });


        // take photo
        minIOHelper = new MinioHelper();
        callApi = new CallApi();
        crud = new CRUD();
        executorService = Executors.newSingleThreadExecutor();
        isPhoto = true;
        isTakePhoto = false;
        time =0;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ln_camera, new CameraFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        binding.chup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    binding.gifImageView.setVisibility(VISIBLE);
                    countDown(time);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        binding.imgTakeAPhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                binding.cdPreviewCardView.setVisibility(VISIBLE);
                binding.lnPreviewCamera.setVisibility(VISIBLE);
                binding.iconCamera.setVisibility(GONE);
                binding.iconSetcamera.setVisibility(VISIBLE);
                binding.lnImage.setVisibility(VISIBLE);
                binding.lnShowImage.setVisibility(GONE);
            }
        });
        binding.setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(Scanner.this, R.style.PopupMenuStyle), v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_set_time, popupMenu.getMenu());
                for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                    MenuItem menuItemTime = popupMenu.getMenu().getItem(i);
                    View itemView = getLayoutInflater().inflate(R.layout.menu_item_layout, null);
                    TextView title = itemView.findViewById(R.id.menu_item_text);
                    title.setText(menuItemTime.getTitle());

                    menuItemTime.setActionView(itemView);
                    menuItemTime.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItemTime) {
                        int id = menuItemTime.getItemId();
                        if (id == R.id.menu_item_0) {
                            time = 0;
                            binding.textTime.setText(R.string.shutdown);
                            try {
                                settingUiCamera(time);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        }
                        if (id == R.id.menu_item_1) {
                            time = 3000;
                            binding.textTime.setText("3s");
                            try {
                                settingUiCamera(time);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        }
                        if (id == R.id.menu_item_2) {
                            time = 5000;
                            binding.textTime.setText("5s");
                            try {
                                settingUiCamera(time);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        }
                        if (id == R.id.menu_item_3) {
                            time = 8000;
                            binding.textTime.setText("8s");
                            try {
                                settingUiCamera(time);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
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
                                getDataScan(customerSave);
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

    private void getDataScan(Customer customerSave){
        setView(340,340,200);
        isViewpage = "take_a_photo";
        customer = customerSave;
        binding.overlayView.setVisibility(GONE);
        binding.iconSetcamera.setVisibility(VISIBLE);
        binding.cdPreviewCardView.setVisibility(VISIBLE);
        isLayoutScan = true;
        requestCheckin();
    }
    private void requestCheckin(){
        binding.editName.setText(customer.getData().getName());
        binding.editTextId.setText(customer.getQrcode());
        binding.editTextCompany.setText(customer.getData().getCompany());
        binding.editTextPosition.setText(customer.getData().getPosition());
        binding.editAge.setText(String.valueOf(customer.getData().getTable()));
        isPhoto = false;
        binding.lnInformation.setVisibility(VISIBLE);
        binding.description.setVisibility(VISIBLE);
        binding.imgConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateData();
            }
        });
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

    //take a photo

    private void setView(int widthInPd, int heightInPd, float radius){
        binding.cdPreviewCardView.setVisibility(VISIBLE);
        float density = binding.cdPreviewCardView.getResources().getDisplayMetrics().density;
        int widthIn = (int) (widthInPd * density + 0.5f);
        int heightIn = (int) (heightInPd * density + 0.5f);
        ViewGroup.LayoutParams layoutParams = binding.cdPreviewCardView.getLayoutParams();
        if (widthInPd != 0 && heightInPd != 0){
            layoutParams.width = widthIn;
            layoutParams.height = heightIn;
        }else{
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        binding.cdPreviewCardView.setLayoutParams(layoutParams);
    }
    private void setView(int widthInPd, int heightInPd, float radius, View v){
        float density = v.getResources().getDisplayMetrics().density;
        int widthIn = (int) (widthInPd * density + 0.5f);
        int heightIn = (int) (heightInPd * density + 0.5f);
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.width = widthIn;
        layoutParams.height = heightIn;
        v.setLayoutParams(layoutParams);
    }
    private void settingUiCamera(int time) throws IOException {
        binding.description.setVisibility(VISIBLE);
        binding.cdPreviewCardView.setVisibility(VISIBLE);
        binding.lnImage.setVisibility(VISIBLE);
        binding.iconSetcamera.setVisibility(VISIBLE);
        setView(340,340,200);
        binding.imgTakeAPhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!isCamera) {
                    binding.cdPreviewCardView.setVisibility(VISIBLE);
                    binding.lnPreviewCamera.setVisibility(VISIBLE);
                    binding.iconCamera.setVisibility(GONE);
                    binding.iconSetcamera.setVisibility(VISIBLE);
                    binding.lnImage.setVisibility(VISIBLE);
                    binding.lnShowImage.setVisibility(GONE);
                }else {
                    isCamera = false;
                    try {
                        countDown(time);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void chupanh(){
        CameraFragment fragment = (CameraFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ln_camera);

        if (fragment != null) {
            fragment.CaptureImageAndSendUri();
            binding.lnShowImage.setVisibility(VISIBLE);
            binding.iconCamera.setVisibility(VISIBLE);
            binding.iconSetcamera.setVisibility(GONE);
            binding.lnPreviewCamera.setVisibility(GONE);
            binding.imgTakeAPhoto.setVisibility(VISIBLE);
            binding.imgConfirm.setVisibility(VISIBLE);
        }
    }
    private void countDown(int time) throws IOException {
        binding.gifImageView.setVisibility(VISIBLE);
        int video = R.raw.countdown;
        if (time == 3000){
            video = R.raw.countdown;
        }
        if (time == 5000){
            video = R.raw.down5;
        }
        if (time == 8000){
            video = R.raw.download8s;
        }
        if (time ==0){
            isCamera = false;
            new Handler(Looper.getMainLooper()).postDelayed(
                    Scanner.this::takeAPhoto, time
            );
        }else {
            GifImageView gifImageView = findViewById(R.id.gifImageView);

            GifDrawable gifDrawable = new GifDrawable(getResources(), video);
            gifDrawable.setLoopCount(1);
            gifImageView.setImageDrawable(gifDrawable);
            gifDrawable.start();

            new Handler(Looper.getMainLooper()).postDelayed(
                    Scanner.this::takeAPhoto, time
            );
        }
    }
    private void takeAPhoto(){
        // take a photo
        chupanh();
        binding.cdPreviewCardView.setVisibility(GONE);
        setUiAfterTakeAPhoto();
        //image from library
    }

    private void UpdateData(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                customer.setImage(Uri.parse(imageFileCustomer.getName()));
                updateCustomer = new UpdateCustomer(String.valueOf(customer.getImage()),customer.getQrcode());
                callApi.updateCustomer(updateCustomer, new CallApi.UpdateCustomerListener() {
                    @Override
                    public void onUpdateCustomerSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                popUpThankyou();
                            }
                        });
                    }

                    @Override
                    public void onUpdateCustomerFailure(String error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                popUpThankyou();
                            }
                        });
                    }
                });
                callApi.getCustomer(Scanner.this);
                crud.updateDB(customer, dbHelper, Scanner.this);
            }
        });
    }

    private void setUiAfterTakeAPhoto(){
        binding.imgConfirm.setVisibility(VISIBLE);
        binding.imgUser.setVisibility(VISIBLE);
        binding.cdImageCardView.setVisibility(VISIBLE);
        binding.cdPreviewCardView.setVisibility(GONE);
        binding.lnImage.setVisibility(VISIBLE);
        binding.gifImageView.setVisibility(GONE);
    }

    @SuppressLint("ResourceType")

    private void popUpThankyou(){
        PopupThankYou popupThankYou = new PopupThankYou(this, customer, uri);
        popupThankYou.setCanceledOnTouchOutside(false);
        popupThankYou.show();
        dismissRunnable = new Runnable() {
            @Override
            public void run() {
                if (popupThankYou.isShowing()) {
                    popupThankYou.dismiss();
                }
                //get home
                getHome();
            }
        };
        handler.postDelayed(dismissRunnable, 5000);
    }

    //get home
    private void getHome(){
        binding.overlayView.setVisibility(VISIBLE);
        binding.layoutScan.setVisibility(VISIBLE);
        binding.listViewCustomer.setVisibility(GONE);
        binding.iconCamera.setVisibility(GONE);
        binding.lnShowImage.setVisibility(GONE);
        binding.lnPreviewCamera.setVisibility(VISIBLE);
        isShowPopup = false;
        isLayoutScan = false;
        isViewpage = "scan";
    }
    @Override
    public void onUriCaptured(File file) throws IOException {
        uri = Uri.fromFile(file);
//        imageFileCustomer = file;
        imageFileCustomer = ConverFile.cropImageFileToSquare720(file, this);
        MinioUploader.uploadImage(imageFileCustomer, imageFileCustomer.getName());
        Log.d("Screenshot", "Uri: " + uri.toString());
        if (uri != null) {
            Glide.with(this)
                    .load(uri)
                    .error(R.drawable.teamwork)
                    .into(binding.imgUser);
        }
    }
    //listview

    private void getDataList(){
        callApi.getCustomer(this);
        customerList = dbHelper.getAllPersons();
        crud = new CRUD();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void initList(){
        adapter = new ViewPagerAdapter(this, customerList);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.all_customer);
                    break;
                case 1:
                    tab.setText(R.string.registed);
                    break;
                case 2:
                    tab.setText(R.string.unregisted);
                    break;
            }
        }).attach();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                sendQueryToCurrentFragment(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                sendQueryToCurrentFragment(newText);
                return true;
            }
        });
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sendQueryToCurrentFragment(currentQuery);
            }
        });
        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.imgBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.cameraList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.lnCameraList.setVisibility(GONE);
                binding.lnShowImage.setVisibility(VISIBLE);
                chupanh();
            }
        });
        binding.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setView(1,1,300,binding.viewCameraList);
                binding.camera.setBackground(null);
                binding.listView.setVisibility(VISIBLE);
                binding.titleList.setVisibility(VISIBLE);
                binding.lnShowImage.setVisibility(GONE);
                binding.lnCameraList.setVisibility(VISIBLE);
                UpdateData();
                getDataList();

                int currentPosition = binding.viewPager.getCurrentItem();
                Fragment currentFragment = adapter.getFragment(currentPosition);

                if (currentFragment instanceof AllCustomer) {
                    ((AllCustomer) currentFragment).refreshData();
                } else if (currentFragment instanceof CustomerChecked) {
                    ((CustomerChecked) currentFragment).refreshData();
                } else if (currentFragment instanceof CustomerNotChecked) {
                    ((CustomerNotChecked) currentFragment).refreshData();
                }
            }
        });
        binding.cameraAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.lnShowImage.setVisibility(GONE);
                binding.lnCameraList.setVisibility(VISIBLE);
            }
        });
    }
    private void sendQueryToCurrentFragment(String query) {
        Fragment currentFragment = adapter.getFragment(binding.viewPager.getCurrentItem());
        if (currentFragment instanceof Searchable) {
            ((Searchable) currentFragment).onSearchQuery(query);
        }
    }
    private void UpdateDataList(){
        executorService.execute(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                UpdateCustomer updateCustomer;
                customer.setImage(Uri.parse(imageFileCustomer.getName()));
                updateCustomer = new UpdateCustomer(String.valueOf(customer.getImage()),customer.getQrcode());
                callApi.updateCustomer(updateCustomer, new CallApi.UpdateCustomerListener() {
                    @Override
                    public void onUpdateCustomerSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }

                    @Override
                    public void onUpdateCustomerFailure(String error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }
                });
                callApi.getCustomer(Scanner.this);
                crud.updateDB(customer, dbHelper, Scanner.this);
            }
        });
    }

    @Override
    public void onItemClickedAll(Customer customer) {
        setView(600,600,300,binding.viewCameraList);
        binding.camera.setBackgroundResource(R.drawable.kioskcheckinwithtitle);
        binding.listView.setVisibility(GONE);
        binding.titleList.setVisibility(GONE);
        CameraFragment fragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.view_camera);
        @SuppressLint("WrongViewCast") UVCCameraTextureView newView = findViewById(R.id.ln_camera);

        if (fragment != null && newView != null) {
            fragment.moveCameraToNewView(newView);
        }
        this.customer = customer;
    }

    @Override
    public void onItemClickedNotChecked(Customer customer) {
        binding.camera.setBackgroundResource(R.drawable.kioskcheckinwithtitle);
        setView(600,600,300,binding.viewCameraList);
        binding.listView.setVisibility(GONE);
        binding.titleList.setVisibility(GONE);
        this.customer = customer;
    }

    @Override
    public void onItemClickedChecked(Customer customer) {
        binding.camera.setBackgroundResource(R.drawable.kioskcheckinwithtitle);
        setView(600,600,300,binding.viewCameraList);
        binding.listView.setVisibility(GONE);
        binding.titleList.setVisibility(GONE);
        this.customer = customer;
    }

    public void onBackPressed() {
        if (isViewpage == "scan") {
            if (backPressedTime > System.currentTimeMillis()) {
                super.onBackPressed();
            }
            backPressedTime = System.currentTimeMillis();
        }
        if (isViewpage == "take_a_photo"){
            binding.overlayView.setVisibility(VISIBLE);
            binding.layoutScan.setVisibility(VISIBLE);
            binding.listViewCustomer.setVisibility(GONE);
            isShowPopup = false;
            isLayoutScan = false;
            isViewpage = "scan";
        }
        if (isViewpage == "list"){
            binding.overlayView.setVisibility(VISIBLE);
            binding.layoutScan.setVisibility(VISIBLE);
            binding.listViewCustomer.setVisibility(GONE);
            isShowPopup = false;
            isLayoutScan = false;
            isViewpage = "scan";
        }
    }
}