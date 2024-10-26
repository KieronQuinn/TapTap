package com.kieronquinn.app.taptap.utils.extensions

import android.animation.LayoutTransition
import android.app.view.ViewHidden
import android.app.view.ViewRootImpl
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import dev.rikka.tools.refine.Refine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val CLICK_DEBOUNCE = 250L

fun View.onClicked() = callbackFlow<View> {
    setOnClickListener {
        trySend(it)
    }
    awaitClose {
        setOnClickListener(null)
    }
}.debounce(CLICK_DEBOUNCE)

fun View.onLongClicked() = callbackFlow<View> {
    setOnLongClickListener {
        trySend(it)
        true
    }
    awaitClose {
        setOnClickListener(null)
    }
}.debounce(CLICK_DEBOUNCE)

fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun View.removeRipple() {
    setBackgroundResource(0)
}

fun View.addRippleForeground() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    foreground = ContextCompat.getDrawable(context, resourceId)
}

fun View.removeRippleForeground() {
    foreground = null
}

fun ViewGroup.enableChangingAnimations() {
    layoutTransition = (layoutTransition ?: LayoutTransition()).apply {
        enableTransitionType(LayoutTransition.CHANGING)
    }
}

/**
 *  Disables pre-draw for a given view for [delayTime] ms. Useful for allowing the splash to run.
 */
fun View.delayPreDrawFor(delayTime: Long, lifecycle: Lifecycle) {
    val listener = ViewTreeObserver.OnPreDrawListener {
        false
    }
    val removeListener = {
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    lifecycle.runOnDestroy {
        removeListener()
    }
    viewTreeObserver.addOnPreDrawListener(listener)
    lifecycle.coroutineScope.launch {
        delay(delayTime)
        removeListener()
    }
}

fun View.delayPreDrawUntilFlow(flow: Flow<Boolean>, lifecycle: Lifecycle) {
    val listener = ViewTreeObserver.OnPreDrawListener {
        false
    }
    val removeListener = {
        viewTreeObserver.removeOnPreDrawListener(listener)
    }
    lifecycle.runOnDestroy {
        removeListener()
    }
    viewTreeObserver.addOnPreDrawListener(listener)
    lifecycle.whenResumed {
        flow.collect {
            if(!it) return@collect
            removeListener()
        }
    }
}

suspend fun View.awaitPost() = suspendCancellableCoroutine<View> {
    post {
        if(isAttachedToWindow){
            it.resume(this)
        }else{
            it.cancel()
        }
    }
}

fun View.getViewRootImpl(): ViewRootImpl? {
    return Refine.unsafeCast<ViewHidden>(this).viewRootImpl
}