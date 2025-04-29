package com.example.scanimin.data;

import android.net.Uri;
import java.io.Serializable;
public class CustomerApi implements Serializable {
    private Data data;
    private String image;
    private String qrcode;
    private Boolean status;

    public CustomerApi() {
        this.data = new Data();
        this.image = null;
        this.qrcode = "";
        this.status = false;
    }

    public CustomerApi(Data data, String image, String qrcode, Boolean status) {
        this.data = data;
        this.image = image;
        this.qrcode = qrcode;
        this.status = status;

    }

    public Data getData() {
        return data;
    }

    public String getImage() {
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
    public void setImage(String image) {
        this.image = image;
    }
    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
    public void setStatus(Boolean status) {
        this.status = status;
    }
}
