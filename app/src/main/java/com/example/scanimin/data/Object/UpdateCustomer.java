package com.example.scanimin.data.Object;

public class UpdateCustomer {
    private String qrcode;
    private String image;

    public UpdateCustomer(){

    }

    public UpdateCustomer(String qrcode, String image, Boolean status) {
        this.qrcode = qrcode;
        this.image = image;
    }
    public String getQrcode() {
        return qrcode;
    }
    public String getImage() {
        return image;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
    public void setImage(String image) {
        this.image = image;
    }
}
