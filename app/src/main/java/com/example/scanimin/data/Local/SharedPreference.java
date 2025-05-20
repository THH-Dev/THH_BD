package com.example.scanimin.data.Local;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    private SharedPreferences sharedPreferences;
    private Context context;

    public SharedPreference(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PrefKeys.MY_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveDirFile(String dirFile) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefKeys.KEY_EXPORT_DIR_FILE, dirFile);
        editor.apply();
    }

    public String getDirFile() {
        return sharedPreferences.getString(PrefKeys.KEY_EXPORT_DIR_FILE, "");
    }
    public void saveIpServer(String ipServer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefKeys.KEY_IP_SERVER, ipServer);
        editor.apply();
    }
    public String getIpServer() {
        return sharedPreferences.getString(PrefKeys.KEY_IP_SERVER, "");
    }
    public void savePortServer(String portServer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefKeys.KEY_PORT_SERVER, portServer);
        editor.apply();
    }
    public String getPortServer() {
        return sharedPreferences.getString(PrefKeys.KEY_PORT_SERVER, "");
    }

}
