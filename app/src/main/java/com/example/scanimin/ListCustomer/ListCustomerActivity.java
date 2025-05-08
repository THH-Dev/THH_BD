package com.example.scanimin.ListCustomer;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scanimin.ScanImin.Scanner;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ListCustomerLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getData();
        init();
    }

    private void getData(){
        dbHelper = new SQLLite(this);
        customerList = dbHelper.getAllPersons();
        Log.d("ListCustomerActivity", "Customer List Size: " + customerList.size());
    }

    private void init(){

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Create and set adapter
        if (customerList != null) {
            customerAdapter = new CustomerAdapter(customerList, this, new CustomerAdapter.OnItemClickListener(){
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
//            adapterView = new AdapterView(customerList);
//            binding.recyclerView.setAdapter(adapterView);
        }
        binding.imgBack.setOnClickListener(v -> {
            onBackPressed();
        });
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
