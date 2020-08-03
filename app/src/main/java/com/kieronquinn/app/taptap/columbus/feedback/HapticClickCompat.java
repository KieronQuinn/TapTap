//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.kieronquinn.app.taptap.columbus.feedback;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import com.google.android.systemui.columbus.feedback.FeedbackEffect;
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties;

import kotlin.jvm.internal.Intrinsics;

public final class HapticClickCompat implements FeedbackEffect {
    @SuppressLint("WrongConstant")
    private static final AudioAttributes SONIFICATION_AUDIO_ATTRIBUTES = (new Builder()).setContentType(4).setUsage(13).build();
    private final VibrationEffect progressVibrationEffect;
    private final ContentResolver contentResolver;
    private final VibrationEffect resolveVibrationEffect;
    private final Vibrator vibrator;
    private int dndMode;

    public HapticClickCompat(Context var1) {
        super();
        Intrinsics.checkParameterIsNotNull(var1, "context");
        this.vibrator = (Vibrator) var1.getSystemService(Context.VIBRATOR_SERVICE);
        this.progressVibrationEffect = getVibrationEffect(0);
        this.resolveVibrationEffect = getVibrationEffect(5);
        this.contentResolver = var1.getContentResolver();

    }

    public void onProgress(int var1, DetectionProperties var2) {
        try {
            dndMode = Settings.Global.getInt(contentResolver, "zen_mode");
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if ((var2 == null || !var2.isHapticConsumed()) && dndMode == 0) {
            Vibrator var3 = this.vibrator;
            if (var3 != null) {
                if (var1 == 3) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && resolveVibrationEffect != null) {
                        var3.vibrate(this.resolveVibrationEffect, SONIFICATION_AUDIO_ATTRIBUTES);
                    } else {
                        var3.vibrate(300);
                    }
                } else if (var1 == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && progressVibrationEffect != null) {
                        var3.vibrate(this.progressVibrationEffect, SONIFICATION_AUDIO_ATTRIBUTES);
                    } else {
                        var3.vibrate(200);
                    }
                }
            }
        }

    }

    private VibrationEffect getVibrationEffect(int effectId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return VibrationEffect.createPredefined(effectId);
        }
        return null;
    }
}
