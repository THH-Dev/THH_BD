package com.example.scanimin.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    public static File cropImageFileToSquare720(File inputFile, Context context) throws IOException {
        // 1. Decode ảnh từ file
        Bitmap original = BitmapFactory.decodeFile(inputFile.getAbsolutePath());

        // 2. Cắt ảnh thành 720x720 (center crop)
        int width = original.getWidth();
        int height = original.getHeight();
        int size = 720;
        int xOffset = (width - size) / 2;
        int yOffset = (height - size) / 2;

        Bitmap cropped = Bitmap.createBitmap(original, xOffset, yOffset, size, size);

        // 3. Ghi bitmap ra file mới
        File outputDir = context.getCacheDir(); // thư mục tạm
        File outputFile = new File(outputDir, inputFile.getName());

        FileOutputStream out = new FileOutputStream(outputFile);
        cropped.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();

        return outputFile;
    }

}