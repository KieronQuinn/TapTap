package com.google.android.columbus

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.google.android.systemui.columbus.IColumbusService
import com.google.android.systemui.columbus.IColumbusServiceListener

class ColumbusServiceProxy : Service() {

    companion object {
        private const val TAG = "Columbus/ColumbusProxy"
    }

    inner class ColumbusServiceListener(
        private var token: IBinder?,
        private var listener: IColumbusServiceListener?
    ) : IBinder.DeathRecipient {

        private fun linkToDeath() {
            token?.let {
                try {
                    it.linkToDeath(this, 0)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Unable to linkToDeath", e)
                }
            }
        }

        init {
            linkToDeath()
        }

        override fun binderDied() {
            Log.w(TAG, "ColumbusServiceListener binder died")
            token = null
            listener = null
        }

        fun getListener(): IColumbusServiceListener? {
            return listener
        }

        fun getToken(): IBinder? {
            return token
        }

        fun unlinkToDeath(): Boolean? {
            return token?.unlinkToDeath(this, 0)
        }

    }

    inner class Binder : IColumbusService.Stub() {

        override fun registerGestureListener(
            token: IBinder?,
            listener: IBinder?
        ) {
            checkPermission()
            var v0 = columbusServiceListeners.size - 1
            if (v0 >= 0) {
                while (true) {
                    val v1 = v0 - 1
                    val currentServiceListener = columbusServiceListeners[v0].getListener()
                    if (currentServiceListener == null) {
                        columbusServiceListeners.removeAt(v0)
                    } else {
                        try {
                            currentServiceListener.setListener(token, listener)
                        } catch (e: RemoteException) {
                            Log.e(TAG, "Cannot set listener", e)
                            columbusServiceListeners.removeAt(v0)
                        }
                    }

                    if (v1 < 0) {
                        return
                    }

                    v0 = v1
                }
            }
        }

        override fun registerServiceListener(token: IBinder?, listener: IBinder?) {
            checkPermission()
            if (token == null) {
                Log.e(TAG, "Binder token must not be null")
                return
            }

            if (listener == null) {
                columbusServiceListeners.removeAll { it.getToken() == token }
            } else {
                columbusServiceListeners.add(
                    ColumbusServiceListener(
                        token,
                        IColumbusServiceListener.Stub.asInterface(listener)
                    )
                )
            }
        }

    }

    private val columbusServiceListeners = ArrayList<ColumbusServiceListener>()
    private val binder = Binder()

    private fun checkPermission() {
        enforceCallingOrSelfPermission(
            BuildConfig.COLUMBUS_PERMISSION_NAME,
            "Must have ${BuildConfig.COLUMBUS_PERMISSION_NAME} permission"
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY_COMPATIBILITY
    }

}