package com.example.scanimin.Qrcode;

import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanimin.R;
import com.imin.scan.CaptureActivity;
import com.imin.scan.DecodeConfig;
import com.imin.scan.DecodeFormatManager;
import com.imin.scan.Result;
import com.imin.scan.analyze.MultiFormatAnalyzer;
import com.imin.scan.config.ResolutionCameraConfig;

public class CustomCaptureActivity extends CaptureActivity {
    StringBuilder sb = new StringBuilder();
    private Toast toast;
    private TextView textresult;
    public int decode_count = 0;

    @Override
    public int getLayoutId() {
        return R.layout.activity_custom_capture;
    }

    @Override
    public void initCameraScan() {
        super.initCameraScan();
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        textresult = findViewById(R.id.textresult);
        //初始化解码配置
        DecodeConfig decodeConfig = new DecodeConfig();
        decodeConfig.setHints(DecodeFormatManager.ALL_HINTS)
                .setSupportVerticalCode(true)
                .setSupportLuminanceInvert(true)
                .setAreaRectRatio(0.8f)
                .setFullAreaScan(false);

        getCameraScan().setPlayBeep(true)
                .setPlayRaw(R.raw.beep)
                .setVibrate(true)
                .setCameraConfig(new ResolutionCameraConfig(CustomCaptureActivity.this))
                .setNeedAutoZoom(false)
                .setNeedTouchZoom(false)
                .setDarkLightLux(45f)
                .setBrightLightLux(100f)
                .bindFlashlightView(null)
                .setOnScanResultCallback(this)
                .setAnalyzer(new MultiFormatAnalyzer(decodeConfig))
                .setAnalyzeImage(true);

    }
    @Override
    public boolean onScanResultCallback(Result result) {
        Resources resources = getResources();
        sb.append(resources.getString(R.string.count)).append(decode_count++);
        sb.append("\n").append(resources.getString(R.string.time_consuming)).append(System.currentTimeMillis() - result.getTimestamp()).append(" ms\n");
        sb.append(resources.getString(R.string.symbology)).append(result.getBarcodeFormat().name()).append("\n");
        sb.append(resources.getString(R.string.capacity)).append(result.getNumBits()).append("\n");
        sb.append(resources.getString(R.string.content)).append(result.toString());
        textresult.setText(sb.toString());
        sb.delete(0, sb.length());
        return true;
    }


    @Override
    public void onScanResultFailure() {
        textresult.setText(sb.toString());
        sb.delete(0, sb.length());
    }

    private void showToast(String text){
        if(toast == null){
            toast = Toast.makeText(this,text,Toast.LENGTH_SHORT);
        }else{
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setText(text);
        }
        toast.show();
    }
}
