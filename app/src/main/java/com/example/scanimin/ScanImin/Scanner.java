package com.example.scanimin.ScanImin;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.scanimin.File.ConverFile;
import com.example.scanimin.File.MinioHelper;
import com.example.scanimin.File.MinioUploader;
import com.example.scanimin.Fragment.SettingFragment;
import com.example.scanimin.Fragment.ViewPager2.AllCustomer;
import com.example.scanimin.Fragment.ViewPager2.CustomerChecked;
import com.example.scanimin.Fragment.ViewPager2.CustomerNotChecked;
import com.example.scanimin.Fragment.ViewPager2.Searchable;
import com.example.scanimin.Fragment.ViewPager2.ViewPagerAdapter;
import com.example.scanimin.ListCustomer.CustomerAdapter;
import com.example.scanimin.Fragment.CameraFragment;
import com.example.scanimin.Qrcode.UsbCameraManger;
import com.example.scanimin.R;
import com.example.scanimin.Register.RegisterActivity;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Local.CRUD;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Object.Data;
import com.example.scanimin.data.Object.PostCustomer;
import com.example.scanimin.data.Object.UpdateCustomer;
import com.example.scanimin.databinding.ScanLayoutBinding;
import com.example.scanimin.function.FunctionUtils;
import com.example.scanimin.function.LanguageManager;
import com.example.scanimin.popup.OverlayDialogFragment;
import com.example.scanimin.popup.PopupCompare;
import com.example.scanimin.popup.PopupEnounce;
import com.example.scanimin.popup.PopupThankYou;
import com.google.android.material.tabs.TabLayoutMediator;
import com.imin.scan.Result;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class Scanner extends AppCompatActivity implements CameraFragment.OnUriCapturedListener,
        AllCustomer.OnItemClickListener,
        CustomerNotChecked.OnItemClickListener,
        CustomerChecked.OnItemClickListener{
    private ConnectivityManager.NetworkCallback networkCallback;
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
    private String brightness;
    private String contrast;

    private UsbCameraManger cameraManager;
    private static int time = 3000, time1;
    private File imageFiles;
    private FunctionUtils jsonUtils;
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

    //register

    private PostCustomer postCustomer;
    private static final String alpha = "abcdefghijklmnopqrstuvwxyz";
    private static final String alphaUpperCase = alpha.toUpperCase();

    private static final String UPPERCASE_AND_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String digits = "0123456789";
    private static final String specials = "~=+%^*/()[]{}/!@#$?|";
    private static final String ALPHA_NUMERIC = alpha + alphaUpperCase + digits;
    private static final String ALL = alpha + alphaUpperCase + digits + specials;
    // navigation
    private GestureDetector gestureDetector;
    private boolean isSearch = false;
    private boolean allowSwipeToOpenDrawer = true;

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
        checkinternet();
        initList();
        getDataList();
        if (isViewpage == "scan"){
            navigation();
        }
    }
    private void checkinternet(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() ->
                        Toast.makeText(Scanner.this, "Mất kết nối Internet", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() ->
                        Toast.makeText(Scanner.this, "Đã kết nối Internet", Toast.LENGTH_SHORT).show()
                );
            }
        };

        NetworkRequest request = new NetworkRequest.Builder().build();
        cm.registerNetworkCallback(request, networkCallback);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void navigation(){

        // Bắt sự kiện click trong NavigationView
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.view_camera);
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .remove(fragment)
                                .commit();
                        isViewpage = "scan";
                    }
                }
            }
            if (item.getItemId() == R.id.nav_settings){
                isViewpage = "setting";
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.view_camera, new SettingFragment())
                        .commit();
            }
            if (item.getItemId() == R.id.nav_lock){
                allowSwipeToOpenDrawer = false;
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Tạo gesture detector để hỗ trợ vuốt mở Drawer
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (!allowSwipeToOpenDrawer) return false;
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0 && !binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        binding.drawerLayout.openDrawer(GravityCompat.START);
                        return true;
                    }
                }
                return false;
            }
        });
        if (isViewpage == "scan") {
            // Đăng ký listener vuốt màn hình
            binding.drawerLayout.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return false;
            });
        }
    }



    private void getdata(){
        callApi = new CallApi();
        callApi.getCustomer(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(){
        dbHelper = new SQLLite(this);
        customer = new Customer();
        languageManager = new LanguageManager(this);
        String title = binding.hd.getText().toString();
        setView(1,1,200);
        SpannableString spannable = new SpannableString(binding.hd.getText().toString());
        if (getLanguage().equals("vi")) {
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
        }else {
            int start = title.indexOf("according");
            int end = title.lastIndexOf(" to");
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
        }
        startGif();
        binding.lnLanguage.setOnClickListener(v -> {
            if (getLanguage().equals("vi")) {
                languageManager.changeLanguage("en");
            } else {
                languageManager.changeLanguage("vi");
            }
        });
        binding.imgLogo.setOnClickListener(v -> {
            if (isViewpage == "scan") {
                isViewpage = "list";
                isShowPopup = true;
                binding.listViewCustomer.setVisibility(VISIBLE);
                binding.layoutScan.setVisibility(GONE);
            }
        });
        binding.lnRegister.setOnClickListener(v -> {
            initRegister();
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
        CameraFragment fragment = (CameraFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ln_camera);
        brightness = binding.textBRIGHTNESS.getText().toString();
        contrast = binding.textCONTRAST.getText().toString();
        if (brightness == null) {
            brightness = "200";
        }
        if (contrast == null) {
            contrast = "200";
        }
        binding.setBRIGHTNESS.setOnClickListener(v -> {
            if (fragment != null) {
                fragment.getSetting(Integer.parseInt(brightness), Integer.parseInt(contrast));
            }
        });
        binding.setCONTRAST.setOnClickListener(v -> {
            if (fragment != null) {
                fragment.getSetting(Integer.parseInt(brightness), Integer.parseInt(contrast));
            }
        });
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
        binding.searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                isShowPopup = false;
                isSearch = true;
            }
        });
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                sendQueryToCurrentFragment(query);
                isShowPopup = false;
                isSearch = true;
                Log.d("check", "onQueryTextSubmit: ");
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                sendQueryToCurrentFragment(newText);
                isShowPopup = false;
                isSearch = true;
                Log.d("check", "onQueryTextChange: ");
                return true;
            }
        });
        binding.settingNavigation.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // 300ms
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    allowSwipeToOpenDrawer = true;
                    Toast.makeText(v.getContext(), "You are developer", Toast.LENGTH_SHORT).show();
                }
                lastClickTime = clickTime;
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
                if (!isShowPopup) {
                    for (Customer customerSave : dbHelper.getAllPersons()) {
                        if (Objects.equals(customerSave.getQrcode(), strData)) {
                            if (isSearch){
                                isSearch = false;
//                                binding.searchView.setIconified(false);
                                binding.searchView.setQuery(customerSave.getData().getName(), true);
                                isShowPopup = true;
                                Log.d("dataCustomer", customerSave.getData().getName());
                                break;
                            }else {
                                if (customerSave.getImage() == null) {
                                    onScanSuccess();
                                    getDataScan(customerSave);
                                    checkIn = true;
                                    isShowPopup = true;
                                    break;
                                } else {
                                    isShowPopup = true;
                                    showPopupCheckin(getResources().getString(R.string.you_are_checked), R.drawable.thank_you, customerSave);
                                }
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
        }, new PopupCompare.EditListener() {
            @Override
            public void onEdit(Customer customer) {
                isViewpage = "take a photo";
                onScanSuccess();
                getDataScan(customer);
                isShowPopup = true;
            }
        });
        popupCompare.setCanceledOnTouchOutside(false);
        popupCompare.setCancelable(false);
        popupCompare.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scannerReceiver != null) {
            unregisterReceiver(scannerReceiver);
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && networkCallback != null) {
            cm.unregisterNetworkCallback(networkCallback);
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
        isShowPopup = false;
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
            binding.chup.setVisibility(VISIBLE);
        }
    }
    private void countDown(int time) throws IOException {
        binding.gifImageView.setVisibility(VISIBLE);
        binding.chup.setVisibility(INVISIBLE);
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
                binding.searchView.setQuery("", false);
                isSearch = false;
                isShowPopup = false;
                isViewpage = "scan";
            }
        });
    }
    private void sendQueryToCurrentFragment(String query) {
        Fragment currentFragment = adapter.getFragment(binding.viewPager.getCurrentItem());
        if (currentFragment instanceof Searchable) {
            ((Searchable) currentFragment).onSearchQuery(query);
        }
    }

    @Override
    public void onItemClickedAll(Customer customer) {
        this.customer = customer;
        binding.layoutRegister.setVisibility(GONE);
        binding.layoutScan.setVisibility(VISIBLE);
        binding.searchView.setQuery("", false);
        getDataScan(customer);
        reset();
    }

    @Override
    public void onItemClickedNotChecked(Customer customer) {
        this.customer = customer;
        binding.layoutRegister.setVisibility(GONE);
        binding.layoutScan.setVisibility(VISIBLE);
        binding.searchView.setQuery("", false);
        getDataScan(customer);
        reset();
    }

    @Override
    public void onItemClickedChecked(Customer customer) {
        this.customer = customer;
        binding.layoutRegister.setVisibility(GONE);
        binding.layoutScan.setVisibility(VISIBLE);
        binding.searchView.setQuery("", false);
        getDataScan(customer);
        reset();
    }

    public void onBackPressed() {
        if (isViewpage == "scan") {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            }
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
            isSearch = false;
            isLayoutScan = false;
            isViewpage = "scan";
        }
        if (isViewpage == "list"){
            binding.overlayView.setVisibility(VISIBLE);
            binding.layoutScan.setVisibility(VISIBLE);
            binding.listViewCustomer.setVisibility(GONE);
            isShowPopup = false;
            isLayoutScan = false;
            isSearch = false;
            isViewpage = "scan";
            binding.searchView.setQuery("", false);
        }
        if (isViewpage == "register"){
            binding.overlayView.setVisibility(VISIBLE);
            binding.layoutScan.setVisibility(VISIBLE);
            binding.layoutRegister.setVisibility(GONE);
            isShowPopup = false;
            isLayoutScan = false;
            isSearch = false;
            isViewpage = "scan";
            reset();
        }
        if (isViewpage == "setting"){
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            }
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.view_camera);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragment)
                        .commit();
                isViewpage = "scan";
            }
        }
    }

    // register
    private void initRegister() {
        isViewpage = "register";
        isShowPopup = true;
        postCustomer = new PostCustomer();
        binding.layoutRegister.setVisibility(VISIBLE);
        binding.layoutScan.setVisibility(GONE);
        binding.overlayView.setVisibility(VISIBLE);
        Random generator = new Random();
        binding.imgBackRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Scanner.this, Scanner.class);
            startActivity(intent);
        });
        binding.btnRegister.setOnClickListener(v -> {
            if (binding.editNameRegister.getText().toString().isEmpty()
                    || binding.editAgeRegister.getText().toString().isEmpty()
                    || binding.editCompany.getText().toString().isEmpty()){
                PopupEnounce popupEnounce = new PopupEnounce(Scanner.this, new PopupEnounce.PopupCompareListener() {
                    @Override
                    public void onCompareUpdated() {
                        Log.d("check", "onCompareUpdated: ");
                    }
                });
                popupEnounce.setCanceledOnTouchOutside(false);
                popupEnounce.show();
            }else {
                Data data = new Data();
                try {
                    data.setName(binding.editNameRegister.getText().toString());
                } catch (RuntimeException e) {
                    data.setName("");
                    throw new RuntimeException(e);
                }
                try {
                    int number = Integer.parseInt(binding.editAgeRegister.getText().toString());
                    data.setTable(number);
                } catch (NumberFormatException e) {
                    data.setTable(0);
                    e.printStackTrace();
                }
                try {
                    data.setCompany(binding.editCompany.getText().toString());
                    data.setPosition(binding.editPosition.getText().toString());
                } catch (RuntimeException e) {
                    data.setCompany("");
                    data.setPosition("");
                    throw new RuntimeException(e);
                }
                data.setRole("uninvited");
                customer.setData(data);
                customer.setStatus(false);
                customer.setImage(null);
                customer.setQrcode(randomUpperCaseAndDigits(5));
                postCustomer.setData(data);
                postCustomer.setQrcode(customer.getQrcode());
                insertData();
                insertSQlite();
                binding.layoutRegister.setVisibility(GONE);
                binding.layoutScan.setVisibility(VISIBLE);
                getDataScan(customer);
                reset();
            }
        });
    }
    private void reset(){
        binding.editNameRegister.setText(null);
        binding.editAgeRegister.setText(null);
        binding.editCompany.setText(null);
        binding.editPosition.setText(null);
    }
    private void insertData(){
        callApi.insertCustomer(postCustomer);
    }

    private void insertSQlite(){
        CRUD crud = new CRUD();
        crud.inserDB(customer, dbHelper, this);
    }

    public String randomUpperCaseAndDigits(int numberOfCharacters) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfCharacters; i++) {
            int number = randomNumber(0, UPPERCASE_AND_DIGITS.length() - 1);
            char ch = UPPERCASE_AND_DIGITS.charAt(number);
            sb.append(ch);
        }
        return sb.toString();
    }

    public static int randomNumber(int min, int max) {
        return generator.nextInt((max - min) + 1) + min;
    }

    private static Random generator = new Random();

    //setting

    public void backToCameraFromSetting() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.view_camera);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
            isViewpage = "scan";
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShowPopup = true;
    }


}