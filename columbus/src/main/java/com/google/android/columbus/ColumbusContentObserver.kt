package com.google.android.columbus

import android.app.IActivityManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler

class ColumbusContentObserver(
    private val contentResolver: ContentResolverWrapper,
    private val settingsUri: Uri,
    private val callback: (Uri?) -> Unit,
    handler: Handler
) : ContentObserver(handler) {

    companion object {
        private const val TAG = "Columbus/ContentObserve"
    }

    /*private val userSwitchCallback = UserSwitchCallback()

    inner class UserSwitchCallback: SynchronousUserSwitchObserver() {
        override fun onUserSwitching(newUserId: Int) {
            updateContentObserver()
            callback.invoke(settingsUri)
        }
    }*/

    fun activate() {
        updateContentObserver()
        /*try {
            activityManagerService.registerUserSwitchObserver(
                userSwitchCallback,
                TAG
            )
        }catch (e: RemoteException){
            Log.e(TAG, "Failed to register user switch observer", e)
        }*/
    }

    fun deactivate() {
        contentResolver.unregisterContentObserver(this)
        /*try {
            activityManagerService.unregisterUserSwitchObserver(userSwitchCallback)
        }catch (e: RemoteException){
            Log.e(TAG, "Failed to unregister user switch observer",  e)
        }*/
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        callback.invoke(uri)
    }

    private fun updateContentObserver(){
        contentResolver.unregisterContentObserver(this)
        contentResolver.registerContentObserver(settingsUri, false, this, -2)
    }

}

class ColumbusContentObserverFactory(
    private val contentResolver: ContentResolverWrapper,
    private val handler: Handler
) {

    fun create(settingsUri: Uri, callback: (Uri?) -> Unit): ColumbusContentObserver {
        return ColumbusContentObserver(contentResolver, settingsUri, callback, handler)
    }

}