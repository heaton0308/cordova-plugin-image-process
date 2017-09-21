package com.qxcloud.imageprocess.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.qxcloud.imageprocess.ImageProcess;
import com.qxcloud.imageprocess.ResourceUtils;
import com.qxcloud.imageprocess.operate.CameraView;
import com.qxcloud.imageprocess.utils.Logger;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


/**
 * @Class: TakePhoteActivity
 * @Description: 拍照界面
 */
public class TakePhotoActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2,
        CameraView.OnSavedListener{
    private String savedPath;
    CameraView mOpenCvCameraView;
    private CheckBox photograph;//闪关灯

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Logger.e("OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置横屏
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(ResourceUtils.getIdByName(this,ResourceUtils.TYPE_LAYOUT,"activity_take_photo"));

        savedPath = getIntent().getStringExtra(ImageProcess.EXTRA_DEFAULT_SAVE_PATH);

        mOpenCvCameraView = (CameraView) findViewById(ResourceUtils.getIdByName(this,ResourceUtils.TYPE_ID,"cameraPreview"));
//        FocusView focusView = (FocusView) findViewById(R.id.view_focus);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mOpenCvCameraView.setOnSavedListener(this);

        photograph = (CheckBox) findViewById(ResourceUtils.getIdByName(this,ResourceUtils.TYPE_ID,"photographs"));
        photograph.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Logger.e("启动闪光灯");
                mOpenCvCameraView.triggerFlash(isChecked);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Logger.e("Internal OpenCV library not found. Using OpenCV Manager for initialization");
        } else {
            Logger.e("OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    public void close(View view) {
        finish();
    }

    public void takePhoto(View view) {
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.takePicture();
        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    @Override
    public void onSaved(byte[] data) {
        Intent intent = new Intent(this, CropImgActivity.class);
        intent.putExtra(ImageProcess.EXTRA_DEFAULT_SAVE_PATH, savedPath);
        BitmapTransfer.transferBitmapData = data;
        startActivity(intent);
        finish();
    }
}
