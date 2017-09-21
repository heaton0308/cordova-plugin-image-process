package com.qxcloud.imageprocess;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;

import com.qxcloud.imageprocess.utils.FileUtils;
import com.qxcloud.imageprocess.utils.Logger;
import com.qxcloud.imageprocess.utils.URIUtils;

import static android.content.Intent.ACTION_PICK;

import io.ionic.starter.R;

public class MainActivity extends Activity {

    private static final String ACTION_CAMERA = "com.qxcloud.imageprocess.activity.TakePhotoActivity";
    private static final String ACTION_CROP = "com.qxcloud.imageprocess.activity.CropImgActivity";
    private static final String ACTION_ALBUM = ACTION_PICK;

    private static final int REQUEST_ALBUM = 101;
    private static final int HANDLER_WHAT_CROP = 101;

    public static final String EXTRA_DEFAULT_SAVE_PATH = "default_save_path";
    public static final String EXTRA_DEFAULT_SELECT_PATH = "default_select_path";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openCamera(View view){
        Intent intent = new Intent(ACTION_CAMERA);
        intent.putExtra(EXTRA_DEFAULT_SAVE_PATH, FileUtils.createFile(this));
        startActivity(intent);
    }
    public void openAlbum(View view){
        Intent intent = new Intent(ACTION_ALBUM);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent,REQUEST_ALBUM);
    }

    public void openCrop(Uri selectedImageUri){
        if(selectedImageUri.getScheme().equals("file")) {//判断uri地址是以什么开头的
            selectedImageUri = URIUtils.getFileUri(this,selectedImageUri);
        }
        Cursor cursor = getContentResolver().query(selectedImageUri, null, null, null, null);
        if(cursor !=null && cursor.moveToFirst()){
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            Logger.e("imagePath === "+imagePath);
            Intent intent = new Intent(ACTION_CROP);
            intent.putExtra(EXTRA_DEFAULT_SAVE_PATH, FileUtils.createFile(this));
            intent.putExtra(EXTRA_DEFAULT_SELECT_PATH,imagePath);
            startActivity(intent);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ALBUM && resultCode == RESULT_OK){
            if (data != null) {
                Message message = new Message();
                message.obj = data.getData();
                message.what = HANDLER_WHAT_CROP;
                handler.sendMessage(message);
            }
        }
    }
}
