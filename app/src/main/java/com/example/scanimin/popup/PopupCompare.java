package com.example.scanimin.popup;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.scanimin.R;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.databinding.PopupCompareBinding;

public class PopupCompare extends Dialog {
    private PopupCompareListener listener;
    private final Context context;
    private AlertDialog alertDialog;
    private TextView btnConfirm;
    private String text;
    private int url;
    private Customer customer;
    private PopupCompareBinding binding;
    public interface PopupCompareListener {
        void onCompareUpdated();
    }
    public PopupCompare(String text, int url, Context context, Customer customer, PopupCompareListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.text = text;
        this.url = url;
        this.customer = customer;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        binding = PopupCompareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập kích thước cho Dialog
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        // Bo tròn các góc
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_corner_popup);

        binding.descriptionThankYou.setText(text);
        binding.titleThankYou.setImageResource(url);

        if (customer == null){
            binding.cdInformation.setVisibility(GONE);
            binding.cdImageCardView.setVisibility(GONE);
            binding.titleThankYou.setVisibility(VISIBLE);
            binding.viewLine.setVisibility(VISIBLE);
            binding.descriptionThankYou.setVisibility(VISIBLE);
        }else {
            if (text == null || url == 0){
                binding.titleThankYou.setVisibility(GONE);
                binding.viewLine.setVisibility(GONE);
                binding.descriptionThankYou.setVisibility(GONE);
            }else {
                binding.titleThankYou.setVisibility(VISIBLE);
                binding.viewLine.setVisibility(VISIBLE);
                binding.descriptionThankYou.setVisibility(VISIBLE);
            }
            binding.cdInformation.setVisibility(VISIBLE);
            binding.cdImageCardView.setVisibility(VISIBLE);
            binding.editName.setText(customer.getData().getName());
            binding.editTextCompany.setText(customer.getData().getCompany());
            binding.editTextPosition.setText(customer.getData().getPosition());
            binding.editAge.setText(String.valueOf(customer.getData().getTable()));
            String imageUri = customer.getUrl();
            Glide.with(context)
                    .load(imageUri)
                    .error(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .into(binding.imgUser);
            binding.editTextId.setText(customer.getQrcode());
        }

        binding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onCompareUpdated();
            }
        });
    }
}

