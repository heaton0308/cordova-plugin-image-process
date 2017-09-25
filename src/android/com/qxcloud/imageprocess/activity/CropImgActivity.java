package com.qxcloud.imageprocess.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.qxcloud.imageprocess.ImageProcess;
import com.qxcloud.imageprocess.ResourceUtils;
import com.qxcloud.imageprocess.ToastUtils;
import com.qxcloud.imageprocess.crop.CropImageType;
import com.qxcloud.imageprocess.crop.CropImageView;
import com.qxcloud.imageprocess.editAPI.EditImageAPI;
import com.qxcloud.imageprocess.editAPI.EditImageMessage;
import com.qxcloud.imageprocess.utils.FileUtils;
import com.qxcloud.imageprocess.utils.Logger;
import com.qxcloud.imageprocess.utils.MyBitmapFactory;
import com.qxcloud.imageprocess.utils.OpenCVUtils;

/**
 * Created by cfh on 2017-09-05.
 * 图片编辑 裁剪
 */

public class CropImgActivity extends Activity implements View.OnClickListener{
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
        initView();
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
            saveBitmapFile();
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
                    cropImage(bitmap);
                    break;
                case 4:
                    showProgressDialog("图片加载中");
                    break;
            }
        }
    };

    private void saveBitmapFile(){
        new Thread(){
            @Override
            public void run() {
                try {
                    Bitmap bitmap = cropmageView.getCroppedImage();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        boolean isSaved = MyBitmapFactory.saveBitmap(bitmap,savedFilePath);
                        if(isSaved){
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    handler.sendEmptyMessage(1);
                                    EditImageAPI.getInstance().post(0,new EditImageMessage(0));
                                    finish();
                                }
                            },500);
                        }else{
                            handler.sendEmptyMessageDelayed(1,200);
                            ToastUtils.toastMessage(CropImgActivity.this,"文件保存失败");
                        }
                    }else{
                        handler.sendEmptyMessageDelayed(1,200);
                        ToastUtils.toastMessage(CropImgActivity.this,"图片裁剪失败");
                    }
                } catch (Exception e) {
                    ToastUtils.toastMessage(CropImgActivity.this,"图片裁剪异常");
                    handler.sendEmptyMessageDelayed(1,200);
                }
            }
        }.start();
    }

    private void initBitMap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //压缩图片并加载
                String originalPath = getIntent().getStringExtra(ImageProcess.EXTRA_DEFAULT_SELECT_PATH);//原图路径
                if (null != originalPath && !originalPath.equals("")) {
                    try {
                        Bitmap bitmap = MyBitmapFactory.getBitmapByPath(originalPath);
                        // bitmap = OpenCVUtils.threshold(bitmap,17,7.5D);
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
                            Logger.e("initBitmap ----- " + bitmap.getByteCount() + " w = " + bitmap.getWidth() + " h = " + bitmap.getHeight());
                            // bitmap = OpenCVUtils.threshold(bitmap,17,7.5D);
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
}
