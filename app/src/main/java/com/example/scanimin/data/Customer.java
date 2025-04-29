package com.example.scanimin.data;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;

import javax.xml.transform.Source;

public class Customer implements Serializable {
    private Data data;
    private Uri image;
    private String qrcode;
    private Boolean status;

    public Customer() {
        this.data = new Data();
        this.image = null;
        this.qrcode = "";
        this.status = false;
    }

    public Customer(Data data, Uri image, String qrcode, Boolean status) {
        this.data = data;
        this.image = image;
        this.qrcode = qrcode;
        this.status = status;

    }

    public Data getData() {
        return data;
    }

    public Uri getImage() {
        return image;
    }
    public String getQrcode() {
        return qrcode;
    }
    public Boolean getStatus() {
        return status;
    }
    public void setData(Data data) {
        this.data = data;
    }
    public void setImage(Uri image) {
        this.image = image;
    }
    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
    public void setStatus(Boolean status) {
        this.status = status;
    }
}
