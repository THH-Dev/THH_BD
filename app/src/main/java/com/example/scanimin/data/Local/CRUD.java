package com.example.scanimin.data.Local;

import android.content.Context;
import android.widget.Toast;

import com.example.scanimin.data.Customer;

import java.util.List;

public class CRUD {
    public void inserDB(String name, int age, String company, String position, String role, String qrcode, String image, Boolean status, String timestamp, String url, SQLLite dbHelper, Context context){
        try {
            dbHelper.insertUser(name, age, company, position, role, qrcode, image, status, timestamp, url);
            dbHelper.close();
        }catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void inserDB(Customer customer, SQLLite dbHelper, Context context){
        try {
            dbHelper.insertUser(customer);
            dbHelper.close();
        }catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateDB(Customer customer, SQLLite dbHelper, Context context) {
        dbHelper.updateUser(customer);
        dbHelper.close();
    }
}
