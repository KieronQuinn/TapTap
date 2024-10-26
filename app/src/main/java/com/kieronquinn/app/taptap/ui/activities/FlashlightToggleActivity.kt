package com.kieronquinn.app.taptap.ui.activities

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.whenCreated
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationChannel
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.take

/**
 *  Due to changes in Android 11, you can no longer toggle the Flashlight from a service.
 *  Instead, we fire this activity which has the sole purpose of toggling the torch, and then
 *  closing. If it hits a permission issue, it will show a notification and then close.
 */
class FlashlightToggleActivity: AppCompatActivity() {

    private val cameraManager by lazy {
        getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val torchCallback = callbackFlow {
        val callback = object: CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                super.onTorchModeChanged(cameraId, enabled)
                trySend(enabled)
            }
        }
        cameraManager.registerTorchCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose {
            cameraManager.unregisterTorchCallback(callback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }else{
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            toggleFlashlight()
        }else{
            showErrorNotification()
            finish()
        }
    }

    private fun toggleFlashlight(){
        lifecycle.whenCreated {
            torchCallback.take(1).collect {
                try {
                    val cameraId = cameraManager.cameraIdList[0]
                    cameraManager.setTorchMode(cameraId, !it)
                    delay(100)
                }catch (e: CameraAccessException){
                    //Camera in use
                }
                finish()
            }
        }
    }

    private fun showErrorNotification(){
        val clickIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        TapTapNotificationChannel.Action.showNotification(this, TapTapNotificationId.ACTION) {
            val content = getString(R.string.notification_action_flashlight_error_content)
            it.setOngoing(false)
            it.setSmallIcon(TapTapNotificationChannel.NOTIFICATION_ICON)
            it.setContentTitle(getString(R.string.notification_action_flashlight_error_title))
            it.setContentText(content)
            it.setAutoCancel(true)
            it.setCategory(Notification.CATEGORY_SERVICE)
            it.setStyle(NotificationCompat.BigTextStyle(it).bigText(content))
            it.setContentIntent(PendingIntent.getActivity(this, TapTapNotificationId.ACTION.ordinal, clickIntent, PendingIntent.FLAG_IMMUTABLE))
            it.priority = NotificationCompat.PRIORITY_HIGH
        }
    }

}