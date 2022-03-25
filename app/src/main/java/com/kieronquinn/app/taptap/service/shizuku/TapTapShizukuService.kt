package com.kieronquinn.app.taptap.service.shizuku

import android.annotation.SuppressLint
import android.hardware.location.ContextHubClient
import android.hardware.location.ContextHubManager
import android.hardware.location.NanoAppMessage
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.kieronquinn.app.taptap.contexthub.IContextHubClientCallback
import com.kieronquinn.app.taptap.contexthub.IRemoteContextHubClient
import com.kieronquinn.app.taptap.shizuku.ITapTapShizukuService
import com.kieronquinn.app.taptap.utils.contexthub.ContextHubClientCallbackRemoteToLocalWrapper
import com.kieronquinn.app.taptap.utils.extensions.execGrantReadLogsPermission
import com.topjohnwu.superuser.internal.Utils
import kotlin.system.exitProcess

@SuppressLint("RestrictedApi", "WrongConstant", "MissingPermission")
class TapTapShizukuService : ITapTapShizukuService.Stub() {

    companion object {
        private const val TAG = "TapTapShizuku"
    }

    private val context by lazy {
        Utils.getContext()
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

    override fun grantReadLogsPermission() {
        execGrantReadLogsPermission()
    }

    override fun destroy() {
        contextHubClient.close()
        exitProcess(0)
    }

}