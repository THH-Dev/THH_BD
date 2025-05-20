package com.example.scanimin.Qrcode;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.serenegiant.utils.UIThreadHelper.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.scanimin.File.ConverFile;
import com.example.scanimin.File.MinioHelper;
import com.example.scanimin.File.MinioUploader;
import com.example.scanimin.Fragment.CameraFragment;
import com.example.scanimin.R;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Local.CRUD;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Object.UpdateCustomer;
import com.example.scanimin.databinding.TakeAPhotoActivityBinding;
import com.example.scanimin.function.FunctionUtils;
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

public class TakeAPhotoFragment extends Fragment implements CameraFragment.OnUriCapturedListener {

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

    private static int time = 3000;
    private long backPressedTime = 0;
    private File imageFiles;
    private FunctionUtils jsonUtils;
    private CameraFragment cameraFragment;
    private Handler idleHandler = new Handler();
    private Runnable idleRunnable;
    private static final int IDLE_TIMEOUT = 30000;
    private Activity mActivity;
    private View mView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.take_a_photo_activity, container, false);
        mActivity = getActivity();
        return mView;
    }
    private void init(){
        binding.imgConfirm.setVisibility(GONE);
        binding.description.setVisibility(VISIBLE);
        dbHelper = new SQLLite(requireActivity());
        minIOHelper = new MinioHelper();
        callApi = new CallApi();
        crud = new CRUD();
        executorService = Executors.newSingleThreadExecutor();
        isPhoto = true;
        isTakePhoto = false;
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
        if (getArguments() != null) {
            String data = getArguments().getString("customer_new");
            // sử dụng data
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
        PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(requireActivity(), R.style.PopupMenuStyle), v);
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
                        PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(requireActivity(), R.style.PopupMenuStyle), v);
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
        binding.description.setVisibility(GONE);
        binding.imgConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateData();
            }
        });
    }

    private void settingUiCamera(int time) throws IOException {
        binding.description.setVisibility(GONE);
        binding.textDescription.setVisibility(GONE);
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
//        CameraFragment fragment = (CameraFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.ln_camera);

//        if (fragment != null) {
//            fragment.CaptureImageAndSendUri();
//            binding.iconCamera.setVisibility(VISIBLE);
//            binding.imgTakeAPhoto.setVisibility(VISIBLE);
//            binding.imgConfirm.setVisibility(VISIBLE);
//        }
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
        GifImageView gifImageView = mView.findViewById(R.id.gifImageView);

        GifDrawable gifDrawable = new GifDrawable(getResources(), video);
        gifDrawable.setLoopCount(1);
        gifImageView.setImageDrawable(gifDrawable);
        gifDrawable.start();
        binding.videoCountdown.setVisibility(GONE);
        new Handler(Looper.getMainLooper()).postDelayed(
                TakeAPhotoFragment.this::takeAPhoto, time
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
                callApi.getCustomer(requireActivity());
                crud.updateDB(customer, dbHelper, requireActivity());
            }
        });
    }

    private void setUiAfterTakeAPhoto(){
            binding.imgConfirm.setVisibility(VISIBLE);
            binding.imgUser.setVisibility(VISIBLE);
            binding.cdImageCardView.setVisibility(VISIBLE);
            binding.textDescription.setVisibility(GONE);
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
            }
        };
        handler.postDelayed(dismissRunnable, 2000);
    }

    // No back

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
        getScreenshotImages(requireActivity());
        Uri uri = Uri.fromFile(file);
//        imageFileCustomer = file;
        imageFileCustomer = ConverFile.cropImageFileToSquare720(file, requireActivity());
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
}
