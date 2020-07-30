//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.kieronquinn.app.taptap.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

import com.google.android.systemui.columbus.actions.Action;
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties;
import com.topjohnwu.superuser.Shell;

import java.util.List;

import kotlin.jvm.internal.Intrinsics;

public final class OpenRecents extends Action {

    public void setTriggerListener(TriggerListener listener) {
        this.triggerListener = listener;
    }

    public interface TriggerListener {
        void onTrigger();
    }

    private TriggerListener triggerListener = null;

    public OpenRecents(Context var1) {
        super(var1, (List)null);
        Intrinsics.checkParameterIsNotNull(var1, "context");
    }

    public boolean isAvailable() {
        return true;
    }

    public void onProgress(int var1, DetectionProperties var2) {
        if (var1 == 3) {
            this.onTrigger();
        }

    }

    @SuppressLint("WrongConstant")
    public void onTrigger() {
        Log.d("XColumbus", "onTrigger");
        if(triggerListener != null) triggerListener.onTrigger();
        Shell.sh("input keyevent KEYCODE_APP_SWITCH").exec();
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        var1.append(super.toString());
        var1.append("]");
        return var1.toString();
    }
}
