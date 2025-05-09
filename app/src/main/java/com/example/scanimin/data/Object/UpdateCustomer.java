package com.example.scanimin.data.Object;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UpdateCustomer implements Serializable {
    @SerializedName("image")
    private String image;
    @SerializedName("code")
    private String code;

    public UpdateCustomer(){
        this.code = "";
        this.image = "";
    }

    public UpdateCustomer(String image, String code) {
        this.image = image;
        this.code = code;
    }
    public String getCode() {
        return code;
    }
    public String getImage() {
        return image;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public void setImage(String image) {
        this.image = image;
    }
}
