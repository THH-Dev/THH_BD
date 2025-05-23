package com.example.scanimin.function;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.example.scanimin.R;

import java.util.Locale;

public class LanguageManager {
    private Context context;


    public LanguageManager(Context context) {
        this.context = context;
    }
    public void changeLanguage(String languageCode) {
        setLocale(context, languageCode);
    }

    public void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        if(context instanceof Activity){
            ((Activity)context).recreate();
        }
    }
}
