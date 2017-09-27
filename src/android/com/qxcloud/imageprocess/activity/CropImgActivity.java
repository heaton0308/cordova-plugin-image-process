package com.qxcloud.imageprocess.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.qxcloud.imageprocess.ImageProcess;
import com.qxcloud.imageprocess.ResourceUtils;
import com.qxcloud.imageprocess.crop.CropImageType;
import com.qxcloud.imageprocess.crop.CropImageView;
import com.qxcloud.imageprocess.editAPI.EditImageAPI;
import com.qxcloud.imageprocess.editAPI.EditImageMessage;
import com.qxcloud.imageprocess.utils.Logger;
import com.qxcloud.imageprocess.utils.MyBitmapFactory;
import com.qxcloud.imageprocess.utils.OpenCVUtils;
import com.qxcloud.imageprocess.utils.PermissionUtils;

import java.io.File;

/**
 * Created by cfh on 2017-09-05.
 * 图片编辑 裁剪
 */

public class CropImgActivity extends FragmentActivity implements View.OnClickListener{

    private static final String[] NEED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private CropImageView cropmageView;//图片
    private RelativeLayout layout_return;//返回
    private RelativeLayout layout_preservation;
    private RelativeLayout layout_rotate;
    private String savedFilePath;
    private Activity activity;
    private ProgressDialog progressDialog;

    public void showProgressDialog(String text) {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
        }
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        progressDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        progressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        progressDialog.setMessage(text);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(ResourceUtils.getIdByName(this,ResourceUtils.TYPE_LAYOUT,"activity_editimg_view"));
        activity = this;
        checkPermission();
    }

    private void checkPermission(){
        if(Build.VERSION.SDK_INT >= 23
                && PermissionUtils.hasPermissions(this, NEED_PERMISSIONS)){
            PermissionUtils.requestPermissions(this,101,NEED_PERMISSIONS);
        }else{
            initView();
        }
    }


    /**
     * 初始化View
     */
    private void initView() {
        cropmageView = (CropImageView) findViewById(ResourceUtils.getIdByName(this,ResourceUtils.TYPE_ID,"cropmageView"));
        layout_return = (RelativeLayout) findViewById(ResourceUtils.getIdByName(this,ResourceUtils.TYPE_ID,"tv_return"));
        layout_preservation = (RelativeLayout) findViewById(ResourceUtils.getIdByName(this,ResourceUtils.TYPE_ID,"tv_preservation"));
        layout_rotate = (RelativeLayout) findViewById(ResourceUtils.getIdByName(this,ResourceUtils.TYPE_ID,"tv_rotate"));
        layout_return.setOnClickListener(this);
        layout_preservation.setOnClickListener(this);
        layout_rotate.setOnClickListener(this);
//        图片保存地址
        savedFilePath = getIntent().getStringExtra(ImageProcess.EXTRA_DEFAULT_SAVE_PATH);
        handler.sendEmptyMessage(4);
        handler.sendEmptyMessageDelayed(2, 100);
    }

    /**
     * 初始裁剪
     *
     * @param bitmap
     */
    private void cropImage(Bitmap bitmap) {
        Bitmap hh = BitmapFactory.decodeResource(this.getResources(),
                ResourceUtils.getIdByName(this,ResourceUtils.TYPE_DRAWABLE,"crop_button"));
        cropmageView.setCropOverlayCornerBitmap(hh);
        cropmageView.setImageBitmap(bitmap);
        cropmageView.setGuidelines(CropImageType.CROPIMAGE_GRID_ON_TOUCH);// 触摸时显示网格
        cropmageView.setFixedAspectRatio(false);// 自由剪切
        handler.sendEmptyMessage(1);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == ResourceUtils.getIdByName(this,ResourceUtils.TYPE_ID,"tv_return")) {
            //返回
            finish();
        } else if (i == ResourceUtils.getIdByName(this,ResourceUtils.TYPE_ID,"tv_preservation")) {
            //确定
            handler.sendEmptyMessage(0);
            try {
                Bitmap bitmap = cropmageView.getCroppedImage();
                if (bitmap != null && !bitmap.isRecycled()) {
                    Logger.e("saved bitmap size = "+bitmap.getByteCount()/8/1024+"KB");
                    boolean isSaved = MyBitmapFactory.saveBitmap(bitmap,savedFilePath);
                    if(isSaved){
                        File file = new File(savedFilePath);
                        Logger.e("saved file size = "+file.length()/1024+"KB");
                        handler.sendEmptyMessageDelayed(1,500);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                EditImageAPI.getInstance().post(0,new EditImageMessage(0));
                                finish();
                            }
                        },500);
                    }
                }
            } catch (Exception e) {
                dismissProgressDialog();
            }
        } else if (i == ResourceUtils.getIdByName(this,ResourceUtils.TYPE_ID,"tv_rotate")) {
            cropmageView.rotateImage(-90);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    showProgressDialog("图片裁剪中");
                    break;
                case 1:
                    dismissProgressDialog();
                    break;
                case 2:
                    initBitMap();
                    break;
                case 3:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    Logger.e("init crop image size = "+bitmap.getByteCount()/8/1024+"KB");
                    cropImage(bitmap);
                    break;
                case 4:
                    showProgressDialog("图片加载中");
                    break;
            }
        }
    };

    private void initBitMap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //压缩图片并加载
                String originalPath = getIntent().getStringExtra(ImageProcess.EXTRA_DEFAULT_SELECT_PATH);//原图路径
                if (null != originalPath && !originalPath.equals("")) {
                    try {
                        Bitmap bitmap = MyBitmapFactory.getBitmapByPath(originalPath);
                        Logger.e("init uri compress bitmap image size = "+bitmap.getByteCount()/8/1024+"KB");
                        Logger.e("init uri threshold bitmap image size = "+bitmap.getByteCount()/8/1024+"KB");
                        Message message = new Message();
                        message.what = 3;
                        message.obj = bitmap;
                        handler.sendMessageDelayed(message, 200);
                    } catch (Exception e) {
                        Logger.e("Exception = " + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (null != BitmapTransfer.transferBitmapData) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(BitmapTransfer.transferBitmapData, 0, BitmapTransfer.transferBitmapData.length);
                            Logger.e("init data compress bitmap image size = "+bitmap.getByteCount()/8/1024+"KB");
                            Logger.e("init data threshold bitmap image size = "+bitmap.getByteCount()/8/1024+"KB");
                            Message message = new Message();
                            message.what = 3;
                            message.obj = bitmap;
                            handler.sendMessageDelayed(message, 200);
                        }
                    } catch (Exception e) {
                        Logger.e("Exception = " + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapTransfer.transferBitmap = null;
        BitmapTransfer.transferBitmapData = null;
        cropmageView = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            boolean isGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }
            Logger.e("isGranted === " + isGranted + " --- " + grantResults.length);
            if (isGranted) {
                initView();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("此功能需要相机及存储权限，请前往设置打开")
                        .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        EditImageAPI.getInstance().post(1, new EditImageMessage(1));
                                        finish();
                                    }
                                }, 1000);
                            }
                        })
                        .show();
            }
        }
    }
}
