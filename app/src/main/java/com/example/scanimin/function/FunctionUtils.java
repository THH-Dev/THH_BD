package com.example.scanimin.function;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.VideoView;

import com.example.scanimin.R;
import com.jiangdg.usbcamera.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FunctionUtils {
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
    public static List<File> getImagesFromDirectory() {
        String picPath = FileUtils.ROOT_PATH + MyApplication.DIRECTORY_NAME + "/images/";
        File imageDir = new File(picPath);
        List<File> imageFiles = new ArrayList<>();

        if (imageDir.exists() && imageDir.isDirectory()) {
            File[] files = imageDir.listFiles((dir, name) -> {
                String nameLower = name.toLowerCase();
                return nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg") || nameLower.endsWith(".png");
            });

            if (files != null) {
                imageFiles.addAll(Arrays.asList(files));
            }
        }

        return imageFiles;
    }

    public static File createTempFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getCacheDir();
        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    public static void startCountdownVideo(Context context, VideoView videoView) {

        // Prepare the video URI
        Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.countdown); // Replace with your video file name

        // Set up the VideoView
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(false); // If you want the video to loop
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                //start the video
                videoView.start();
            }
        });
    }
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

}
