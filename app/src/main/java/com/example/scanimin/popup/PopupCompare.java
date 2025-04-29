package com.example.scanimin.popup;

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

import com.example.scanimin.R;
import com.example.scanimin.databinding.PopupCompareBinding;

public class PopupCompare extends Dialog {
    private PopupCompareListener listener;
    private final Context context;
    private AlertDialog alertDialog;
    private TextView btnConfirm;
    private String text;
    private int url;
    private PopupCompareBinding binding;
    public interface PopupCompareListener {
        void onCompareUpdated();
    }
    public PopupCompare(String text, int url, Context context, PopupCompareListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.text = text;
        this.url = url;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        binding = PopupCompareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập kích thước cho Dialog
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        // Bo tròn các góc
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Đặt màu nền trong suốt cho cửa sổ
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_corner_popup); // Sử dụng background đã tạo

        binding.descriptionThankYou.setText(text);
        binding.titleThankYou.setImageResource(url);
        binding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onCompareUpdated();
            }
        });
    }
}

