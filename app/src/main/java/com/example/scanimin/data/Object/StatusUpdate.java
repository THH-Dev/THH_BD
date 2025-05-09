package com.example.scanimin.data.Object;

import com.google.gson.annotations.SerializedName;

public class StatusUpdate {
    @SerializedName("message")
    private String message;
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
