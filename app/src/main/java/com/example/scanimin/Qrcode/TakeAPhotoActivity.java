package com.example.scanimin.Qrcode;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.scanimin.File.MinioHelper;
import com.example.scanimin.Register.RegisterActivity;
import com.example.scanimin.ScanImin.Scanner;
import com.example.scanimin.data.Customer;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Data;
import com.example.scanimin.data.Local.CRUD;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.UpdateCustomer;
import com.example.scanimin.databinding.TakeAPhotoActivityBinding;
import com.example.scanimin.function.CameraUtils;
import com.example.scanimin.function.JsonUtils;
import com.example.scanimin.main.MainActivity;
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
    private int count = 3;

    private int time = 3000;
    private long backPressedTime = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TakeAPhotoActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getData();
        init();
    }
    private void init(){
        binding.imgConfirm.setVisibility(GONE);
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
        customer = new Customer();
        Intent intent = getIntent();
        if (intent.hasExtra("customer_new")) {
            Bundle bundle = intent.getBundleExtra("customer_new");
            String name = bundle.getString("name");
            String age = bundle.getString("age");
            String company = bundle.getString("company");
            String position = bundle.getString("position");
            String qrcode = bundle.getString("qrcode");
            Data data = new Data();
            data.setName(name);
            data.setAge(Integer.parseInt(age));
            data.setCompany(company);
            data.setPosition(position);
            customer.setData(data);
            customer.setQrcode(qrcode);
            customer.setImage(null);
            customer.setStatus(false);
            requestCheckin();
        }
    }

    private void requestCheckin(){
        binding.editName.setText(customer.getData().getName());
        binding.editTextId.setText(customer.getQrcode());
        binding.editTextCompany.setText(customer.getData().getCompany());
        binding.editTextPosition.setText(customer.getData().getPosition());
        binding.editAge.setText(String.valueOf(customer.getData().getAge()));
        isPhoto = false;
        binding.titleScan.setVisibility(VISIBLE);
        binding.lnInformation.setVisibility(VISIBLE);
        binding.previewView.setVisibility(GONE);
        binding.cdPreviewCardView.setVisibility(GONE);
        binding.textDescription.setVisibility(VISIBLE);
        binding.textRequestCamera.setVisibility(VISIBLE);
        binding.imgTakeAPhotoA.setVisibility(VISIBLE);
        binding.imgConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateData();
            }
        });
        binding.imgTakeAPhotoA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.previewView.setVisibility(VISIBLE);
                binding.cdPreviewCardView.setVisibility(VISIBLE);
                binding.imgTakeAPhotoA.setVisibility(GONE);
                binding.textRequestCamera.setVisibility(GONE);
                binding.textDescription.setVisibility(GONE);
                binding.viewLine.setVisibility(GONE);
                binding.timeCountDown.setVisibility(VISIBLE);
                countDown(3000);
            }
        });
    }

    private void countDown(int time){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (time > 0 && count >0) {
                    //count down
                    count--;
                    binding.timeCountDown.setText(String.valueOf(count));
                    countDown(time);
                }else{
                    takeAPhoto();
                }
            }
        }, time);
    }

    private void takeAPhoto(){
        // take a photo

        //update ui after take a photo
        setUiAfterTakeAPhoto();
    }

    private void UpdateData(){
        overlayDialogFragment = OverlayDialogFragment.newInstance();
        overlayDialogFragment.show(getSupportFragmentManager(), "OverlayDialog");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
//                updateCustomer = new UpdateCustomer();
//                customer.setImage(Uri.parse(imageFileCustomer.getName()));
//                updateCustomer.setImage(customer.getImage().toString());
//                customer.setStatus(true);
//                updateCustomer.setQrcode(customer.getQrcode());
//                crud.updateDB(customer, dbHelper, TakeAPhotoActivity.this);
//                minIOHelper.uploadImageToMinIO(imageFileCustomer);
//                callApi.updateCustomer(updateCustomer, new CallApi.UpdateCustomerListener() {
//                    @Override
//                    public void onUpdateCustomerSuccess() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                overlayDialogFragment.dismiss();
//                                popUpThankyou();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onUpdateCustomerFailure(String error) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                overlayDialogFragment.dismiss();
//                                popUpThankyou();
//                            }
//                        });
//                    }
//                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        overlayDialogFragment.dismiss();
                        popUpThankyou();
                    }
                });
            }
        });
    }

    private void setUiAfterTakeAPhoto(){
//        if (isTakePhoto){
            binding.imgConfirm.setVisibility(VISIBLE);
            binding.imgUser.setVisibility(VISIBLE);
            binding.cdImageCardView.setVisibility(VISIBLE);
            binding.fmLayoutImage.setVisibility(VISIBLE);
            binding.imgTakeAPhotoA.setVisibility(GONE);
            binding.textDescription.setVisibility(GONE);
            binding.textRequestCamera.setVisibility(GONE);
            binding.previewView.setVisibility(GONE);
            binding.cdPreviewCardView.setVisibility(GONE);
            binding.timeCountDown.setVisibility(GONE);
//        }
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
                startActivity(new Intent(TakeAPhotoActivity.this, Scanner.class));
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
        binding.timeCountDown.setVisibility(GONE);
        setUiAfterTakeAPhoto();
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime  > System.currentTimeMillis()) {
            super.onBackPressed();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
