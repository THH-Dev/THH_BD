package com.example.scanimin.data.Object;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Data implements Serializable {

    @SerializedName("tableid")
    private int tableid;
    @SerializedName("company")
    private String company;
    @SerializedName("name")
    private String name;
    @SerializedName("position")
    private String position;
    @SerializedName("role")
    private String role;

    public Data() {
        this.name = "";
        this.company = "";
        this.position = null;
        this.tableid = 0;
        this.role = "";
    }

    public Data(String name,  String company, String position, int tablePosition, String role) {
        this.name = name;
        this.company = company;
        this.position = position;
        this.tableid = tablePosition;
        this.role = role;
    }
    public String getName() {
        return name;
    }
    public int getTable() {
        return tableid;
    }

    public String getCompany() {
        return company;
    }
    public String getPosition() {
        return position;
    }
    public String getRole() {
        return role;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setTable(int table) {
        this.tableid = table;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public void setPosition(String position) {
        this.position = position;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
