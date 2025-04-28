package com.example.scanimin.popup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.scanimin.R;

public class PopupCompare extends DialogFragment {
    private PopupCompareListener listener;
    private final Context context;
    private AlertDialog alertDialog;
    private TextView btnConfirm;

    private String text;
    public interface PopupCompareListener {
        void onCompareUpdated();
    }
    public PopupCompare(String text, Context context, PopupCompareListener listener) {
        this.context = context;
        this.listener = listener;
        this.text = text;
        createPopup();
    }

    private void createPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_compare, null);
        TextView title = dialogView.findViewById(R.id.title_thank_you);
        title.setText(text);
        btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        builder.setView(dialogView);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                listener.onCompareUpdated();
            }
        });
        alertDialog = builder.create();
    }

    public void show() {
        alertDialog.show();
    }

    public void dismiss1() {
        alertDialog.dismiss();
    }
}

