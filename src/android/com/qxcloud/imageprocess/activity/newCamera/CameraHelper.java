package com.qxcloud.imageprocess.activity.newCamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

import com.qxcloud.imageprocess.utils.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by cfh on 2017-05-10.
 * 拍照工具类
 */

public class CameraHelper {
    private final String TAG = "CameraHelper";
    private ToneGenerator tone;
    private String filePath;// = "/carchecker/photo";
    private boolean isPreviewing;

    private static CameraHelper helper;
    private Camera camera;
    private MaskSurfaceView surfaceView;

    //	分辨率
    private Size resolution;

    //	照片质量
    private int picQuality = 100;

    //	照片尺寸
    private Size pictureSize;

    //	闪光灯模式(default：自动)
    private String flashlightStatus = Parameters.FLASH_MODE_OFF;
    private Activity activity;

    public enum Flashlight {
        AUTO, ON, OFF
    }

    private CameraHelper() {
    }

    public static synchronized CameraHelper getInstance() {
        if (helper == null) {
            helper = new CameraHelper();
        }
        return helper;
    }

    /**
     * 设置照片质量
     *
     * @param picQuality
     * @return
     */
    public CameraHelper setPicQuality(int picQuality) {
        this.picQuality = picQuality;
        return helper;
    }

    /**
     * 设置闪光灯模式
     *
     * @param status
     * @return
     */
    public CameraHelper setFlashlight(Flashlight status) {
        switch (status) {
            case AUTO:
                this.flashlightStatus = Parameters.FLASH_MODE_AUTO;
                break;
            case ON:
                this.flashlightStatus = Parameters.FLASH_MODE_ON;
                break;
            case OFF:
                this.flashlightStatus = Parameters.FLASH_MODE_OFF;
                break;
            default:
                this.flashlightStatus = Parameters.FLASH_MODE_AUTO;
        }
        return helper;
    }

    /**
     * @param mIsOpenFlashMode
     * @Description: 设置开启闪光灯(重新预览)
     * @Since:2015-8-12
     * @Version:1.1.0
     */
    public void setIsOpenFlashMode(boolean mIsOpenFlashMode) {
        try {
            if (null == this.camera) {
                return;
            }
            if (!mIsOpenFlashMode) {
                //要求关闭
                if (null == this.camera.getParameters().getFlashMode() || this.camera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_OFF)) {
                    //不做处理
                    return;
                } else {
                    this.flashlightStatus = Parameters.FLASH_MODE_OFF;
                }
            } else {
                //要求打开
                this.flashlightStatus = Parameters.FLASH_MODE_TORCH;
            }
            Parameters p = this.camera.getParameters();
            p.setFlashMode(this.flashlightStatus);
            this.camera.setParameters(p);
            this.startPreview();
        } catch (Exception e) {

        }
    }

    /**
     * 设置文件保存路径(default: /mnt/sdcard/DICM)
     *
     * @param path
     * @return
     */
    public CameraHelper setPictureSaveDictionaryPath(String path) {
        this.filePath = path;
        return helper;
    }

    public CameraHelper setMaskSurfaceView(MaskSurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        return helper;
    }

    /**
     * 打开相机并开启预览
     *
     * @param holder       SurfaceHolder
     * @param format       图片格式
     * @param width        SurfaceView宽度
     * @param height       SurfaceView高度
     * @param screenWidth  屏幕宽度
     * @param screenHeight 屏幕高度
     */
    public void openCamera(SurfaceHolder holder, int format, int width, int height, int screenWidth, int screenHeight, Context context, CameraOpenCallBack cameraOpenCallBack) {
        try {
            if (this.camera != null) {
                this.camera.setPreviewCallback(null);
                this.camera.release();
            }
            this.camera = Camera.open();
            this.initParameters(holder, format, width, height, screenWidth, screenHeight);
            this.startPreview();
        } catch (Exception e) {
            Logger.e("++++++相机打开失败++++++++");
//            ModuleInterface.getInstance().showToast(context,"相机授权失败，请授权");
            cameraOpenCallBack.onCameraOpen(false, null);
        }
    }

    /**
     * 照相
     */
    public void tackPicture(Activity activity, final OnCaptureCallback callback, CameraOpenCallBack cameraOpenCallBack) {
        try {
            this.activity = activity;
//        this.camera.takePicture(new ShutterCallback() {
//                    @Override
//                    public void onShutter() {
//                        if (tone == null) {
////						 发出提示用户的声音
//                            tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
//                        }
//                        tone.startTone(ToneGenerator.TONE_PROP_BEEP);
//                    }
//                }, null, new PictureCallback() {
//                    @Override
//                    public void onPictureTaken(byte[] data, Camera camera) {
//                        String filepath = savePicture(data);
//                        boolean success = false;
//                        if(filepath != null){
//                            success = true;
//                        }
//                        stopPreview();
//                        callback.onCapture(success, filepath);
//                    }
//                });
            if (null != this.camera) {
                this.camera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean flag, Camera camera) {
                        camera.takePicture(new ShutterCallback() {
                            @Override
                            public void onShutter() {
                                if (tone == null) {
//						 发出提示用户的声音
                                    tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                                }
                                tone.startTone(ToneGenerator.TONE_PROP_BEEP);
                            }
                        }, null, new PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                stopPreview();
                                callback.onCapture(data);
                            }
                        });
                    }
                });
            } else {
                Logger.e("this.camera   is   null++++++++++++++++++++++++++++++++++");
                cameraOpenCallBack.onCameraOpen(false, null);
            }
        } catch (Exception e) {
            Logger.e("拍照失败");
            cameraOpenCallBack.onCameraOpen(false, null);
        }
    }

    /**
     * 相机聚焦
     */
    public void autoFocus() {
        try {
            if (null != this.camera) {
                this.camera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            //聚焦成功
                            Logger.e("聚焦成功");
                        } else {
                            //聚焦失败
                            Logger.e("聚焦失败");
                        }
                    }
                });
            }
        } catch (Exception e) {

        }
    }


    /**
     * 生成图片名称
     *
     * @return
     */
    private String generateFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault());
        String strDate = dateFormat.format(new Date());
        return "img_" + strDate + ".jpg";
    }

    /**
     * @return
     */
    private File getImageDir() {
        String path = null;
        if (this.filePath == null || this.filePath.equals("")) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
        } else {
            path = Environment.getExternalStorageDirectory().getPath() + filePath;
        }
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    /**
     * 初始化相机参数
     *
     * @param holder       SurfaceHolder
     * @param format       图片格式
     * @param width        SurfaceView宽度
     * @param height       SurfaceView高度
     * @param screenWidth  屏幕宽度
     * @param screenHeight 屏幕高度
     */
    private void initParameters(SurfaceHolder holder, int format, int width, int height, int screenWidth, int screenHeight) {
        try {
            Parameters p = this.camera.getParameters();

            this.camera.setPreviewDisplay(holder);

            if (width > height) {
//				横屏
                this.camera.setDisplayOrientation(0);
            } else {
//				竖屏
                this.camera.setDisplayOrientation(90);
            }

//			照片质量
            p.set("jpeg-quality", picQuality);

//			设置照片格式
            p.setPictureFormat(PixelFormat.JPEG);

//			设置闪光灯
            p.setFlashMode(this.flashlightStatus);

//			设置最佳预览尺寸
            List<Size> previewSizes = p.getSupportedPreviewSizes();



//			设置预览分辨率
            if (this.resolution == null) {
                this.resolution = this.getOptimalPreviewSize(previewSizes, width, height);
            }
            try {
                p.setPreviewSize(this.resolution.width, this.resolution.height);
            } catch (Exception e) {
                Log.e(TAG, "不支持的相机预览分辨率: " + this.resolution.width + " × " + this.resolution.height);
            }

//            p.setPreviewSize(1920, 1080);

//			设置照片尺寸
            List<Size> pictureSizes = p.getSupportedPictureSizes();
            if (this.pictureSize == null) {
                this.setPicutreSize(pictureSizes, width, height);
            }
//            p.setPictureSize(1920, 1080);
            if (pictureSizes.contains(resolution)) {
                //包含尺寸
                Log.e("cfh", "包含尺寸" + this.resolution.width + " X " + this.resolution.height);
                try {
                    p.setPictureSize(this.resolution.width, this.resolution.height);
                } catch (Exception e) {
                    Log.e(TAG, "不支持的照片尺寸: " + this.pictureSize.width + " × " + this.pictureSize.height);
                }
            } else {
                //不包含尺寸
                Log.e("cfh", "不包含尺寸" + this.pictureSize.width + " X " + this.pictureSize.height);
                try {
                    p.setPictureSize(this.pictureSize.width, this.pictureSize.height);
                } catch (Exception e) {
                    Log.e(TAG, "不支持的照片尺寸: " + this.pictureSize.width + " × " + this.pictureSize.height);
                }
            }
            this.camera.setParameters(p);
        } catch (Exception e) {
            Log.e(TAG, "相机参数设置错误");
        }
    }

    /**
     * 释放Camera
     */
    public void releaseCamera() {
        try {
            if (this.camera != null) {
                if (this.isPreviewing) {
                    this.camera.setPreviewCallback(null);
                    this.stopPreview();
                }
                isPreviewing = false;
                this.camera.release();
                this.camera = null;
            }
        } catch (Exception e) {
            Logger.e("释放Camera异常");
        }
    }

    /**
     * 停止预览
     */
    private void stopPreview() {
        try {
            if (this.camera != null && this.isPreviewing) {
                this.camera.setPreviewCallback(null);
                this.camera.stopPreview();
                this.isPreviewing = false;
            }
        } catch (Exception e) {
            Logger.e("停止预览");
        }
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        try {
            if (this.camera != null) {
                this.camera.startPreview();
                autoFocus();
                this.isPreviewing = true;
            }
        } catch (Exception e) {

        }
    }



    /**
     * 获取最佳预览尺寸
     * @param sizes  预览列表
     * @param width  SurfaceView宽度
     * @param height SurfaceView高度
     * @return
     */
    private Size getOptimalPreviewSize(List<Size> sizes, int width, int height) {
        Size optimalSize = null;
        try {
            Log.e("cfh", "+++++++++++++++++++++++" + width + " X " + height);
            final double ASPECT_TOLERANCE = 0.05;
            double targetRatio = (double) width / height;
            if (sizes == null)
                return null;
            double minDiff = Double.MAX_VALUE;
            int targetHeight = height;
            for (Size size : sizes) {
                double r = size.width * 1.0 / size.height * 1.0;
                if (r != 4 / 3 || r != 3 / 4 || r != 16 / 9 || r != 9 / 16) {
                    continue;
                }

                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
            // Cannot find the one match the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
            Log.e("cfh", "**********optimalSize*************:" + optimalSize.height + "X" + optimalSize.width);
            return optimalSize;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("cfh",e.toString());
        }
        return optimalSize;
    }

    /**
     * 设置照片尺寸为最接近屏幕尺寸
     * @param list           拍照尺寸列表
     * @param width_
     * @param height_
     */
    private void setPicutreSize(List<Size> list, int width_, int height_) {
        try {
            int calcWidth = 0;
            int calcHeight = 0;

            int maxAllowedWidth = this.resolution.width;//获取预览宽
            int maxAllowedHeight = this.resolution.height;//获取预览高
            Log.e("cfh", "**********预览图************:" + resolution.height + "X" + resolution.width);
            float aspectRatio = (float) (Math.round(((float) maxAllowedWidth / (float) maxAllowedHeight) * 100)) / 100;//获取预览比例

            for (Size size : list) {
                int width = size.width;//循环列表数据中的宽
                int height = size.height;//循环列表数据中的高
                float sizeAspectRatio = (float) (Math.round(((float) width / (float) height) * 100)) / 100;////循环列表数据中的比例
                Log.e("cfh", "width -- " + width + " height = " + height + " sizeAspectRatio " + sizeAspectRatio + " aspectRatio = " + aspectRatio);
                if (width <= maxAllowedWidth && height <= maxAllowedHeight) {
                    if (width >= calcWidth && height >= calcHeight) {
                        calcWidth = (int) width;
                        calcHeight = (int) height;
                        this.pictureSize = size;
                        Log.e("cfh", "**********pictureSize*************:" + pictureSize.height + "X" + pictureSize.width);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("cfh",e.toString());
        }
    }
}

