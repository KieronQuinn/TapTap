package com.kieronquinn.app.taptap.utils.extensions

import android.graphics.Typeface
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

fun Snackbar.setTypeface(typeface: Typeface?) {
    val tv = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
    val actionTv = view.findViewById(com.google.android.material.R.id.snackbar_action) as TextView
    tv.typeface = typeface
    actionTv.typeface = typeface
}

fun Snackbar.onSwipeDismissed(block: () -> Unit) {
    addCallback(object: Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            if(event == DISMISS_EVENT_SWIPE){
                block()
            }
        }
    })
}