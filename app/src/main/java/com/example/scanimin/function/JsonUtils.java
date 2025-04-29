package com.example.scanimin.function;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.example.scanimin.data.Customer;
import com.example.scanimin.data.Data;

import org.json.JSONObject;

public class JsonUtils {
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
