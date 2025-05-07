package com.example.scanimin.data.Local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Object.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SQLLite extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyDatabase.db";
    private static final int DATABASE_VERSION = 2;

    public SQLLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("SQLLite", "onCreate() called");
        db.execSQL("CREATE TABLE customersBD (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, tablePosition INTEGER, company TEXT, position TEXT, role TEXT, qrcode TEXT, image TEXT, status TEXT, timestamp TEXT, url TEXT)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("SQLLite", "onUpdate() called");
        db.execSQL("DROP TABLE IF EXISTS customersBD");
        onCreate(db);
    }

    public void insertUser(String name, int tablePosition, String company, String position, String role, String qrcode, String image, Boolean status, String timestamp, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("company", company);
        values.put("position", position);
        values.put("role", role);
        values.put("table", tablePosition);
        values.put("qrcode", qrcode);
        if (status){
            values.put("status", "true");
        }else {
            values.put("status", "false");
        }
        values.put("image", image);
        values.put("timestamp", timestamp);
        values.put("url", url);
        db.insert("customersBD", null, values);
        db.close();
    }

    public void insertUser(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", customer.getData().getName());
        values.put("company", customer.getData().getCompany());
        values.put("position", customer.getData().getPosition());
        values.put("qrcode", customer.getQrcode());
        values.put("table", customer.getData().getTable());
        values.put("role", customer.getData().getRole());
        if (customer.getStatus()){
            values.put("status", "true");
        }else {
            values.put("status", "false");
        }
        if (customer.getImage() != null) {
            values.put("image", customer.getImage().toString());
        } else {
            values.put("image", (String) null);
        }
        values.put("timestamp", customer.getTimestamp());
        values.put("url", customer.getUrl());
        db.insert("customersBD", null, values);
        db.close();
    }

    public int updateUser(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", customer.getData().getName());
        values.put("tablePosition", customer.getData().getTable());
        values.put("company", customer.getData().getCompany());
        values.put("position", customer.getData().getPosition());
        values.put("qrcode", customer.getQrcode());
        values.put("role", customer.getData().getRole());
        if (customer.getImage() != null) {
            values.put("image", customer.getImage().toString());
        } else {
            values.put("image", (String) null);
        }
        if (customer.getStatus()){
            values.put("status", "true");
        }else {
            values.put("status", "false");
        }
        values.put("timestamp", customer.getTimestamp());
        values.put("url", customer.getUrl());
        // Cập nhật dựa trên qrcode của khách hàng
        int rowsAffected = db.update("customersBD", values, "qrcode" + " = ?", new String[]{customer.getQrcode()});
        db.close();
        return rowsAffected;
    }

    public List<Customer> getAllPersons() {
        List<Customer> personList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM customersBD", null);
            if (cursor.moveToFirst()) {
                do {
                    Customer person = new Customer();
                    person.setData(new Data(
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("company")),
                            cursor.getString(cursor.getColumnIndexOrThrow("position")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("tablePosition")),
                            cursor.getString(cursor.getColumnIndexOrThrow("role"))));
                    person.setQrcode(cursor.getString(cursor.getColumnIndexOrThrow("qrcode")));
                    if (Objects.equals(cursor.getString(cursor.getColumnIndexOrThrow("status")), "true")){
                        person.setStatus(true);
                    }else {
                        person.setStatus(false);
                    }
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image"));
                    if (imagePath != null){
                        person.setImage(Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow("image"))));
                    }else {
                        person.setImage(null);
                    }
                    person.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow("timestamp")));
                    person.setUrl(cursor.getString(cursor.getColumnIndexOrThrow("url")));
                    personList.add(person);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return personList;
    }
}
