package com.kieronquinn.app.taptap.service.shizuku

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.ILauncherApps
import android.content.pm.ParceledListSlice
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutQueryWrapper
import android.graphics.drawable.Icon
import android.os.UserHandle
import com.android.internal.statusbar.IStatusBarService
import com.kieronquinn.app.taptap.models.appshortcut.AppShortcutIcon
import com.kieronquinn.app.taptap.shizuku.ITapTapShizukuShellService
import com.kieronquinn.app.taptap.utils.extensions.makeShellIfNeeded
import com.topjohnwu.superuser.internal.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.SystemServiceHelper
import kotlin.system.exitProcess

@SuppressLint("RestrictedApi", "WrongConstant", "MissingPermission")
class TapTapShizukuShellService: ITapTapShizukuShellService.Stub() {

    init {
        makeShellIfNeeded()
    }

    companion object {
        private const val TAG = "TapTapShizukuShell"
        private const val PACKAGE_SHELL = "com.android.shell"
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
        exitProcess(0)
    }

    override fun inputKeyEvent(keyId: Int) {
        Runtime.getRuntime().exec("input keyevent $keyId")
    }

}