//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.kieronquinn.app.taptap.core.columbus.actions

import androidx.annotation.StringRes
import android.content.Context
import android.widget.Toast
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor.DetectionProperties
import kotlin.jvm.internal.Intrinsics

class ToastAction(context: Context, @StringRes private val toastTextId: Int) : Action(context, null) {

    override fun isAvailable(): Boolean {
        return true
    }

    override fun onProgress(var1: Int, var2: DetectionProperties?) {
        if (var1 == 3) {
            onTrigger()
        }
    }

    override fun onTrigger() {
        Toast.makeText(context, context.getString(toastTextId), Toast.LENGTH_LONG).show()
    }

    override fun toString(): String {
        val var1 = StringBuilder()
        var1.append(super.toString())
        var1.append("]")
        return var1.toString()
    }

    init {
        Intrinsics.checkParameterIsNotNull(context, "context")
    }
}