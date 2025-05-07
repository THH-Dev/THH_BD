package com.example.scanimin.data.DBRemote;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Object.CustomerApi;
import com.example.scanimin.data.Object.PostCustomer;
import com.example.scanimin.data.Object.UpdateCustomer;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiInterface {
    @GET("/api/get_guests")
    Call<List<CustomerApi>> getCustomer();
    @POST("/customers")
    Call<Customer> postCustomer(@Body PostCustomer postCustomer);
    @POST("/api/update_guest")
    Call<Customer> updateCustomerByQrcode(@Body UpdateCustomer updateCustomer);
    @POST("/api/insert_guest")
    Call<PostCustomer> insertCustomerByQrcode(@Body PostCustomer postCustomer);
    @PUT("/customers/{id}")
    Call<Customer> updateCustomer(@Path("id") String id, @Body Customer customer);
    @DELETE("/customers/{id}")
    Call<Void> deleteCustomer(@Path("id") String id);
}
