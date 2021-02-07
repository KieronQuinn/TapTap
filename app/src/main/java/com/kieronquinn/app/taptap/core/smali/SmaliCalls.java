package com.kieronquinn.app.taptap.core.smali;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kieronquinn.app.taptap.utils.extensions.Extensions_SharedPrefsKt;

import static com.kieronquinn.app.taptap.core.TapSharedPreferences.SHARED_PREFERENCES_KEY_SENSITIVITY;

/*
    DO NOT EDIT OR MOVE THIS FILE WITHOUT ALSO CHANGING THE SMALI. IT WILL BREAK THE LIBRARY CODE.
 */

public class SmaliCalls {

    private static String tapRtModel = "tap7cls_pixel4.tflite";

    public static String getTapRTModel(String currentModel){
        Log.d("TapTap", "getTapRTModel " + tapRtModel);
        return tapRtModel;
    }

    public static Uri getConfigSettingsUri(){
        return Extensions_SharedPrefsKt.stringPrefToUri(SHARED_PREFERENCES_KEY_SENSITIVITY);
    }

    public static float getConfigSettingValue(Context context){
        String floatSetting = Extensions_SharedPrefsKt.getSharedStringPref(context, SHARED_PREFERENCES_KEY_SENSITIVITY, "0.5");
        if(floatSetting == null) floatSetting = "0.5";
        return Float.parseFloat(floatSetting);
    }

    public static void log(String message){
        Log.d("TapTap", message);
    }

    public static String getTapRtModel() {
        return tapRtModel;
    }

    public static void setTapRtModel(String tapRtModel) {
        SmaliCalls.tapRtModel = tapRtModel;
    }

}
