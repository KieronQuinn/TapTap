package com.kieronquinn.app.taptap.repositories.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.kieronquinn.app.taptap.root.ITapTapRootService
import com.kieronquinn.app.taptap.service.root.TapTapRootServiceStarter
import com.kieronquinn.app.taptap.utils.extensions.suspendCoroutineWithTimeout
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

interface TapTapRootServiceRepository {

    suspend fun <T> runWithService(block: suspend (ITapTapRootService) -> T): T?
    fun <T> runWithServiceIfAvailable(block: (ITapTapRootService) -> T?): T?
    suspend fun unbindServiceIfNeeded()

}

class TapTapRootServiceRepositoryImpl(context: Context): TapTapRootServiceRepository {

    companion object {
        val ROOT_TIMEOUT = TimeUnit.SECONDS.toMillis(10)
    }

    private var serviceInstance: ITapTapRootService? = null
    private var serviceConnection: ServiceConnection? = null

    private val serviceIntent = Intent(context, TapTapRootServiceStarter::class.java)
    private val runMutex = Mutex()

    override suspend fun <T> runWithService(block: suspend (ITapTapRootService) -> T): T? = runMutex.withLock {
        val service = withContext(Dispatchers.IO) {
            getServiceLocked()
        } ?: return null
        return block(service)
    }

    private val getServiceMutex = Mutex()

    private suspend fun getServiceLocked() = suspendCoroutineWithTimeout<ITapTapRootService>(ROOT_TIMEOUT) { resume ->
        runBlocking {
            getServiceMutex.lock()
            serviceInstance?.let {
                resume.resume(it)
                getServiceMutex.unlock()
                return@runBlocking
            }
            val serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(component: ComponentName, binder: IBinder) {
                    serviceInstance = ITapTapRootService.Stub.asInterface(binder)
                    serviceConnection = this
                    resume.resume(serviceInstance!!)
                    getServiceMutex.unlock()
                }

                override fun onServiceDisconnected(component: ComponentName) {
                    serviceInstance = null
                    serviceConnection = null
                }
            }
            RootService.bind(serviceIntent, serviceConnection)
        }
    }

    override suspend fun unbindServiceIfNeeded() {
        serviceConnection?.let {
            RootService.unbind(it)
        }
    }

    override fun <T> runWithServiceIfAvailable(block: (ITapTapRootService) -> T?): T? {
        return serviceInstance?.let {
            block(it)
        }
    }

}