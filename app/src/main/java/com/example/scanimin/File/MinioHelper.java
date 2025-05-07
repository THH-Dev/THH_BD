package com.example.scanimin.File;

import android.util.Log;
import java.io.File;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import okhttp3.OkHttpClient;

public class MinioHelper {
    private static final String ENDPOINT = "http://192.168.3.69:9000";
    private static final String ACCESS_KEY = "CLR1bkuA5m9VT5DQtBms";
    private static final String SECRET_KEY = "yM1yfUIbTxeFcdXzx8gCf6xFHNhWbWpJ1lPVax8w";
    private static final String BUCKET_NAME = "image";
    private static MinioClient minioClient;

    public void uploadImageToMinIO(File fileImage) {
        try {
            // 1. Khởi tạo MinIO client
            minioClient = MinioClient.builder()
                    .endpoint(ENDPOINT)
                    .credentials(ACCESS_KEY, SECRET_KEY)
                    .httpClient(new OkHttpClient())
                    .build();


            // 2. Tên bucket & tên object
            String bucketName = BUCKET_NAME;
            String objectName = fileImage.getName();

            // 3. Upload file
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(fileImage.getAbsolutePath())
                            .build()
            );
            Log.d("MinIO", "✅ Upload thành công: " + objectName);
        } catch (Exception e) {
            Log.e("MinIO", "❌ Lỗi khi upload: " + e.getMessage(), e);
        }
    }

}
