package com.example.scanimin.data.Object;

import android.net.Uri;

import java.io.Serializable;

public class Customer implements Serializable {
    private Data data;
    private Uri image;
    private String code;
    private Boolean status;
    private String timestamp;
    private String url;

    public Customer() {
        this.data = new Data();
        this.image = null;
        this.code = "";
        this.status = false;
        this.timestamp = "";
        this.url = "";
    }

    public Customer(Data data, Uri image, String qrcode, Boolean status, String timestamp, String url) {
        this.data = data;
        this.image = image;
        this.code = qrcode;
        this.status = status;
        this.timestamp = timestamp;
        this.url = url;
    }

    public Data getData() {
        return data;
    }

    public Uri getImage() {
        return image;
    }
    public String getQrcode() {
        return code;
    }
    public Boolean getStatus() {
        return status;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getUrl() {
        return url;
    }
    public void setData(Data data) {
        this.data = data;
    }
    public void setImage(Uri image) {
        this.image = image;
    }
    public void setQrcode(String qrcode) {
        this.code = qrcode;
    }
    public void setStatus(Boolean status) {
        this.status = status;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}
