package com.example.scanimin.data.Object;

import com.google.gson.annotations.SerializedName;

public class PostCustomer {
    @SerializedName("data")
    private Data data;
    @SerializedName("code")
    private String qrcode;

    public PostCustomer() {
        this.data = new Data();
        this.qrcode = "";
    }

    public PostCustomer(Data data, String image, String qrcode, Boolean status, String timestamp, String url) {
        this.data = data;
        this.qrcode = qrcode;
    }

    public Data getData() {
        return data;
    }

    public String getQrcode() {
        return qrcode;
    }
    public void setData(Data data) {
        this.data = data;
    }
    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
}
