package com.example.scanimin.popup;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.scanimin.R;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.databinding.LayoutPopupEnounceBinding;
import com.example.scanimin.databinding.PopupCompareBinding;

public class PopupEnounce extends Dialog{

    private LayoutPopupEnounceBinding binding;
    private Context context;
    private String text;
    private int url;
    private Customer customer;
    private PopupCompareListener listener;
    public interface PopupCompareListener {
        void onCompareUpdated();
    }
    public PopupEnounce(Context context, PopupEnounce.PopupCompareListener listener) {
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
        binding = LayoutPopupEnounceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập kích thước cho Dialog
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        // Bo tròn các góc
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_corner_popup);
        binding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onCompareUpdated();
            }
        });
    }
}
