package com.kieronquinn.app.taptap.smaliint;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import com.google.android.systemui.columbus.gates.Gate;
import com.kieronquinn.app.taptap.utils.ExtensionsKt;

import java.util.Set;

public class SmaliCalls {

    private static String tapRtModel = "tap7cls_pixel4.tflite";

    public static String getTapRTModel(String currentModel){
        Log.d("TapTap", "getTapRTModel " + tapRtModel);
        return tapRtModel;
    }

    public static Uri getConfigSettingsUri(){
        Log.d("TapTap", "getConfigSettingsUri");
        return ExtensionsKt.stringPrefToUri(ExtensionsKt.SHARED_PREFERENCES_KEY_SENSITIVITY);
    }

    public static float getConfigSettingValue(Context context){
        String floatSetting = ExtensionsKt.getSharedStringPref(context, ExtensionsKt.SHARED_PREFERENCES_KEY_SENSITIVITY, "0.5");
        if(floatSetting == null) floatSetting = "0.5";
        Log.d("TapTap", "getConfigSettingValue, returning " + floatSetting);
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
