package com.example.scanimin.ListCustomer;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scanimin.R;
import com.example.scanimin.data.Customer;
import com.example.scanimin.data.Data;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private List<Customer> customerList;
    public CustomerAdapter(List<Customer> customerList) {
        this.customerList = customerList;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.textName.setText(customer.getData().getName());
        holder.textAge.setText(String.valueOf(customer.getData().getAge()));
        holder.textCompany.setText(customer.getData().getCompany());
        holder.textPosition.setText(customer.getData().getPosition());
        Uri imageUri = customer.getImage();
        if (customer.getImage() != null){
            holder.imgUser.setImageURI(customer.getImage());
        }else holder.imgUser.setImageResource(R.drawable.teamwork);
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        public TextView textName, textAge, textCompany, textPosition;
        public ImageView imgUser;

        public CustomerViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textAge = itemView.findViewById(R.id.text_age);
            textCompany = itemView.findViewById(R.id.text_company);
            textPosition = itemView.findViewById(R.id.text_position);
            imgUser = itemView.findViewById(R.id.img_user);
        }
    }
}
