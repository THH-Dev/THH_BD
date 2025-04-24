package com.example.scanimin.Qrcode;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;

import com.example.scanimin.File.MinioHelper;
import com.example.scanimin.R;
import com.example.scanimin.data.Customer;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Local.CRUD;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.UpdateCustomer;
import com.example.scanimin.databinding.ActivityMainBinding;
import com.example.scanimin.databinding.TakeAPhotoActivityBinding;
import com.example.scanimin.function.CameraUtils;
import com.example.scanimin.function.JsonUtils;
import com.example.scanimin.function.LanguageManager;
import com.example.scanimin.popup.OverlayDialogFragment;
import com.example.scanimin.popup.PopupCompare;
import com.example.scanimin.popup.PopupThankYou;
import com.imin.scan.Result;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TakeAPhotoActivity extends AppCompatActivity {

    private String TAGScan = "TakeAPhotoActivity";
    private TakeAPhotoActivityBinding binding;
    String receivedData;
    private PopupCompare popupCompare;
    private PopupThankYou popupThankYou;
    private PopupMenu popupMenu;
    private Result sb;
    private Customer customer;
    private CameraUtils cameraUtils;
    private List<Customer> customerList;
    private MinioHelper minIOHelper;
    private CRUD crud;
    private File imageFileCustomer;
    private Boolean isPhoto = true;
    private CallApi callApi;
    private OverlayDialogFragment overlayDialogFragment;
    private ExecutorService executorService;
    private SQLLite dbHelper;
    private UpdateCustomer updateCustomer;
    private Boolean isTakePhoto = false;
    private boolean  isStopCamera = false, isReTakePhoto = false, isNewCheck = true;
    private Handler handler = new Handler();
    private Runnable dismissRunnable;
    private VideoView videoView;

    private int time = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TakeAPhotoActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getData();
        init();
    }
    private void init(){
        isStopCamera = false;
        binding.imgConfirm.setVisibility(GONE);
        binding.imgRetry.setVisibility(GONE);
        popupThankYou = new PopupThankYou(this);
        dbHelper = new SQLLite(this);
        cameraUtils = new CameraUtils(this);
        minIOHelper = new MinioHelper();
        callApi = new CallApi();
        crud = new CRUD();
        executorService = Executors.newSingleThreadExecutor();
        isPhoto = true;
    }

    private void getData(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            receivedData = bundle.getString("dataScan");
            customer = new Customer();
            customer = JsonUtils.parseJson(receivedData);
            requestCheckin(receivedData);
        }
    }

    private void requestCheckin(String rawResult){
        binding.editTextName.setText(customer.getData().getName());
        binding.editTextId.setText(customer.getQrcode());
        binding.editTextCompany.setText(customer.getData().getCompany());
        binding.editTextPosition.setText(customer.getData().getPosition());
        if (rawResult != null) {
            isPhoto = false;
            binding.titleScan.setVisibility(VISIBLE);
            binding.lnInformation.setVisibility(VISIBLE);
            binding.previewView.setVisibility(GONE);
            binding.cdPreviewCardView.setVisibility(GONE);
            if (isReTakePhoto) {
                binding.imgConfirm.setVisibility(VISIBLE);
                binding.imgRetry.setVisibility(VISIBLE);
                binding.imgUser.setVisibility(VISIBLE);
                binding.cdImageCardView.setVisibility(VISIBLE);
            }
            if (isNewCheck) {
                binding.textDescription.setVisibility(VISIBLE);
                binding.textRequestCamera.setVisibility(VISIBLE);
                binding.imgTakeAPhoto.setVisibility(VISIBLE);
            }
            isNewCheck = false;
            binding.imgConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customer != null) {
                        isNewCheck = true;
                        Log.d(TAGScan, customer.getImage());
                        UpdateData();
                    }
                }
            });
            binding.imgTakeAPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePhoto(time);
                    binding.previewView.setVisibility(VISIBLE);
                    binding.cdPreviewCardView.setVisibility(VISIBLE);
                    binding.imgTakeAPhoto.setVisibility(GONE);
                    binding.textSetTime.setVisibility(GONE);
                    binding.btnSetime.setVisibility(GONE);
                    binding.setting.setVisibility(GONE);
//					setUiAfterTakeAPhoto();
                }
            });
            binding.imgRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isPhoto = false;
                    binding.imgUser.setVisibility(GONE);
                    binding.cdImageCardView.setVisibility(GONE);
                    binding.previewView.setVisibility(VISIBLE);
                    binding.cdPreviewCardView.setVisibility(VISIBLE);
                    cameraUtils.startCamera(binding.previewView, TakeAPhotoActivity.this, time);
                    binding.imgRetry.setVisibility(GONE);
                    binding.imgConfirm.setVisibility(GONE);
                }
            });
            binding.setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LanguageManager languageManager = new LanguageManager(TakeAPhotoActivity.this);
                    PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(TakeAPhotoActivity.this, R.style.PopupMenuStyle), v);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_setting, popupMenu.getMenu());
                    for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                        MenuItem menuItem = popupMenu.getMenu().getItem(i);
                        View itemView = getLayoutInflater().inflate(R.layout.menu_item_layout, null);
                        TextView title = itemView.findViewById(R.id.menu_item_text);
                        title.setText(menuItem.getTitle());

                        menuItem.setActionView(itemView);
                        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int id = menuItem.getItemId();
                                if (id == R.id.menu_item_0) {
                                    PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(TakeAPhotoActivity.this, R.style.PopupMenuStyle), v);
                                    popupMenu.getMenuInflater().inflate(R.menu.menu_language, popupMenu.getMenu());
                                    for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                                        MenuItem menuItemLanguage = popupMenu.getMenu().getItem(i);
                                        View itemView = getLayoutInflater().inflate(R.layout.menu_item_layout, null);
                                        TextView title = itemView.findViewById(R.id.menu_item_text);
                                        title.setText(menuItemLanguage.getTitle());

                                        menuItemLanguage.setActionView(itemView);
                                        menuItemLanguage.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                                    }
                                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem menuItemTime) {
                                            int id = menuItemTime.getItemId();
                                            if (id == R.id.menu_item_0) {
                                                languageManager.changeLanguage("vi");
                                                return true;
                                            }
                                            if (id == R.id.menu_item_1) {
                                                languageManager.changeLanguage("en");
                                                return true;
                                            }return false;
                                        }
                                    });
                                    popupMenu.show();
                                }
                                if (id == R.id.menu_item_1) {
                                    PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(TakeAPhotoActivity.this, R.style.PopupMenuStyle), v);
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
                                            if (id ==R.id.menu_item_0) {
                                                binding.btnSetime.setText(R.string.one_s);
                                                time = 1000;
                                                return true;
                                            }
                                            if (id ==R.id.menu_item_1) {
                                                binding.btnSetime.setText(R.string.five_s);
                                                time = 5000;
                                                return true;
                                            }
                                            if (id ==R.id.menu_item_2) {
                                                binding.btnSetime.setText(R.string.ten_s);
                                                time = 10000;
                                                return true;
                                            }
                                            if (id ==R.id.menu_item_3) {
                                                binding.btnSetime.setText(R.string.fitten_s);
                                                time = 15000;
                                                return true;
                                            }
                                            if (id ==R.id.menu_item_4) {
                                                time = 20000;
                                                binding.btnSetime.setText(R.string.thirty_s);
                                            }
                                            return false;
                                        }
                                    });
                                    popupMenu.show();
                                }
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                }
            });
            binding.btnSetime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    private void UpdateData(){
        overlayDialogFragment = OverlayDialogFragment.newInstance();
        overlayDialogFragment.show(getSupportFragmentManager(), "OverlayDialog");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                updateCustomer = new UpdateCustomer();
                customer.setImage(imageFileCustomer.getName());
                updateCustomer.setImage(customer.getImage());
                customer.setStatus(true);
                updateCustomer.setStatus(customer.getStatus());
                updateCustomer.setQrcode(customer.getQrcode());
                crud.updateDB(customer, dbHelper, TakeAPhotoActivity.this);
                minIOHelper.uploadImageToMinIO(imageFileCustomer);
                callApi.updateCustomer(updateCustomer, new CallApi.UpdateCustomerListener() {
                    @Override
                    public void onUpdateCustomerSuccess() {
                        overlayDialogFragment.dismiss();
                        popUpThankyou();
                    }

                    @Override
                    public void onUpdateCustomerFailure(String error) {
                        overlayDialogFragment.dismiss();
                        popUpThankyou();
                    }
                });
            }
        });
    }

    private void setUiAfterTakeAPhoto(){
        if (isTakePhoto){
            binding.imgConfirm.setVisibility(VISIBLE);
            binding.imgRetry.setVisibility(VISIBLE);
            binding.imgUser.setVisibility(VISIBLE);
            binding.cdImageCardView.setVisibility(VISIBLE);
            binding.btnTakeAPhoto.setVisibility(GONE);
            binding.imgTakeAPhoto.setVisibility(GONE);
            binding.textDescription.setVisibility(GONE);
            binding.textRequestCamera.setVisibility(GONE);
            binding.previewView.setVisibility(GONE);
            binding.cdPreviewCardView.setVisibility(GONE);
        }
    }

    @SuppressLint("ResourceType")

    private void popUpThankyou(){
        popupThankYou.show();
        dismissRunnable = new Runnable() {
            @Override
            public void run() {
                if (popupThankYou.isShowing()) {
                    popupThankYou.dismiss();
                }
                startActivity(new Intent(TakeAPhotoActivity.this, ScanIminActivity.class));
                finish();
            }
        };
        handler.postDelayed(dismissRunnable, 5000);
    }

    private void takePhoto(int time){
        cameraUtils.startCamera(binding.previewView, this, time);
        isTakePhoto = true;
    }

    public void handleCapturedImage(File imageFile) {
        isTakePhoto = true;
        if (imageFile != null && imageFile.exists()) {
            imageFileCustomer = imageFile;
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            binding.imgUser.setImageBitmap(bitmap);
            Log.d("MainActivity", "Image file name: " + imageFile.getName());
        }
        binding.imgUser.setVisibility(VISIBLE);
        binding.cdImageCardView.setVisibility(VISIBLE);
        binding.previewView.setVisibility(GONE);
        binding.cdPreviewCardView.setVisibility(GONE);
        binding.imgSetting.setVisibility(GONE);
        setUiAfterTakeAPhoto();
    }
}
