package com.example.scanimin.data.DBRemote;

import android.content.Context;
import android.util.Log;

import com.example.scanimin.data.Customer;
import com.example.scanimin.data.Local.SQLLite;
import com.example.scanimin.data.UpdateCustomer;

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

        Call<List<Customer>> call = apiInterface.getCustomer();
        call.enqueue(new Callback<List<Customer>>() {
            @Override
            public void onResponse(Call<List<Customer>> call, Response<List<Customer>> response) {
                if (response.isSuccessful()) {
                    List<Customer> customers = response.body();
                    for (Customer customer : customers) {
                        Log.d("MainActivity", "Customer :" +
                                ", Name: " + customer.getData().getName()
                                + ", age: " + customer.getData().getAge()
                                + ", company: " + customer.getData().getCompany()
                                + ", Image: " + customer.getImage()
                                +", qrcode: " + customer.getQrcode()
                                + ", status: " + customer.getStatus());
                        List<Customer> customerList = sqlLite.getAllPersons();
                        if (!isCustomerInList(customer, customerList)) {
                            sqlLite.insertUser(customer);
                        }
                    }
                    customerList = response.body();
                } else {
                    Log.e("MainActivity", "Request failed: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<Customer>> call, Throwable t) {
                Log.e("MainActivity", "Request failed: " + t.getMessage());
            }
        });
    }

    public void postCustomer(Customer customer) {
        ApiInterface apiInterface = Retrofit2.getInstance().getApiInterface();
        Call<Customer> call = apiInterface.postCustomer(customer);
        call.enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if (response.isSuccessful()) {
                    Customer postedCustomer = response.body();
                    Log.d("CallApi", "Customer posted successfully: " + postedCustomer.getData().getName());
                    // Xử lý response ở đây nếu cần (ví dụ: cập nhật UI)
                } else {
                    Log.e("CallApi", "Failed to post customer. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
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
