package com.qxcloud.imageprocess.activity.newCamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qxcloud.imageprocess.ImageProcess;
import com.qxcloud.imageprocess.ResourceUtils;
import com.qxcloud.imageprocess.ToastUtils;
import com.qxcloud.imageprocess.activity.BitmapTransfer;
import com.qxcloud.imageprocess.activity.CropImgActivity;
import com.qxcloud.imageprocess.operate.FocusView;
import com.qxcloud.imageprocess.utils.Logger;

/**
 * Created by cfh on 2017-11-03.
 * 基于SurfaceView 进行自定义相机处理
 */

public class NewTackPhotoActivity extends Activity implements OnCaptureCallback, View.OnClickListener {
    private CheckBox m_photographs;//闪光灯
    private MaskSurfaceView m_surfaceView;//相机
    private FocusView m_viewFocus;//聚焦
    private TextView m_hint;//默认文字
    private ImageView m_btnRecapture;//关闭按钮
    private TextView m_tackphotoBtn;//拍照

    private Activity activity;
    private String savedPath;
    private static final String EXTRA_DEFAULT_SAVE_DIR = "default_save_dir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity = this;
        setContentView(ResourceUtils.getIdByName(this, ResourceUtils.TYPE_LAYOUT,"new_tackphoto_activity_view"));
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessageDelayed(6, 300);
    }

    /**
     * 初始化页面事件及其操作
     */
    private void initView() {
        savedPath = getIntent().getStringExtra(ImageProcess.EXTRA_DEFAULT_SAVE_PATH);
        Logger.e("+++++++++++mSavedDir+++++++++++" + savedPath);
        m_photographs = (CheckBox) findViewById(ResourceUtils.getIdByName(this, ResourceUtils.TYPE_ID,"photographs"));
        m_surfaceView = (MaskSurfaceView) findViewById(ResourceUtils.getIdByName(this, ResourceUtils.TYPE_ID,"surface_view"));
        m_tackphotoBtn = (TextView) findViewById(ResourceUtils.getIdByName(this, ResourceUtils.TYPE_ID,"tackphoto_btn"));
        m_viewFocus = (FocusView) findViewById(ResourceUtils.getIdByName(this, ResourceUtils.TYPE_ID,"view_focus"));
        m_btnRecapture = (ImageView) findViewById(ResourceUtils.getIdByName(this, ResourceUtils.TYPE_ID,"btn_recapture"));
        m_tackphotoBtn.setOnClickListener(this);
        m_viewFocus.setOnClickListener(this);
        m_btnRecapture.setOnClickListener(this);

        m_photographs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Logger.e("启动闪光灯");
                Message message = new Message();
                message.what = 4;
                message.obj = isChecked;
                handler.sendMessage(message);
            }
        });
    }


    @Override
    public void onCapture(byte[] data) {
        String message = "拍照成功";
        if (data == null) {
            message = "拍照失败";
            CameraHelper.getInstance().startPreview();
            this.m_surfaceView.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent(this, CropImgActivity.class);
            intent.putExtra(ImageProcess.EXTRA_DEFAULT_SAVE_PATH, savedPath);
            BitmapTransfer.transferBitmapData = data;
            startActivity(intent);
            finish();
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 相机开启监听
     */
    CameraOpenCallBack cameraOpenCallBack = new CameraOpenCallBack() {
        @Override
        public void onCameraOpen(boolean success, Object object) {
            if (!success) {
                //相机打开失败
                handler.sendEmptyMessage(5);
            }
        }
    };
    /**
     * 消息机制
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //拍照
                    CameraHelper.getInstance().tackPicture(activity, NewTackPhotoActivity.this, cameraOpenCallBack);
                    break;
                case 2:
//                    //重拍
//                    deleteFile();
//                    CameraHelper.getInstance().startPreview();
                    //关闭
                    finish();
                    break;
                case 3:
                    //聚焦
                    CameraHelper.getInstance().autoFocus();
                    Logger.e("+++++++聚焦+++");
                    break;
                case 4:
                    //闪光灯
                    boolean isChecked = (boolean) msg.obj;
                    CameraHelper.getInstance().setIsOpenFlashMode(isChecked);
                    break;
                case 5:
                    //相机打开失败
                    ToastUtils.toastMessage(activity,"相机打开失败");
                    break;
                case 6:
                    if (m_surfaceView != null) {
                        m_surfaceView.openCamera(cameraOpenCallBack);
                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == ResourceUtils.getIdByName(this, ResourceUtils.TYPE_ID,"view_focus")) {//聚焦
            handler.sendEmptyMessage(3);
        } else if (i == ResourceUtils.getIdByName(this, ResourceUtils.TYPE_ID,"tackphoto_btn")) {//拍照
            handler.sendEmptyMessage(1);

        } else if (i == ResourceUtils.getIdByName(this, ResourceUtils.TYPE_ID,"btn_recapture")) {//关闭
            handler.sendEmptyMessage(2);
        }
    }
}

