package com.example.scanimin.File;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConverFile {

    private static final String TAG = "BitmapUtils";

    public static void saveBitmapToFile(Bitmap bitmap, File file) {
        if (bitmap == null || file == null) {
            Log.e(TAG, "saveBitmapToFile: Bitmap or File is null");
            return;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Compress the bitmap and write it to the file
            boolean isCompressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); // 100 is the quality (0-100)
            if (!isCompressed) {
                Log.e(TAG, "saveBitmapToFile: Failed to compress bitmap");
            }
        } catch (IOException e) {
            Log.e(TAG, "saveBitmapToFile: Error saving bitmap to file", e);
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "saveBitmapToFile: Error closing FileOutputStream", e);
                    e.printStackTrace();
                }
            }
            //recycle the bitmap
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
}