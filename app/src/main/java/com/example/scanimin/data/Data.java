package com.example.scanimin.data;

import java.io.Serializable;

public class Data implements Serializable {
    private int age;
    private String company;
    private String name;
    private String position;

    public Data() {
        this.name = "";
        this.age = 0;
        this.company = "";
        this.position = "";
    }

    public Data(String name, int age, String company, String position) {
        this.name = name;
        this.age = age;
        this.company = company;
        this.position = position;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }

    public String getCompany() {
        return company;
    }
    public String getPosition() {
        return position;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public void setPosition(String position) {
        this.position = position;
    }
}
