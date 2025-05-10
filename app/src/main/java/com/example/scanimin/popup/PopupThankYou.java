package com.example.scanimin.popup;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.scanimin.R;
import com.example.scanimin.databinding.PopupCompareBinding;
import com.example.scanimin.databinding.PopupThankyouBinding;

public class PopupThankYou extends Dialog {
    private final Context context;
    private AlertDialog alertDialog;

    private PopupThankyouBinding binding;
    public PopupThankYou(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        binding = PopupThankyouBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập kích thước cho Dialog
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        // Bo tròn các góc
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_corner_popup);

        binding.descriptionThankYou.setText(R.string.thank_you_message);
//        Glide.with(context)
//                .asGif()
//                .load(R.drawable.icons_success_1)
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                .into(binding.iconImageGif);
    }
}
