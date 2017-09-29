package com.qxcloud.imageprocess;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.qxcloud.imageprocess.editAPI.EditImageAPI;
import com.qxcloud.imageprocess.editAPI.EditImageMessage;
import com.qxcloud.imageprocess.editAPI.EditImgInterface;
import com.qxcloud.imageprocess.utils.FileUtils;
import com.qxcloud.imageprocess.utils.Logger;
import com.qxcloud.imageprocess.utils.URIUtils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

/**
 * CREATED BY:         heaton
 * CREATED DATE:       2017/9/18
 * CREATED TIME:       下午5:52
 * CREATED DESCRIPTION:
 */

public class ImageProcess extends CordovaPlugin implements EditImgInterface{

    public static final String ACTION_CAMERA = "com.qxcloud.imageprocess.activity.TakePhotoActivity";
    public static final String ACTION_CROP = "com.qxcloud.imageprocess.activity.CropImgActivity";
    public static final String ACTION_ALBUM = Intent.ACTION_PICK;

    public static final int REQUEST_ALBUM = 101;
    public static final int HANDLER_WHAT_CROP = 101;

    public static final String EXTRA_DEFAULT_SAVE_PATH = "default_save_path";
    public static final String EXTRA_DEFAULT_SELECT_PATH = "default_select_path";
    public static final String METHOD_OPEN_CAMERA = "openCamera";
    public static final String METHOD_OPEN_ALBUM = "openAlbum";
    public static final String METHOD_OPEN_CROP = "openCrop";

    public static final String EXTRA_DEFAULT_METHOD_ACTION = "method_action";

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLER_WHAT_CROP:
                    openCrop((Uri) msg.obj);
                    break;
            }
        }
    };

    private String mSavedFilePath;

    private CallbackContext callbackContext;

    private String mAction;

    private String mSelectFilePath;

    @Override
    public void onDestroy() {
        super.onDestroy();
        EditImageAPI.getInstance().unRegisterEditImg(this);
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        EditImageAPI.getInstance().registerEditImg(this);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.mAction = action;
        mSavedFilePath = args.optString(0);
        mSelectFilePath = args.optString(1);
        exeMethod();
        return true;
    }

    private void exeMethod(){
        if(TextUtils.isEmpty(mSavedFilePath)){
            mSavedFilePath = FileUtils.createFile(this.cordova.getActivity());
        }
        if(mAction.equals(METHOD_OPEN_CAMERA)){
            openCamera();
        }else if(mAction.equals(METHOD_OPEN_ALBUM)){
            openAlbum();
        }else if(mAction.equals(METHOD_OPEN_CROP)){
            if(!TextUtils.isEmpty(mSelectFilePath)){
                openCrop(Uri.parse(mSelectFilePath));
            }
        }
    }

    public void openCamera(){
        Intent intent = new Intent(ACTION_CAMERA);
        intent.putExtra(EXTRA_DEFAULT_SAVE_PATH, mSavedFilePath);
        intent.putExtra(EXTRA_DEFAULT_METHOD_ACTION, mAction);
        this.cordova.startActivityForResult(this,intent,102);
    }
    public void openAlbum(){
        Intent intent = new Intent(ACTION_ALBUM);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        this.cordova.startActivityForResult(this,intent,REQUEST_ALBUM);
    }

    public void openCrop(Uri selectedImageUri){
        if(selectedImageUri.getScheme().equals("file")) {//判断uri地址是以什么开头的
            selectedImageUri = URIUtils.getFileUri(this.cordova.getActivity(),selectedImageUri);
        }
        Cursor cursor = this.cordova.getActivity().getContentResolver().query(selectedImageUri, null, null, null, null);
        if(cursor !=null && cursor.moveToFirst()){
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            Logger.e("imagePath === "+imagePath);
            Intent intent = new Intent(ACTION_CROP);
            intent.putExtra(EXTRA_DEFAULT_SAVE_PATH, mSavedFilePath);
            intent.putExtra(EXTRA_DEFAULT_SELECT_PATH,imagePath);
            intent.putExtra(EXTRA_DEFAULT_METHOD_ACTION, mAction);
            this.cordova.startActivityForResult(this,intent,103);
        }
    }

    public void openCrop(String selectFilePath){
        Intent intent = new Intent(ACTION_CROP);
        intent.putExtra(EXTRA_DEFAULT_SAVE_PATH, mSavedFilePath);
        intent.putExtra(EXTRA_DEFAULT_SELECT_PATH,selectFilePath);
        intent.putExtra(EXTRA_DEFAULT_METHOD_ACTION, mAction);
        this.cordova.startActivityForResult(this,intent,104);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == REQUEST_ALBUM && resultCode == Activity.RESULT_OK){
            if (intent != null) {
                Message message = new Message();
                message.obj = intent.getData();
                message.what = HANDLER_WHAT_CROP;
                handler.sendMessage(message);
            }
        }
    }

    @Override
    public void onEditImgResult(int code, EditImageMessage editImageMessage) {
        if(code == 0 && editImageMessage.getWhat() == 0){
            callbackContext.success(Uri.fromFile(new File(mSavedFilePath)).toString());
        }
        if(code == 1 && editImageMessage.getWhat() == 1){
            callbackContext.error("请前往设置打开相机及内部存储权限");
        }else if(code == 2 && editImageMessage.getWhat() == 1){
            callbackContext.error("用户手动关闭页面");
        }
    }
}
