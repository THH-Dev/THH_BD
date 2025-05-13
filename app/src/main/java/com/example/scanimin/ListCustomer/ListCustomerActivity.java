package com.example.scanimin.ListCustomer;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.scanimin.ScanImin.Scanner;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.databinding.ListCustomerLayoutBinding;
import com.example.scanimin.popup.PopupCompare;

import java.util.List;

public class ListCustomerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CustomerAdapter customerAdapter;
    private List<Customer> customerList;
    private SQLLite dbHelper;
    private ListCustomerLayoutBinding binding;
    private PopupCompare popupCompare;
    private CallApi callApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ListCustomerLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getData();
        init();
    }

    private void getData(){
        callApi = new CallApi();
        callApi.getCustomer(this);
        dbHelper = new SQLLite(this);
        customerList = dbHelper.getAllPersons();
    }

    private void init(){
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Create and set adapter
        if (customerList != null) {
            customerAdapter = new CustomerAdapter(customerList, this, new CustomerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Customer customer, View view) {
                    String string = "";
                    int url = 0;
                    popupCompare = new PopupCompare(string, url, ListCustomerActivity.this, customer, new PopupCompare.PopupCompareListener() {
                        @Override
                        public void onCompareUpdated() {
                            //bat lại scan
                        }
                    });
                    popupCompare.show();
                }
            });
            binding.recyclerView.setAdapter(customerAdapter);
            binding.imgBack.setOnClickListener(v -> {
                onBackPressed();
            });
            binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getData();
                    customerAdapter = new CustomerAdapter(customerList, ListCustomerActivity.this, new CustomerAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Customer customer, View view) {

                        }
                    });
                    reloadData();
                }
            });

            binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    customerAdapter.filter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    customerAdapter.filter(newText);
                    return true;
                }
            });
        }
    }
    private void reloadData() {
        new Handler().postDelayed(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                binding.swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ListCustomerActivity.this, "Dữ liệu đã được tải lại", Toast.LENGTH_SHORT).show();
                customerAdapter.notifyDataSetChanged();
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Scanner.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
