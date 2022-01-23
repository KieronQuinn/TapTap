package com.kieronquinn.app.taptap.utils.extensions

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun BottomSheetBehavior<*>.slideOffset() = callbackFlow {
    val callback = object: BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            trySend(slideOffset)
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            //No-op
        }
    }
    addBottomSheetCallback(callback)
    awaitClose {
        removeBottomSheetCallback(callback)
    }
}