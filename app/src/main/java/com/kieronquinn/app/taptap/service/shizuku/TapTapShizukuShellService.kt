package com.kieronquinn.app.taptap.service.shizuku

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.ILauncherApps
import android.content.pm.ParceledListSlice
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.RemoteException
import android.os.SystemClock
import android.os.UserHandle
import com.android.internal.statusbar.IStatusBarService
import com.kieronquinn.app.taptap.models.appshortcut.AppShortcutIcon
import com.kieronquinn.app.taptap.models.appshortcut.ShortcutQueryWrapper
import com.kieronquinn.app.taptap.shizuku.ITapTapColumbusLogCallback
import com.kieronquinn.app.taptap.shizuku.ITapTapShizukuShellService
import com.kieronquinn.app.taptap.utils.extensions.makeShellIfNeeded
import com.topjohnwu.superuser.internal.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import rikka.shizuku.SystemServiceHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

@SuppressLint("RestrictedApi", "WrongConstant", "MissingPermission")
class TapTapShizukuShellService: ITapTapShizukuShellService.Stub() {

    init {
        makeShellIfNeeded()
    }

    companion object {
        private const val TAG = "TapTapShizukuShell"
        private const val PACKAGE_SHELL = "com.android.shell"

        private val LOGCAT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private const val COLUMBUS_LOG_MESSAGE_FOOTER = "[COLUMBUS] back tap detected!"
        private const val COLUMBUS_LOG_TAG = "CHRE"
    }

    private val context by lazy {
        Utils.getContext()
    }

    private val scope = MainScope()

    private val launcherApps by lazy {
        val launcherAppsProxy = SystemServiceHelper.getSystemService("launcherapps")
        ILauncherApps.Stub.asInterface(launcherAppsProxy)
    }

    private val statusBar by lazy {
        val statusBarServiceProxy = SystemServiceHelper.getSystemService("statusbar")
        IStatusBarService.Stub.asInterface(statusBarServiceProxy)
    }

    private val logcatLines = callbackFlow {
        val time = LOGCAT_TIME_FORMAT.format(LocalDateTime.now()) + ".000"
        val command = arrayOf("logcat", "-s", COLUMBUS_LOG_TAG, "-T", time)
        var process: Process? = null
        val processThread = Thread {
            process = ProcessBuilder(*command).start().also {
                it.inputStream.reader().useLines { lines ->
                    lines.forEach { line ->
                        trySend(Pair(SystemClock.elapsedRealtimeNanos(), line.trim()))
                    }
                }
            }
        }
        processThread.start()
        awaitClose {
            process?.destroy()
            processThread.interrupt()
        }
    }.flowOn(Dispatchers.IO)

    private val columbusTimestamps = logcatLines.mapNotNull {
        if(it.second.endsWith(COLUMBUS_LOG_MESSAGE_FOOTER)) it.first else null
    }

    private var columbsLogJob: Job? = null

    private fun getUserHandle(): UserHandle {
        return Context::class.java.getMethod("getUser").invoke(context) as UserHandle
    }

    private fun getUserId(): Int {
        return UserHandle::class.java.getMethod("getIdentifier").invoke(getUserHandle()) as Int
    }

    override fun getShortcuts(query: ShortcutQueryWrapper): ParceledListSlice<ShortcutInfo> {
        val token = clearCallingIdentity()
        val shortcuts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            launcherApps.getShortcuts(
                PACKAGE_SHELL,
                query.toSystemShortcutQueryWrapper(),
                getUserHandle()
            ) as ParceledListSlice<ShortcutInfo>
        } else {
            launcherApps.getShortcuts(
                PACKAGE_SHELL,
                0,
                null,
                null,
                null,
                query.queryFlags,
                getUserHandle()
            )
        }
        restoreCallingIdentity(token)
        return shortcuts
    }

    override fun getAppShortcutIcon(packageName: String, shortcutId: String): AppShortcutIcon {
        val token = clearCallingIdentity()
        val iconResId =
            launcherApps.getShortcutIconResId(PACKAGE_SHELL, packageName, shortcutId, getUserId())
        val iconUri =
            launcherApps.getShortcutIconUri(PACKAGE_SHELL, packageName, shortcutId, getUserId())
        val iconFd =
            launcherApps.getShortcutIconFd(PACKAGE_SHELL, packageName, shortcutId, getUserId())
        val icon = when {
            //Prioritise uri over res ID
            iconUri != null -> {
                Icon.createWithContentUri(iconUri)
            }
            iconResId != 0 -> {
                Icon.createWithResource(packageName, iconResId)
            }
            else -> null
        }
        val result = AppShortcutIcon(icon = icon, descriptor = iconFd)
        restoreCallingIdentity(token)
        return result
    }

    override fun startShortcut(packageName: String, shortcutId: String) {
        scope.launch {
            val token = clearCallingIdentity()
            allowBackgroundStarts()
            launcherApps.startShortcut(
                PACKAGE_SHELL,
                packageName,
                null,
                shortcutId,
                null,
                null,
                getUserId()
            )
            delay(250)
            resetBackgroundStarts()
            restoreCallingIdentity(token)
        }
    }

    override fun clickQuickSetting(component: String) {
        scope.launch {
            statusBar.expandSettingsPanel(component)
            delay(250L)
            statusBar.clickTile(ComponentName.unflattenFromString(component))
            delay(250L)
            statusBar.collapsePanels()
        }
    }

    private fun allowBackgroundStarts() {
        Runtime.getRuntime().exec("cmd device_config put activity_manager default_background_activity_starts_enabled true")
    }

    private fun resetBackgroundStarts() {
        Runtime.getRuntime().exec("cmd device_config put activity_manager default_background_activity_starts_enabled false")
    }

    override fun destroy() {
        scope.cancel()
        exitProcess(0)
    }

    override fun inputKeyEvent(keyId: Int) {
        Runtime.getRuntime().exec("input keyevent $keyId")
    }

    override fun setColumbusLogListener(callback: ITapTapColumbusLogCallback?) {
        columbsLogJob?.cancel()
        if(callback != null) {
            columbsLogJob = setupColumbusLog(callback)
        }
    }

    private fun setupColumbusLog(callback: ITapTapColumbusLogCallback) = scope.launch {
        columbusTimestamps.collect {
            try {
                callback.onLogEvent(it)
            }catch (e: RemoteException){
                //Disconnected
                return@collect
            }
        }
    }

}