package com.example.scanimin.Register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scanimin.Qrcode.TakeAPhotoActivity;
import com.example.scanimin.ScanImin.Scanner;
import com.example.scanimin.data.Customer;
import com.example.scanimin.data.Data;
import com.example.scanimin.databinding.RegisterLayoutBinding;

import java.util.Random;

public class RegisterActivity extends AppCompatActivity {
    private RegisterLayoutBinding binding;
    private Customer customer;

    private static final String alpha = "abcdefghijklmnopqrstuvwxyz";
    private static final String alphaUpperCase = alpha.toUpperCase();
    private static final String digits = "0123456789";
    private static final String specials = "~=+%^*/()[]{}/!@#$?|";
    private static final String ALPHA_NUMERIC = alpha + alphaUpperCase + digits;
    private static final String ALL = alpha + alphaUpperCase + digits + specials;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RegisterLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }
    private void init() {
        customer = new Customer();
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
                data.setAge(number);
            } catch (NumberFormatException e) {
                data.setAge(0);
                e.printStackTrace();
            }
            data.setCompany(binding.editCompany.getText().toString());
            data.setPosition(binding.editPosition.getText().toString());
            customer.setData(data);
            customer.setStatus(false);
            customer.setImage(null);
            customer.setQrcode(randomAlphaNumeric(4));
            Bundle bundle = new Bundle();
            bundle.putString("name", binding.editName.getText().toString());
            bundle.putString("age", binding.editAge.getText().toString());
            bundle.putString("company", binding.editCompany.getText().toString());
            bundle.putString("position", binding.editPosition.getText().toString());
            bundle.putString("qrcode", customer.getQrcode());
            Intent intent = new Intent(RegisterActivity.this, TakeAPhotoActivity.class);
            intent.putExtra("customer_new", bundle);
            startActivity(intent);
        });
    }

    public String randomAlphaNumeric(int numberOfCharactor) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfCharactor; i++) {
            int number = randomNumber(0, ALPHA_NUMERIC.length() - 1);
            char ch = ALPHA_NUMERIC.charAt(number);
            sb.append(ch);
        }
        return sb.toString();
    }

    public static int randomNumber(int min, int max) {
        return generator.nextInt((max - min) + 1) + min;
    }

    private static Random generator = new Random();
}
