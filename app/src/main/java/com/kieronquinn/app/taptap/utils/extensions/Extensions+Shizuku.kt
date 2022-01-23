package com.kieronquinn.app.taptap.utils.extensions

import android.content.pm.PackageManager
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepositoryImpl
import kotlinx.coroutines.suspendCancellableCoroutine
import rikka.shizuku.Shizuku
import java.util.*
import kotlin.coroutines.resume

private suspend fun awaitShizuku() = suspendCoroutineWithTimeout<Unit>(
    TapTapShizukuServiceRepositoryImpl.SHIZUKU_TIMEOUT
) {
    var resumed = false
    val binderListener = object: Shizuku.OnBinderReceivedListener {
        override fun onBinderReceived() {
            Shizuku.removeBinderReceivedListener(this)
            if(resumed) return
            resumed = true
            it.resume(Unit)
        }
    }
    Shizuku.addBinderReceivedListenerSticky(binderListener)
}

private suspend fun Shizuku_requestPermissionIfNeededPostBinder() = suspendCancellableCoroutine<Boolean> {
    if(Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED){
        it.resume(true)
        return@suspendCancellableCoroutine
    }
    val randomRequestCode = UUID.randomUUID().hashCode()
    val listener = object: Shizuku.OnRequestPermissionResultListener {
        override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
            if(requestCode != randomRequestCode) return
            Shizuku.removeRequestPermissionResultListener(this)
            it.resume(grantResult == PackageManager.PERMISSION_GRANTED)
        }
    }
    Shizuku.addRequestPermissionResultListener(listener)
    Shizuku.requestPermission(randomRequestCode)
    it.invokeOnCancellation {
        Shizuku.removeRequestPermissionResultListener(listener)
    }
}

suspend fun Shizuku_requestPermissionIfNeeded(): Boolean? {
    awaitShizuku() ?: return null
    return Shizuku_requestPermissionIfNeededPostBinder()
}