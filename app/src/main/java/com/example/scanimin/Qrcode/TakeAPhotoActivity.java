package com.example.scanimin.Qrcode;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.scanimin.File.ConverFile;
import com.example.scanimin.File.MinioHelper;
import com.example.scanimin.File.MinioUploader;
import com.example.scanimin.R;
import com.example.scanimin.ScanImin.Scanner;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Object.Data;
import com.example.scanimin.data.Local.CRUD;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Object.UpdateCustomer;
import com.example.scanimin.databinding.TakeAPhotoActivityBinding;
import com.example.scanimin.function.JsonUtils;
import com.example.scanimin.popup.OverlayDialogFragment;
import com.example.scanimin.popup.PopupCompare;
import com.example.scanimin.popup.PopupThankYou;
import com.imin.scan.Result;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class TakeAPhotoActivity extends AppCompatActivity implements CameraFragment.OnUriCapturedListener{

    private String TAGScan = "TakeAPhotoActivity";
    private TakeAPhotoActivityBinding binding;
    String receivedData;
    private PopupCompare popupCompare;
    private PopupThankYou popupThankYou;
    private PopupMenu popupMenu;
    private Result sb;
    private Customer customer;
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
    private Runnable dismissRunnable, runnable;
    private ImageView videoView;
    private int count = 3;
    private Uri contentUri;

    private UsbCameraManger cameraManager;

    private int time = 3000;
    private long backPressedTime = 0;
    private File imageFiles;
    private JsonUtils jsonUtils;
    private CameraFragment cameraFragment;
    private Handler idleHandler = new Handler();
    private Runnable idleRunnable;
    private static final int IDLE_TIMEOUT = 30000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TakeAPhotoActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getData();
        setView(1,1,12);
        init();
    }
    private void init(){
//        Glide.with(TakeAPhotoActivity.this)
//                .asGif()
//                .load(R.raw.background2) // có thể là URL, asset, hoặc file
//                .into(binding.imageBackground);
        binding.imgConfirm.setVisibility(GONE);
        binding.description.setVisibility(VISIBLE);
        popupThankYou = new PopupThankYou(this);
        dbHelper = new SQLLite(this);
        minIOHelper = new MinioHelper();
        callApi = new CallApi();
        crud = new CRUD();
        executorService = Executors.newSingleThreadExecutor();
        isPhoto = true;
        isTakePhoto = false;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ln_camera, new CameraFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!isTakePhoto){
                }else{

                }
                try {
                    settingUiCamera(3000);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    private void getData(){
        customer = new Customer();
        Intent intent = getIntent();
        if (intent.hasExtra("customer_new")) {
            Bundle bundle = intent.getBundleExtra("customer_new");
            String name = bundle.getString("name");
            String table = bundle.getString("table");
            String company = bundle.getString("company");
            String position = bundle.getString("position");
            String qrcode = bundle.getString("qrcode");
            Data data = new Data();
            data.setName(name);
            data.setTable(Integer.parseInt(table));
            data.setCompany(company);
            data.setPosition(position);
            customer.setData(data);
            customer.setQrcode(qrcode);
            customer.setImage(null);
            customer.setStatus(false);
            requestCheckin();
        }
    }

    private void setView(int widthInPd, int heightInPd, float radius){
        binding.cdPreviewCardView.setVisibility(VISIBLE);
        float density = binding.cdPreviewCardView.getResources().getDisplayMetrics().density;
        int widthIn = (int) (widthInPd * density + 0.5f);
        int heightIn = (int) (heightInPd * density + 0.5f);
        ViewGroup.LayoutParams layoutParams = binding.cdPreviewCardView.getLayoutParams();
        layoutParams.width = widthIn;
        layoutParams.height = heightIn;

        binding.cdPreviewCardView.setLayoutParams(layoutParams);
        float radiusInPx = radius * density;
        binding.cdPreviewCardView.setRadius(radiusInPx);
    }

    private void setting(View v){
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
                        binding.imgTakeAPhotoA.setImageResource(R.drawable.camera_blue);
                        binding.imgTakeAPhoto.setVisibility(VISIBLE);
                        try {
                            settingUiCamera(0);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return true;
                    }
                    if (id == R.id.menu_item_1) {
                        binding.imgTakeAPhoto.setImageResource(R.drawable.icon_setting);
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
                                    time = 3000;
                                    try {
                                        settingUiCamera(time);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return true;
                                }
                                if (id ==R.id.menu_item_1) {
                                    time = 5000;
                                    try {
                                        settingUiCamera(time);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return true;
                                }
                                if (id ==R.id.menu_item_2) {
                                    time = 10000;
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
                    return false;
                }
            });
            popupMenu.show();
        }
    }

    private void requestCheckin(){
        binding.editName.setText(customer.getData().getName());
        binding.editTextId.setText(customer.getQrcode());
        binding.editTextCompany.setText(customer.getData().getCompany());
        binding.editTextPosition.setText(customer.getData().getPosition());
        binding.editAge.setText(String.valueOf(customer.getData().getTable()));
        isPhoto = false;
        binding.lnInformation.setVisibility(VISIBLE);
        binding.textDescription.setVisibility(GONE);
        binding.textRequestCamera.setVisibility(GONE);
        binding.imgTakeAPhotoA.setVisibility(VISIBLE);
        binding.description.setVisibility(GONE);
        binding.imgConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateData();
            }
        });
        binding.imgTakeAPhotoA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting(v);
            }
        });
    }

    private void settingUiCamera(int time) throws IOException {
        binding.description.setVisibility(GONE);
        binding.imgTakeAPhotoA.setVisibility(VISIBLE);
        binding.textRequestCamera.setVisibility(GONE);
        binding.textDescription.setVisibility(GONE);
        binding.viewLine.setVisibility(GONE);
        binding.timeCountDown.setVisibility(GONE);
        binding.cdPreviewCardView.setVisibility(VISIBLE);
        binding.videoCountdown.setVisibility(VISIBLE);
        binding.lnImage.setVisibility(VISIBLE);
        setView(700,500,12);
        if (time !=0){
            binding.iconCamera.setVisibility(GONE);
            binding.lnImage.setVisibility(GONE);
            binding.videoCountdown.setVisibility(VISIBLE);
            countDown(time);
        }else{
            binding.iconCamera.setVisibility(VISIBLE);
            binding.imgTakeAPhoto.setVisibility(VISIBLE);
            binding.imgConfirm.setVisibility(GONE);
            binding.lnImage.setVisibility(GONE);
        }
        binding.imgTakeAPhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                {
                    binding.cdPreviewCardView.setVisibility(VISIBLE);
                    binding.videoCountdown.setVisibility(VISIBLE);
                    binding.iconCamera.setVisibility(GONE);
                    binding.lnImage.setVisibility(GONE);
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
            binding.iconCamera.setVisibility(VISIBLE);
            binding.imgTakeAPhoto.setVisibility(VISIBLE);
            binding.imgConfirm.setVisibility(VISIBLE);
        }
    }

    private void countDown(int time) throws IOException {
        int video = R.raw.countdown;
        if (time == 3000){
            video = R.raw.countdown;
        }
        if (time == 5000){
            video = R.raw.down5;
        }
        if (time == 8000){
            video = R.raw.down8;
        }
        GifImageView gifImageView = findViewById(R.id.gifImageView);

        GifDrawable gifDrawable = new GifDrawable(getResources(), video);
        gifDrawable.setLoopCount(1);
        gifImageView.setImageDrawable(gifDrawable);
        gifDrawable.start();
        binding.videoCountdown.setVisibility(GONE);

//        Glide.with(this).clear(binding.videoCountdown);
//        Glide.with(TakeAPhotoActivity.this)
//                .asGif()
//                .load(video)
//                .into(binding.videoCountdown);
        new Handler(Looper.getMainLooper()).postDelayed(
                TakeAPhotoActivity.this::takeAPhoto, time
        );
    }
    private void takeAPhoto(){
        // take a photo
        chupanh();
        binding.cdPreviewCardView.setVisibility(GONE);
        binding.videoCountdown.setVisibility(GONE);
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
                callApi.getCustomer(TakeAPhotoActivity.this);
                crud.updateDB(customer, dbHelper, TakeAPhotoActivity.this);
            }
        });
    }

    private void setUiAfterTakeAPhoto(){
            binding.imgConfirm.setVisibility(VISIBLE);
            binding.imgUser.setVisibility(VISIBLE);
            binding.cdImageCardView.setVisibility(VISIBLE);
            binding.imgTakeAPhotoA.setVisibility(GONE);
            binding.textDescription.setVisibility(GONE);
            binding.textRequestCamera.setVisibility(GONE);
            binding.timeCountDown.setVisibility(GONE);
            binding.description.setVisibility(GONE);
            binding.cdPreviewCardView.setVisibility(GONE);
            binding.lnImage.setVisibility(VISIBLE);
    }

    @SuppressLint("ResourceType")

    private void popUpThankyou(){
        popupThankYou.setCanceledOnTouchOutside(false);
        popupThankYou.show();
        dismissRunnable = new Runnable() {
            @Override
            public void run() {
                if (popupThankYou.isShowing()) {
                    popupThankYou.dismiss();
                }
                startActivity(new Intent(TakeAPhotoActivity.this, Scanner.class));
                finish();
            }
        };
        handler.postDelayed(dismissRunnable, 2000);
    }

    // No back
    @Override
    public void onBackPressed() {
        if (backPressedTime  > System.currentTimeMillis()) {
            super.onBackPressed();
        }
        backPressedTime = System.currentTimeMillis();
    }

    // back to previous activity
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent intent = new Intent(this, Scanner.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        finish();
//    }

    public void getScreenshotImages(Context context) {
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
        };

        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + Environment.DIRECTORY_DOWNLOADS + "%"};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);

                contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                // Xử lý ảnh ở đây (ví dụ: in ra tên và Uri)
                Log.d("Screenshot", "Tên: " + name + ", Uri: " + contentUri.toString());
            }
            cursor.close();
        }
    }

    @Override
    public void onUriCaptured(File file) throws IOException {
        getScreenshotImages(this);
        Uri uri = Uri.fromFile(file);
//        imageFileCustomer = file;
        imageFileCustomer = ConverFile.cropImageFileToSquare720(file, this);
        MinioUploader.uploadImage(imageFileCustomer, imageFileCustomer.getName());
        Log.d("Screenshot", "Uri: " + uri.toString());
        if (uri != null) {
            Glide.with(this)
                    .load(uri)
                    .error(R.drawable.teamwork)
                    .into(binding.imgUser);
        }else {
            Glide.with(this)
                    .load(contentUri)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.teamwork)
                    .into(binding.imgUser);
        }
    }

    private void startIdleTimer() {
        idleRunnable = () -> {
            startActivity(new Intent(this, Scanner.class));
            finish();
        };
        idleHandler.postDelayed(idleRunnable, IDLE_TIMEOUT);
    }

    private void resetIdleTimer() {
        idleHandler.removeCallbacks(idleRunnable);
        idleHandler.postDelayed(idleRunnable, IDLE_TIMEOUT);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onResume() {
        super.onResume();
//        startIdleTimer();

//        View rootView = findViewById(android.R.id.content);
//        rootView.setOnTouchListener((v, event) -> {
//            resetIdleTimer();
//            return false;
//        });
    }

    @Override
    protected void onPause() {
        super.onPause();
//        idleHandler.removeCallbacks(idleRunnable);
    }

}
