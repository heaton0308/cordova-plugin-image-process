package imagecrop.kevin.com.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

public class ImageEdit extends CordovaPlugin {

    public static final String METHOD_ECHO = "imageedit";
    private CallbackContext callbackContext;
    final CordovaPlugin that = this;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;
        this.callbackContext = callbackContext;
        if (METHOD_ECHO.equals(action)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                  //需要做的事情
                    Intent intent = new Intent("com.qxcloud.imageprocess.activity.TakePhoteActivity");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setPackage(that.cordova.getActivity().getApplicationContext().getPackageName());
                    that.cordova.startActivityForResult(that, intent, 1);
                }
            });
        }else {
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode==1&&resultCode==1&&intent!=null){
            callbackContext.error("success");
        }else {
            callbackContext.error("error");
        }
    }
}
