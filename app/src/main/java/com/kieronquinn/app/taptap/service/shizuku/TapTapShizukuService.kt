package com.kieronquinn.app.taptap.service.shizuku

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.ILauncherApps
import android.content.pm.ParceledListSlice
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutQueryWrapper
import android.graphics.drawable.Icon
import android.hardware.location.ContextHubClient
import android.hardware.location.ContextHubManager
import android.hardware.location.NanoAppMessage
import android.os.IBinder
import android.os.RemoteException
import android.os.UserHandle
import android.util.Log
import com.android.internal.statusbar.IStatusBarService
import com.kieronquinn.app.taptap.contexthub.IContextHubClientCallback
import com.kieronquinn.app.taptap.contexthub.IRemoteContextHubClient
import com.kieronquinn.app.taptap.models.appshortcut.AppShortcutIcon
import com.kieronquinn.app.taptap.shizuku.ITapTapShizukuService
import com.kieronquinn.app.taptap.utils.contexthub.ContextHubClientCallbackRemoteToLocalWrapper
import com.kieronquinn.app.taptap.utils.extensions.makeShellIfNeeded
import com.topjohnwu.superuser.internal.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.SystemServiceHelper

@SuppressLint("RestrictedApi", "WrongConstant", "MissingPermission")
class TapTapShizukuService : ITapTapShizukuService.Stub() {

    companion object {
        private const val TAG = "TapTapShizuku"
        private const val PACKAGE_SHELL = "com.android.shell"
    }

    init {
        makeShellIfNeeded()
    }

    private val context by lazy {
        Utils.getContext()
    }

    private val launcherApps by lazy {
        val launcherAppsProxy = SystemServiceHelper.getSystemService("launcherapps")
        ILauncherApps.Stub.asInterface(launcherAppsProxy)
    }

    private val statusBar by lazy {
        val statusBarServiceProxy = SystemServiceHelper.getSystemService("statusbar")
        IStatusBarService.Stub.asInterface(statusBarServiceProxy)
    }

    private fun getUserHandle(): UserHandle {
        return Context::class.java.getMethod("getUser").invoke(context) as UserHandle
    }

    private fun getUserId(): Int {
        return UserHandle::class.java.getMethod("getIdentifier").invoke(getUserHandle()) as Int
    }

    /**
     *  ContextHubProxy for CHRE access. This only works when the Columbus nanoapp is installed,
     *  which doesn't seem to be port-able.
     *
     *  As it's a system nanoapp, it does not need loading or unloading.
     */
    private val contextHubClient = object : IRemoteContextHubClient.Stub(), IBinder.DeathRecipient {

        private var contextHubClient: ContextHubClient? = null

        init {
            linkToDeath(this, 0)
        }

        private fun initializeContextHubClientIfNull(callback: IContextHubClientCallback) {
            if (contextHubClient == null) {
                val contextHubManager = context.getSystemService("contexthub") as ContextHubManager
                val contextHub = contextHubManager.contextHubs.firstOrNull() ?: run {
                    Log.e(TAG, "No context hubs found")
                    return
                }
                contextHubClient = contextHubManager.createClient(
                    contextHub,
                    ContextHubClientCallbackRemoteToLocalWrapper(callback)
                )
            }
        }

        override fun sendMessageToNanoApp(
            callback: IContextHubClientCallback,
            message: NanoAppMessage
        ): Int {
            initializeContextHubClientIfNull(callback)
            return contextHubClient?.sendMessageToNanoApp(message)
                ?: throw RemoteException("Unable to get ContextHub")
        }

        fun close() {
            contextHubClient?.close()
        }

        override fun binderDied() {
            //Force a restart the next time the client is needed
            contextHubClient = null
        }

    }

    override fun getRemoteContextHubClient(): IRemoteContextHubClient {
        return contextHubClient
    }

    override fun getShortcuts(query: ShortcutQueryWrapper): ParceledListSlice<ShortcutInfo> {
        val token = clearCallingIdentity()
        val shortcuts = launcherApps.getShortcuts(
            PACKAGE_SHELL,
            query,
            getUserHandle()
        ) as ParceledListSlice<ShortcutInfo>
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
        GlobalScope.launch {
            val token = clearCallingIdentity()
            allowBackgroundStarts()
            delay(250)
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
        GlobalScope.launch {
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
        contextHubClient.close()
        System.exit(0)
    }

    override fun inputKeyEvent(keyId: Int) {
        Runtime.getRuntime().exec("input keyevent $keyId")
    }

}