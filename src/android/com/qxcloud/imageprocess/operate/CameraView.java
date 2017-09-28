package com.qxcloud.imageprocess.operate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;

import com.qxcloud.imageprocess.utils.Logger;


public class CameraView extends JavaCameraView implements PictureCallback {

    private OnSavedListener onSavedListener;

    public void setOnSavedListener(OnSavedListener onSavedListener) {
        this.onSavedListener = onSavedListener;
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void takePicture() {
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        mCamera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this);
    }

    public void triggerFlash(boolean on){
        Camera.Parameters parameters = mCamera.getParameters();
        if(on){
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }else{
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }

        mCamera.setParameters(parameters);
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Logger.e("Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        Bitmap bmp = BitmapFactory.decodeByteArray(data,0,data.length);
        Logger.e("bmp === "+data.length/1024+" w = "+bmp.getWidth()+" h = "+bmp.getHeight());
        // Write the image in a file (in jpeg format)

        if(onSavedListener != null){
            onSavedListener.onSaved(data);
        }
    }

    public static interface OnSavedListener{
        void onSaved(byte[] data);
    }
}
