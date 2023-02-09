package com.kieronquinn.app.taptap.service.root

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.IActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SELinux
import android.os.UserHandle
import android.system.Os
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.models.service.ActivityContainer
import com.kieronquinn.app.taptap.models.service.SnapchatQuickTapState
import com.kieronquinn.app.taptap.root.ITapTapRootService
import com.kieronquinn.app.taptap.utils.extensions.SharedPreferences_openFile
import com.kieronquinn.app.taptap.utils.extensions.getContentProviderExternalCompat
import com.kieronquinn.app.taptap.utils.extensions.getIdentifier
import com.kieronquinn.app.taptap.utils.extensions.getUser
import com.kieronquinn.app.taptap.utils.extensions.queryCompat
import com.topjohnwu.superuser.internal.Utils
import com.topjohnwu.superuser.ipc.RootService
import rikka.shizuku.SystemServiceHelper
import java.io.File

@SuppressLint("RestrictedApi")
class TapTapRootService : ITapTapRootService.Stub() {

    companion object {
        private const val SNAPCHAT_PACKAGE_NAME = "com.snapchat.android"
        private const val SNAPCHAT_PROVIDER_NAME = "$SNAPCHAT_PACKAGE_NAME.provider"
        private const val SNAPCHAT_EXPERIMENT_PREFS_NAME = "APP_START_EXPERIMENT_PREFS.xml"

        /*
            Not sure which one (or more?) of these forces the feature to work on non-pixels,
            so we'll apply them all, they get reset the next time the device goes online anyway
         */
        private val SNAPCHAT_EXPERIMENT_PREF_KEYS = arrayOf(
            "CAMERA_QUICK_TAP_TO_CAMERA_DEEPLINK_OVERRIDE_ALLOWED",
            "CAMERA_QUICK_TAP_TO_CAMERA_DEEPLINK_OVERRIDE",
            "CAMERA_QUICK_TAP_TO_CAMERA_ENABLED",
            "LOCKSCREEN_MODE_ENABLED_STATE_PREFERENCE_KEY",
            "CAMERA_QUICK_TAP_TO_CAMERA_PREPARE_ENABLED",
            "CAMERA_QUICK_TAP_TO_CAMERA_PREPARE_FORCE_ENABLED",
            "CAMERA_QUICK_TAP_IS_PRIVACY_DISCLAIMER_ACCEPTED",
            "CAMERA_QUICK_TAP_TO_CAMERA_FORCE_ENABLED"
        )
    }

    private val context by lazy {
        Utils.getContext()
    }

    private val tapTapContext by lazy {
        context.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY)
    }

    private val activityManager by lazy {
        val activityManagerProxy = SystemServiceHelper.getSystemService("activity")
        IActivityManager.Stub.asInterface(activityManagerProxy)
    }

    private fun getUserId(): Int {
        return getUserHandle().getIdentifier()
    }

    private fun getUserHandle(): UserHandle {
        return context.getUser()
    }

    override fun isSnapchatQuickTapToSnapEnabled(): SnapchatQuickTapState {
        try {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                //ActivityManager unsupported, nothing we can do
                return SnapchatQuickTapState.ERROR
            }
            val provider = activityManager.getContentProviderExternalCompat(
                SNAPCHAT_PROVIDER_NAME,
                getUserId(),
                null,
                SNAPCHAT_PROVIDER_NAME
            ) ?: return SnapchatQuickTapState.ERROR
            val cursor = provider.queryCompat(
                SNAPCHAT_PACKAGE_NAME,
                Uri.parse("content://$SNAPCHAT_PROVIDER_NAME/lockscreen_mode_enabled_state")
            )
            cursor.moveToFirst()
            val result = cursor.getString(0) == "true"
            return if (result) SnapchatQuickTapState.ENABLED else SnapchatQuickTapState.DISABLED
        } catch (e: Exception) {
            return SnapchatQuickTapState.ERROR
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun applySnapchatOverride() {
        activityManager.forceStopPackage(SNAPCHAT_PACKAGE_NAME, getUserId())
        val snapchatContext =
            context.createPackageContext(SNAPCHAT_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY)
        val uid = snapchatContext.packageManager.getPackageUid(SNAPCHAT_PACKAGE_NAME, 0)
        val sharedPrefsDir = File(snapchatContext.filesDir.parentFile, "shared_prefs")
        val sharedPrefsFile = File(sharedPrefsDir, SNAPCHAT_EXPERIMENT_PREFS_NAME)
        val sharedPreferences = SharedPreferences_openFile(sharedPrefsFile)
        sharedPreferences.edit().apply {
            SNAPCHAT_EXPERIMENT_PREF_KEYS.forEach {
                putBoolean(it, true)
            }
        }.commit()
        Os.chown(sharedPrefsFile.absolutePath, uid, uid)
        SELinux.restorecon(sharedPrefsFile.absolutePath)
    }

    override fun startActivityPrivileged(
        activityContainer: ActivityContainer,
        intent: Intent
    ): Int {
        val activityOptions = if(activityContainer.enterResId != null && activityContainer.exitResId != null){
            ActivityOptions.makeCustomAnimation(tapTapContext, activityContainer.enterResId, activityContainer.exitResId)
        }else ActivityOptions.makeBasic()
        return activityManager.startActivityWithFeature(activityContainer.thread, context.packageName, null,
            intent, intent.type, null, null, 0, 0, null, activityOptions.toBundle())
    }

}

class TapTapRootServiceStarter : RootService() {

    override fun onBind(intent: Intent): IBinder {
        return TapTapRootService()
    }

}