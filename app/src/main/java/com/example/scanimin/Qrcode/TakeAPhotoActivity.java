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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.scanimin.File.MinioHelper;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private Runnable dismissRunnable;
    private VideoView videoView;
    private int count = 3;
    private Uri contentUri;

    private UsbCameraManger cameraManager;

    private int time = 3000;
    private long backPressedTime = 0;
    private File imageFiles;
    private JsonUtils jsonUtils;
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
        minIOHelper = new MinioHelper();
        callApi = new CallApi();
        crud = new CRUD();
        executorService = Executors.newSingleThreadExecutor();
        isPhoto = true;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ln_camera, new CameraFragment());
        transaction.addToBackStack(null);
        transaction.commit();
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
            data.setTable(Integer.parseInt(age));
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
        binding.editAge.setText(String.valueOf(customer.getData().getTable()));
        isPhoto = false;
        binding.titleScan.setVisibility(VISIBLE);
        binding.lnInformation.setVisibility(VISIBLE);
        binding.textDescription.setVisibility(VISIBLE);
        binding.textRequestCamera.setVisibility(VISIBLE);
        binding.imgTakeAPhotoA.setVisibility(VISIBLE);
        binding.cdPreviewCardView.setVisibility(GONE);
        binding.imgConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateData();
            }
        });
        binding.imgTakeAPhotoA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imgTakeAPhotoA.setVisibility(GONE);
                binding.textRequestCamera.setVisibility(GONE);
                binding.textDescription.setVisibility(GONE);
                binding.viewLine.setVisibility(GONE);
                binding.timeCountDown.setVisibility(VISIBLE);
                binding.lnCamera.setVisibility(VISIBLE);
                binding.cdPreviewCardView.setVisibility(VISIBLE);
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
        binding.lnCamera.setVisibility(GONE);
        binding.cdPreviewCardView.setVisibility(GONE);
//        getScreenshotImages(this);
//        List<File> imageFiles =  JsonUtils.getImagesFromDirectory();
//        Uri uri = Uri.fromFile(imageFiles.get(0));
////        }
//        //update ui after take a photo
//        imageFileCustomer = imageFiles.get(0);
//        Glide.with(this)
//                .load(Uri.fromFile(imageFileCustomer))
//                .placeholder(R.drawable.user)
//                .error(R.drawable.teamwork)
//                .into(binding.imgUser);

        setUiAfterTakeAPhoto();
        //image from library
    }




    private void UpdateData(){
        overlayDialogFragment = OverlayDialogFragment.newInstance();
        overlayDialogFragment.show(getSupportFragmentManager(), "OverlayDialog");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                updateCustomer = new UpdateCustomer();
                customer.setImage(Uri.parse(imageFileCustomer.getName()));
                updateCustomer.setImage(customer.getImage().toString());
                customer.setStatus(true);
                updateCustomer.setQrcode(customer.getQrcode());
                crud.updateDB(customer, dbHelper, TakeAPhotoActivity.this);
                minIOHelper.uploadImageToMinIO(imageFileCustomer);
                callApi.updateCustomer(updateCustomer, new CallApi.UpdateCustomerListener() {
                    @Override
                    public void onUpdateCustomerSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                overlayDialogFragment.dismiss();
                                popUpThankyou();
                            }
                        });
                    }

                    @Override
                    public void onUpdateCustomerFailure(String error) {
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
            binding.timeCountDown.setVisibility(GONE);
            binding.lnCamera.setVisibility(GONE);
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

    @Override
    public void onBackPressed() {
        if (backPressedTime  > System.currentTimeMillis()) {
            super.onBackPressed();
        }
        backPressedTime = System.currentTimeMillis();
    }

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
    public void onUriCaptured(File file) {
        getScreenshotImages(this);
        Uri uri = Uri.fromFile(file);
        imageFileCustomer = file;
        Log.d("Screenshot", "Uri: " + uri.toString());
        if (uri != null) {
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.teamwork)
                    .into(binding.imgUser);
        }else {
            Glide.with(this)
                    .load(contentUri)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.teamwork)
                    .into(binding.imgUser);
        }
        //update ui after take a photo
//        imageFileCustomer = new File(contentUri.getPath());
    }
}
