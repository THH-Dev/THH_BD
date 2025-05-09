package com.imin.scandemo;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.imin.scan.Result;
import com.imin.scan.ScanUtils;
import com.imin.scan.Symbol;


public class MainActivity2 extends Activity implements SurfaceHolder.Callback {
	private static final String TAG = "MainActivity";
	private Camera mCamera;
	private SurfaceHolder mHolder;
	private SurfaceView surface_view;
	private Handler autoFocusHandler;
	public boolean use_auto_focus=true;//T1/T2 mini定焦摄像头没有对焦功能,应该改为false
    public int decode_count = 0;
    private TextView textview;
    //预览分辨率设置，T1/T2 mini设置640x480，其他手持机可选取640x480,800x480,1280x720
    public static int previewSize_width=640;
    public static int previewSize_height=480;
    StringBuilder sb = new StringBuilder();
    String content="";
	private ScanUtils scanUtils;
	private TextView scan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		scan = findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scan.setVisibility(View.GONE);
			}
		});
		init();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( scanUtils!= null) {
			scanUtils.destroy();
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
	private void init() {
		surface_view = (SurfaceView) findViewById(R.id.surface_view);
		textview = (TextView) findViewById(R.id.textview);
		mHolder = surface_view.getHolder();
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder.addCallback(this);

		//初始化扫码工具
		//获取解码工具类
		scanUtils = ScanUtils.getInstance(this);
		scanUtils.initScan();//初始化
		scanUtils.initBeepSound(true,R.raw.beep);//初始化音频文件
		scanUtils.setConfig(Symbol.ALL_FORMATS);

		if(use_auto_focus)
			autoFocusHandler = new Handler();
		decode_count=0;
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (mHolder.getSurface() == null) {
			return;
		}
		try{
			mCamera.stopPreview();
		} catch (Exception e) {
		}
		try {		
			Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(previewSize_width, previewSize_height);  //设置预览分辨率
			if(use_auto_focus)
				parameters.setFocusMode(parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			mCamera.setParameters(parameters);
			mCamera.setDisplayOrientation(90);//手持机使用，竖屏显示,T1/T2 mini需要屏蔽掉
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(previewCallback);
			mCamera.startPreview();
        } catch (Exception e) {
            Log.d("DBG", "Error starting camera preview: " + e.getMessage());
        }
    }

	/**
	 * 预览数据
	 */
	PreviewCallback previewCallback = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Result rawResult = scanUtils.getScanResult(data,previewSize_width,previewSize_height);
			if (scanUtils.getNsyms() != 0 && rawResult != null) {
				Resources resources = getResources();
				sb.append(resources.getString(R.string.count) + decode_count++ );
				sb.append("\n"+resources.getString(R.string.time_consuming) + scanUtils.getCost_time() + " ms\n" );
//				//如果允许识读多个条码，则解码结果可能不止一个
				sb.append(resources.getString(R.string.symbology) + rawResult.getNumBits()+ "\n");
				sb.append(resources.getString(R.string.capacity) + rawResult.getBarcodeFormat().name()+ "\n");
				sb.append(resources.getString(R.string.content) + rawResult.toString());
			}
			Log.i("XGH",":"+sb.toString());
			textview.setText(sb.toString());
			sb.delete(0,sb.length());
		}
	};

	/**
	 * 自动对焦回调
	 */
	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (null == mCamera || null == autoFocusCallback) {
                return;
            }
            mCamera.autoFocus(autoFocusCallback);
        }
    };

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try	{
			int df = Camera.getNumberOfCameras();
			mCamera = Camera.open(/*Camera.CameraInfo.CAMERA_FACING_BACK*/);
		} catch (Exception e){
			Log.d(TAG,"Exception=="+e.getMessage());
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null){
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

 	@Override
 	protected void onResume() {
 		super.onResume();
 	}

}
