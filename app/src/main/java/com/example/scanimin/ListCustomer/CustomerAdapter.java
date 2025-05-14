package com.example.scanimin.ListCustomer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.scanimin.Qrcode.TakeAPhotoActivity;
import com.example.scanimin.R;
import com.example.scanimin.data.Object.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Customer customer, View view);
    }
    public interface OnItemCameraListener {
        void onItemClick(Customer customer, View view);
    }
    private List<Customer> filteredList;
    private List<Customer> customerList;
    private Context context;
    private OnItemClickListener listener;
    private OnItemCameraListener listener1;

    public CustomerAdapter(List<Customer> customerList, Context context, OnItemClickListener listener, OnItemCameraListener listener1) {
        this.customerList = new ArrayList<>(customerList);
        this.context = context;
        this.listener = listener;
        this.listener1 = listener1;
        this.filteredList = new ArrayList<>(customerList);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String keyword) {
        keyword = keyword.toLowerCase(Locale.ROOT);
        filteredList.clear();

        if (keyword.isEmpty()) {
            for (Customer item : customerList) {
                if (item.getData().getName() != null) {
                    filteredList.add(item);
                }
            }
        } else {
            for (Customer item : customerList) {
                if (item.getData().getName() != null && item.getData().getName().toLowerCase(Locale.ROOT).contains(keyword)) {
                    filteredList.add(item);
                }
            }
        }

        notifyDataSetChanged();
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
        Customer customer = filteredList.get(position);
        if (customer.getData().getName() != null) {
            holder.textName.setText(customer.getData().getName());
            holder.textCompany.setText(customer.getData().getCompany());
            holder.textPosition.setText(customer.getData().getPosition());
            if (customer.getImage() != null) {
                holder.status.setText(R.string.yes);
            } else {
                holder.status.setText(R.string.no);
            }
            String imageUri = customer.getUrl();
            Glide.with(context)
                    .load(imageUri)
                    .transform(new RoundedCorners(30))
                    .error(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .into(holder.imgUser);
            holder.refresh.setOnClickListener(v -> {
                if (listener1 != null) {
                    listener1.onItemClick(customer, v);
                }
            });
            holder.detail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(customer, v);
                }
            });
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Customer> newList) {
        this.customerList.clear();
        this.customerList.addAll(newList);

        this.filteredList.clear();
        this.filteredList.addAll(newList);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        public TextView textName, textAge, textCompany, textPosition, status, refresh, detail;
        public ImageView imgUser;

        public CustomerViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
//            textAge = itemView.findViewById(R.id.text_age);
            textCompany = itemView.findViewById(R.id.text_company);
            textPosition = itemView.findViewById(R.id.text_position);
            imgUser = itemView.findViewById(R.id.img_user);
            status = itemView.findViewById(R.id.status);
            refresh = itemView.findViewById(R.id.refresh);
            detail = itemView.findViewById(R.id.detail);
        }
    }
}
