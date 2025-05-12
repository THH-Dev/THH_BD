package com.example.scanimin.data.Newdata;

import android.net.Uri;

import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Object.Data;

import java.util.ArrayList;

public class NewCustomer {
    public static ArrayList<Customer> newList = new ArrayList<Customer>();
    public static void addNewCustomer(){
        if (newList.isEmpty()) {
            for (int i = 54; i <= 150; i++) {
                newList.add(new Customer(new Data("",  "", "", 0, ""), null, "CUS0" + String.valueOf(i), false, null, null));
            }
        }
    }
    public static Customer getCustomer(){
        if (!newList.isEmpty()) {
            Customer customer = new Customer();
            for (int i = 1; i <= 16; i++) {
                customer = newList.get(i);
                break;
            }
            newList.remove(1);
            return customer;
        }else{
            return null;
        }
    }
}
