package com.example.scanimin.data;

public class UpdateCustomer {
    private String qrcode;
    private String image;
    private Boolean status;

    public UpdateCustomer(){

    }

    public UpdateCustomer(String qrcode, String image, Boolean status) {
        this.qrcode = qrcode;
        this.image = image;
        this.status = status;
    }
    public String getQrcode() {
        return qrcode;
    }
    public String getImage() {
        return image;
    }
    public Boolean getStatus() {
        return status;
    }
    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setStatus(Boolean status) {
        this.status = status;
    }
}
