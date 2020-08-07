package com.kieronquinn.app.taptap.smaliint;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import com.google.android.systemui.columbus.gates.Gate;
import com.google.android.systemui.columbus.sensors.Highpass1C;
import com.google.android.systemui.columbus.sensors.Highpass3C;
import com.google.android.systemui.columbus.sensors.Lowpass1C;
import com.google.android.systemui.columbus.sensors.Lowpass3C;
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

    public static void lowpassAccSetPara(Lowpass3C lowpass3C, float para){
        lowpass3C.setPara(para);
    }

    public static void lowpassGyroSetPara(Lowpass3C lowpass3C, float para){
        lowpass3C.setPara(para);
    }

    public static void highpassAccSetPara(Highpass3C highpass3C, float para){
        highpass3C.setPara(para);
    }

    public static void highpassGyroSetPara(Highpass3C highpass3C, float para){
        highpass3C.setPara(para);
    }

    public static void lowpassKeySetPara(Lowpass1C lowpass1C, float para){
        lowpass1C.setPara(para);
    }

    public static void highpassKeySetPara(Highpass1C highpass1C, float para){
        highpass1C.setPara(para);
    }
}
