package com.kieronquinn.app.taptap.utils.contexthub

import android.annotation.SuppressLint
import android.hardware.location.ContextHubClient
import android.hardware.location.ContextHubClientCallback
import android.hardware.location.NanoAppMessage
import com.kieronquinn.app.taptap.contexthub.IContextHubClientCallback
import com.kieronquinn.app.taptap.contexthub.IRemoteContextHubClient

/*
 *  Wrappers for ContextHubClient and ContextHubClientCallback to translate to/from
 *  IRemoteContextHubClient and IContextHubClientCallback respectively.
 *
 *  This allows interaction with ContextHub from the app layer, without needing the restricted
 *  permissions, as it goes via Shizuku or Root.
 */

/**
 *  [ContextHubClient] -> [IRemoteContextHubClient]
 */
private class ContextHubClientLocalToRemoteWrapper(private val client: ContextHubClient) :
    IRemoteContextHubClient.Stub() {

    @SuppressLint("MissingPermission")
    override fun sendMessageToNanoApp(
        callback: IContextHubClientCallback?,
        message: NanoAppMessage
    ): Int {
        return client.sendMessageToNanoApp(message)
    }

}

/**
 *  [IRemoteContextHubClient] -> [ContextHubClient]
 */
@SuppressLint("MissingPermission")
class ContextHubClientRemoteToLocalWrapper(
    private val remote: IRemoteContextHubClient,
    private val callback: IContextHubClientCallback
) : ContextHubClient() {

    override fun sendMessageToNanoApp(message: NanoAppMessage): Int {
        return remote.sendMessageToNanoApp(callback, message)
    }

}

/**
 *  [ContextHubClientCallback] -> [IContextHubClientCallback]
 */
class ContextHubClientCallbackLocalToRemoteWrapper(
    private val callback: ContextHubClientCallback
) : IContextHubClientCallback.Stub() {

    private fun IRemoteContextHubClient.toClient(): ContextHubClientRemoteToLocalWrapper {
        return ContextHubClientRemoteToLocalWrapper(
            this,
            this@ContextHubClientCallbackLocalToRemoteWrapper
        )
    }

    override fun onHubReset(client: IRemoteContextHubClient) {
        callback.onHubReset(client.toClient())
    }

    override fun onMessageFromNanoApp(client: IRemoteContextHubClient, message: NanoAppMessage?) {
        callback.onMessageFromNanoApp(client.toClient(), message)
    }

    override fun onNanoAppAborted(
        client: IRemoteContextHubClient,
        nanoAppId: Long,
        abortCode: Int
    ) {
        callback.onNanoAppAborted(
            client.toClient(),
            nanoAppId, abortCode
        )
    }

    override fun onNanoAppLoaded(client: IRemoteContextHubClient, nanoAppId: Long) {
        callback.onNanoAppLoaded(
            client.toClient(), nanoAppId
        )
    }

}

/**
 *  [IContextHubClientCallback] -> [ContextHubClientCallback]
 */
class ContextHubClientCallbackRemoteToLocalWrapper(
    private val remote: IContextHubClientCallback
) : ContextHubClientCallback() {

    private fun ContextHubClient.toInterface(): ContextHubClientLocalToRemoteWrapper {
        return ContextHubClientLocalToRemoteWrapper(this)
    }

    override fun onHubReset(client: ContextHubClient) {
        try {
            remote.onHubReset(client.toInterface())
        }catch (e: Exception){
            //Died
        }
    }

    override fun onMessageFromNanoApp(client: ContextHubClient, message: NanoAppMessage) {
        try {
            remote.onMessageFromNanoApp(client.toInterface(), message)
        }catch (e: Exception) {
            //Died
        }
    }

    override fun onNanoAppAborted(client: ContextHubClient, nanoAppId: Long, abortCode: Int) {
        try {
            remote.onNanoAppAborted(client.toInterface(), nanoAppId, abortCode)
        }catch (e: Exception) {
            //Died
        }
    }

    override fun onNanoAppLoaded(client: ContextHubClient, nanoAppId: Long) {
        try {
            remote.onNanoAppLoaded(client.toInterface(), nanoAppId)
        }catch (e: Exception) {
            //Died
        }
    }

}