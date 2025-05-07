package com.example.scanimin.Register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scanimin.Qrcode.TakeAPhotoActivity;
import com.example.scanimin.ScanImin.Scanner;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.DBRemote.CallApi;
import com.example.scanimin.data.Object.Data;
import com.example.scanimin.data.Local.CRUD;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Object.PostCustomer;
import com.example.scanimin.databinding.RegisterLayoutBinding;

import java.util.Random;

public class RegisterActivity extends AppCompatActivity {
    private RegisterLayoutBinding binding;
    private Customer customer;
    private CallApi callApi;
    private PostCustomer postCustomer;
    private SQLLite dbHelper;
    private static final String alpha = "abcdefghijklmnopqrstuvwxyz";
    private static final String alphaUpperCase = alpha.toUpperCase();

    private static final String UPPERCASE_AND_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String digits = "0123456789";
    private static final String specials = "~=+%^*/()[]{}/!@#$?|";
    private static final String ALPHA_NUMERIC = alpha + alphaUpperCase + digits;
    private static final String ALL = alpha + alphaUpperCase + digits + specials;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RegisterLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        customer = new Customer();
        dbHelper = new SQLLite(this);
        callApi = new CallApi();
        postCustomer = new PostCustomer();
        init();
    }
    private void init() {
        Random generator = new Random();
        binding.imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, Scanner.class);
            startActivity(intent);
        });
        binding.btnRegister.setOnClickListener(v -> {
            Data data = new Data();
            data.setName(binding.editName.getText().toString());
            try {
                int number = Integer.parseInt(binding.editAge.getText().toString());
                data.setTable(number);
            } catch (NumberFormatException e) {
                data.setTable(0);
                e.printStackTrace();
            }
            data.setCompany(binding.editCompany.getText().toString());
            data.setPosition(binding.editPosition.getText().toString());
            data.setRole("uninvited");
            customer.setData(data);
            customer.setStatus(false);
            customer.setImage(null);
            customer.setQrcode(randomUpperCaseAndDigits(5));
            Bundle bundle = new Bundle();
            bundle.putString("name", binding.editName.getText().toString());
            bundle.putString("age", binding.editAge.getText().toString());
            bundle.putString("company", binding.editCompany.getText().toString());
            bundle.putString("position", binding.editPosition.getText().toString());
            bundle.putString("qrcode", customer.getQrcode());
            Intent intent = new Intent(RegisterActivity.this, TakeAPhotoActivity.class);
            intent.putExtra("customer_new", bundle);
            postCustomer.setData(data);
            postCustomer.setQrcode(customer.getQrcode());
            insertData();
            insertSQlite();
            startActivity(intent);
            finish();
        });
    }
    private void insertData(){
        callApi.insertCustomer(postCustomer);
    }

    private void insertSQlite(){
        CRUD crud = new CRUD();
        crud.inserDB(customer, dbHelper, this);
        for (Customer customer : dbHelper.getAllPersons()){
            Log.d("MainActivity", "Customer name :" + customer.getData().getName());
            Log.d("MainActivity", "Customer age :" + customer.getData().getTable());
            Log.d("MainActivity", "Customer company :" + customer.getData().getCompany());
            Log.d("MainActivity", "Customer position :" + customer.getData().getPosition());
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Scanner.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
