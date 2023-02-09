package com.kieronquinn.app.taptap.utils.extensions

import android.app.view.SurfaceControlHidden
import android.view.SurfaceControl
import dev.rikka.tools.refine.Refine

fun SurfaceControl.Transaction.setBackgroundBlurRadius(
    surfaceControl: SurfaceControl, radius: Int
): SurfaceControlHidden.Transaction {
    return Refine.unsafeCast<SurfaceControlHidden.Transaction>(this)
        .setBackgroundBlurRadius(surfaceControl, radius) as SurfaceControlHidden.Transaction
}