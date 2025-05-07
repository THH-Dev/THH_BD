package com.example.scanimin.data.DBRemote;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Object.CustomerApi;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.Object.PostCustomer;
import com.example.scanimin.data.Object.UpdateCustomer;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallApi{

    private SQLLite sqlLite;

    private List<Customer> customerList;
    public interface UpdateCustomerListener{
        void onUpdateCustomerSuccess();
        void onUpdateCustomerFailure(String error);
    }
    public void getCustomer(Context context) {

        sqlLite = new SQLLite(context);
        customerList = new ArrayList<>();
        ApiInterface apiInterface = Retrofit2.getInstance().getApiInterface();

        Call<List<CustomerApi>> call = apiInterface.getCustomer();
        call.enqueue(new Callback<List<CustomerApi>>() {
            @Override
            public void onResponse(Call<List<CustomerApi>> call, Response<List<CustomerApi>> response) {
                if (response.isSuccessful()) {
                    List<Customer> customers = new ArrayList<>();
                    List<CustomerApi> customerApis = response.body();
                    for (CustomerApi customer : customerApis) {
                        if (customer.getImage() != null) {
                            Customer customerdata = new Customer(customer.getData(), Uri.parse(customer.getImage()), customer.getQrcode(), customer.getStatus(), customer.getTimestamp(), customer.getUrl());
                            customers.add(customerdata);
                        }else {
                            Customer customerdata = new Customer(customer.getData(), null, customer.getQrcode(), customer.getStatus(), customer.getTimestamp(), customer.getUrl());
                            customers.add(customerdata);
                        }
                        Log.d("MainActivity", "Customer :" +
                                ", Name: " + customer.getData().getName()
                                + ", table: " + customer.getData().getTable()
                                + ", company: " + customer.getData().getCompany()
                                + ", Image: " + customer.getImage()
                                +", qrcode: " + customer.getQrcode()
                                + ", status: " + customer.getStatus()
                                + ", timestamp: " + customer.getTimestamp()
                                + ", url: " + customer.getUrl());
                    }
                    for (Customer customer : customers) {
                        List<Customer> customerList = sqlLite.getAllPersons();
                        if (!isCustomerInList(customer, customerList)) {
                            sqlLite.insertUser(customer);
                        }
                    }
                } else {
                    Log.e("MainActivity", "Request failed: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<CustomerApi>> call, Throwable t) {
                Log.e("MainActivity", "Request failed: " + t.getMessage());
            }
        });
    }

    public void insertCustomer(PostCustomer postCustomer) {
        ApiInterface apiInterface = Retrofit2.getInstance().getApiInterface();
        Call<PostCustomer> call = apiInterface.insertCustomerByQrcode(postCustomer);
        call.enqueue(new Callback<PostCustomer>() {
            @Override
            public void onResponse(Call<PostCustomer> call, Response<PostCustomer> response) {
                if (response.isSuccessful()) {
//                    Customer postedCustomer = response.body();
                    Log.d("CallApi", "Customer posted successfully: ");
                    // Xử lý response ở đây nếu cần (ví dụ: cập nhật UI)
                } else {
                    Log.e("CallApi", "Failed to post customer. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PostCustomer> call, Throwable t) {
                Log.e("CallApi", "Failed to post customer. Error: " + t.getMessage());
            }
        });
    }

    public void updateCustomer(UpdateCustomer updateCustomer, UpdateCustomerListener listener) {
        ApiInterface apiInterface = Retrofit2.getInstance().getApiInterface();
        Call<Customer> call = apiInterface.updateCustomerByQrcode(updateCustomer);
        call.enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if (response.isSuccessful()) {
                    Customer updatedCustomer = response.body();
                    listener.onUpdateCustomerSuccess();
                } else {
                    listener.onUpdateCustomerFailure("Failed to update customer");
                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                listener.onUpdateCustomerFailure(t.getMessage());
            }
        });
    }

    public void deleteCustomer(String customerId) {
        ApiInterface apiInterface = Retrofit2.getInstance().getApiInterface();
        Call<Void> call = apiInterface.deleteCustomer(customerId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("CallApi", "Customer deleted successfully: " );
                    // Xử lý response ở đây nếu cần
                } else {
                    Log.e("CallApi", "Failed to delete customer. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CallApi", "Failed to delete customer. Error: " + t.getMessage());
            }
        });
    }
    public static boolean isCustomerInList(Customer customer, List<Customer> customerList) {
        for (Customer item : customerList) {
            if (customer.getQrcode().equals(item.getQrcode())) {
                return true;
            }
        }
        return false;
    }
}
