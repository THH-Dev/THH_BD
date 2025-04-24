package com.example.scanimin.function;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.example.scanimin.data.Customer;
import com.example.scanimin.data.Data;

import org.json.JSONObject;

public class JsonUtils {
    public static Customer parseCustomerJson(String inputString) {
        try {
            int startIndex = inputString.indexOf("{");
            if (startIndex == -1) {
                return null;
            }
            String jsonString = inputString.substring(startIndex);
            JSONObject jsonObject = new JSONObject(jsonString);

            String id = jsonObject.getString("id");
            JSONObject dataObject = jsonObject.getJSONObject("data");
            String name = dataObject.getString("name");
            int age = dataObject.getInt("age");
            String company = dataObject.getString("company");
            String position = dataObject.getString("position");

            Data data = new Data(name,age,company,position);
            String qrcode = jsonObject.getString("qrcode");
            String image = jsonObject.getString("image");
            Boolean status = jsonObject.getBoolean("status");

            return new Customer(data, qrcode, image, status);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Customer parseJson(String string) {
        try {
            int startIndex = string.indexOf("{");
            if (startIndex == -1) {
                return null;
            }
            // Trích xuất chuỗi JSON
            String jsonString = string.substring(startIndex);
            JSONObject jsonObject = new JSONObject(jsonString);
            // Lấy dữ liệu từ "data" object
            JSONObject dataObject = jsonObject.getJSONObject("data");
            String name = dataObject.getString("name");
            int age = dataObject.getInt("age");
            String company = dataObject.getString("company");
            String position = dataObject.getString("position");
            Data data = new Data(name, age, company, position);
            // Lấy các trường còn lại
            String qrcode = jsonObject.getString("qrcode");
            String image = jsonObject.getString("image");
            Boolean status = jsonObject.getBoolean("status");

            return new Customer(data, image, qrcode, status);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFileExtension(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String fileExtension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        if (fileExtension == null) {
            // Fallback: Try to extract the extension from the path
            String path = uri.getPath();
            if (path != null) {
                int lastDotIndex = path.lastIndexOf('.');
                if (lastDotIndex != -1 && lastDotIndex < path.length() - 1) {
                    fileExtension = path.substring(lastDotIndex + 1).toLowerCase();
                }
            }
        }
        return fileExtension;
    }
}
