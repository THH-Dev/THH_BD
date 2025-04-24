package com.example.scanimin.popup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.scanimin.R;

public class PopupThankYou {
    private final Context context;;
    private AlertDialog alertDialog;
    public PopupThankYou(Context context) {
        this.context = context;
        createPopup();
    }

    private void createPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_thankyou, null);
        ImageView gifImageView = dialogView.findViewById(R.id.icon_imageGif);

        Glide.with(context)
                .asGif()
                .load(R.drawable.icons_success_1)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(gifImageView);
        builder.setView(dialogView);
        alertDialog = builder.create();
    }

    public void show() {
        alertDialog.show();
    }

    public boolean isShowing() {
        return alertDialog.isShowing();
    }

    public void dismiss() {
        alertDialog.dismiss();
    }

}
