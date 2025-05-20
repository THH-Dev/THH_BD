package com.example.scanimin.ListCustomer;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.scanimin.File.ConverFile;
import com.example.scanimin.File.MinioUploader;
import com.example.scanimin.Fragment.ViewPager2.AllCustomer;
import com.example.scanimin.Fragment.ViewPager2.CustomerChecked;
import com.example.scanimin.Fragment.ViewPager2.CustomerNotChecked;
import com.example.scanimin.Fragment.ViewPager2.Searchable;
import com.example.scanimin.Fragment.ViewPager2.ViewPagerAdapter;
import com.example.scanimin.Fragment.CameraFragment;
import com.example.scanimin.R;
import com.example.scanimin.ScanImin.Scanner;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Local.CRUD;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Object.UpdateCustomer;
import com.example.scanimin.databinding.ListCustomerLayoutBinding;
import com.example.scanimin.popup.PopupCompare;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListCustomerActivity extends AppCompatActivity implements CameraFragment.OnUriCapturedListener,
        AllCustomer.OnItemClickListener,
        CustomerNotChecked.OnItemClickListener,
        CustomerChecked.OnItemClickListener {
    private RecyclerView recyclerView;
    private CustomerAdapter customerAdapter;
    private List<Customer> customerList;
    private SQLLite dbHelper;
    private ListCustomerLayoutBinding binding;
    private PopupCompare popupCompare;
    private CallApi callApi;
    private ViewPagerAdapter adapter;
    private String currentQuery = "";
    Uri uri;
    File imageFileCustomer;
    private CRUD crud;
    private ExecutorService executorService;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ListCustomerLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getData();
        init();
        setView(1,1,1,binding.viewCamera);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.view_camera, new CameraFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void getData(){
        callApi = new CallApi();
        callApi.getCustomer(this);
        dbHelper = new SQLLite(this);
        customer = new Customer();
        customerList = dbHelper.getAllPersons();
        crud = new CRUD();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void init(){
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
                setView(1,1,300,binding.viewCamera);
                binding.camera.setBackground(null);
                binding.listView.setVisibility(VISIBLE);
                binding.titleList.setVisibility(VISIBLE);
                binding.lnShowImage.setVisibility(GONE);
                binding.lnCameraList.setVisibility(VISIBLE);
                UpdateData();
                getData();

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
    private void reloadData() {
        new Handler().postDelayed(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
//                binding.swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ListCustomerActivity.this, "Dữ liệu đã được tải lại", Toast.LENGTH_SHORT).show();
                customerAdapter.notifyDataSetChanged();
            }
        }, 1000);
    }
    private void chupanh(){
        CameraFragment fragment = (CameraFragment) getSupportFragmentManager()
                .findFragmentById(R.id.view_camera);

        if (fragment != null) {
            fragment.CaptureImageAndSendUri();
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Scanner.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onUriCaptured(File file) throws IOException {
        uri = Uri.fromFile(file);
        binding.lnCameraList.setVisibility(GONE);
        binding.lnShowImage.setVisibility(VISIBLE);
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
    private void UpdateData(){
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
                callApi.getCustomer(ListCustomerActivity.this);
                crud.updateDB(customer, dbHelper, ListCustomerActivity.this);
            }
        });
    }

    @Override
    public void onItemClickedAll(Customer customer) {
        setView(600,600,300,binding.viewCamera);
        binding.camera.setBackgroundResource(R.drawable.kioskcheckinwithtitle);
        binding.listView.setVisibility(GONE);
        binding.titleList.setVisibility(GONE);
        this.customer = customer;
    }

    @Override
    public void onItemClickedNotChecked(Customer customer) {
        binding.camera.setBackgroundResource(R.drawable.kioskcheckinwithtitle);
        setView(600,600,300,binding.viewCamera);
        binding.listView.setVisibility(GONE);
        binding.titleList.setVisibility(GONE);
        this.customer = customer;
    }

    @Override
    public void onItemClickedChecked(Customer customer) {
        binding.camera.setBackgroundResource(R.drawable.kioskcheckinwithtitle);
        setView(600,600,300,binding.viewCamera);
        binding.listView.setVisibility(GONE);
        binding.titleList.setVisibility(GONE);
        this.customer = customer;
    }
}
