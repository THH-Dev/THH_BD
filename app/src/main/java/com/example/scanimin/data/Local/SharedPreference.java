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
    public void saveMinioIp(String minioIp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefKeys.KEY_IP_MINIO, minioIp);
        editor.apply();
    }
    public String getMinioIp() {
        return sharedPreferences.getString(PrefKeys.KEY_IP_MINIO, "");
    }

    public void saveMinioPort(String minioPort) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefKeys.KEY_PORT_MINIO, minioPort);
        editor.apply();
    }
    public String getMinioPort() {
        return sharedPreferences.getString(PrefKeys.KEY_PORT_MINIO, "");
    }

    public void saveAccessKey(String accessKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefKeys.KEY_ACCESS_KEY_MINIO, accessKey);
        editor.apply();
    }
    public String getAccessKey() {
        return sharedPreferences.getString(PrefKeys.KEY_ACCESS_KEY_MINIO, "");
    }
    public void saveSecretKey(String secretKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefKeys.KEY_SECRET_KEY_MINIO, secretKey);
        editor.apply();
    }
    public String getSecretKey() {
        return sharedPreferences.getString(PrefKeys.KEY_SECRET_KEY_MINIO, "");
    }
    public void saveBucketName(String bucketName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefKeys.KEY_BUCKET_NAME, bucketName);
        editor.apply();
    }
    public String getBucketName() {
        return sharedPreferences.getString(PrefKeys.KEY_BUCKET_NAME, "");
    }

}
