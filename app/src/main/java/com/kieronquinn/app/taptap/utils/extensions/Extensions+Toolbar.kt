package com.kieronquinn.app.taptap.utils.extensions

import android.view.View
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce

private const val CLICK_DEBOUNCE = 250L

fun Toolbar.onNavigationIconClicked() = callbackFlow<View> {
    setNavigationOnClickListener {
        trySend(it)
    }
    awaitClose {
        setOnClickListener(null)
    }
}.debounce(CLICK_DEBOUNCE)