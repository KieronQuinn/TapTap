package com.kieronquinn.app.taptap.repositories.service

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.IBinder
import android.util.Log
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.columbus.sensors.ServiceEventEmitter
import com.kieronquinn.app.taptap.service.shizuku.TapTapShizukuService
import com.kieronquinn.app.taptap.shizuku.ITapTapShizukuService
import com.kieronquinn.app.taptap.utils.extensions.suspendCoroutineWithTimeout
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeCallback
import rikka.shizuku.Shizuku
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface TapTapShizukuServiceRepository {

    sealed class ShizukuServiceResponse<T> {
        data class Success<T>(val result: T) : ShizukuServiceResponse<T>()
        data class Failed<T>(val reason: Reason) : ShizukuServiceResponse<T>() {
            sealed class Reason(@StringRes override val contentRes: Int): ServiceEventEmitter.ServiceEvent.Failed.FailureReason {
                object ShizukuNotRunning : Reason(R.string.notification_service_error_content_shizuku_not_running)
                object ShizukuFailedToConnect : Reason(R.string.notification_service_error_content_shizuku_not_running)
                object ShizukuPermissionDenied : Reason(R.string.notification_service_error_content_shizuku_permission_denied)
            }
        }
    }

    suspend fun <T> runWithService(block: suspend (ITapTapShizukuService) -> T): ShizukuServiceResponse<T>
    fun <T> runWithServiceIfAvailable(block: (ITapTapShizukuService) -> T?): T?
    fun unbindServiceIfNeeded()

}

class TapTapShizukuServiceRepositoryImpl(
    context: Context,
    scope: Scope,
    private val shouldKillOnClose: () -> Boolean = { true }
) : TapTapShizukuServiceRepository, ScopeCallback {

    private val userServiceArgs: Shizuku.UserServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(
            context,
            TapTapShizukuService::class.java
        )
    ).apply {
        processNameSuffix("shizukuservice")
        debuggable(BuildConfig.DEBUG)
        version(BuildConfig.VERSION_CODE)
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1
        val SHIZUKU_TIMEOUT = TimeUnit.SECONDS.toMillis(10)
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        serviceConnection = null
        serviceInstance = null
    }

    init {
        scope.registerCallback(this)
        Shizuku.addBinderDeadListener(binderDeadListener)
    }

    private var serviceInstance: ITapTapShizukuService? = null
    private var serviceConnection: ServiceConnection? = null
    private val runMutex = Mutex()

    override suspend fun <T> runWithService(block: suspend (ITapTapShizukuService) -> T): TapTapShizukuServiceRepository.ShizukuServiceResponse<T> = runMutex.withLock {
        awaitShizuku() ?: return TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed(
            TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed.Reason.ShizukuNotRunning
        )
        val permissionResult = requestPermissionIfRequired()
        if(permissionResult is TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed){
            return TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed(permissionResult.reason)
        }
        val service = withContext(Dispatchers.IO) {
            getServiceLocked()
        }
        val result = when (service) {
            is TapTapShizukuServiceRepository.ShizukuServiceResponse.Success -> TapTapShizukuServiceRepository.ShizukuServiceResponse.Success(
                block(service.result)
            )
            is TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed -> TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed(
                service.reason
            )
        }
        return result
    }

    private suspend fun requestPermissionIfRequired() =
        suspendCancellableCoroutine<TapTapShizukuServiceRepository.ShizukuServiceResponse<Boolean>> {
            if (Shizuku.checkSelfPermission() == PERMISSION_GRANTED) {
                it.resume(TapTapShizukuServiceRepository.ShizukuServiceResponse.Success(false))
                return@suspendCancellableCoroutine
            }
            val listener = object : Shizuku.OnRequestPermissionResultListener {
                override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                    if(requestCode != REQUEST_CODE_PERMISSION) return
                    Shizuku.removeRequestPermissionResultListener(this)
                    if (grantResult == PERMISSION_GRANTED) {
                        it.resume(TapTapShizukuServiceRepository.ShizukuServiceResponse.Success(true))
                    } else {
                        it.resume(
                            TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed(
                                TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed.Reason.ShizukuPermissionDenied
                            )
                        )
                    }
                }
            }
            Shizuku.addRequestPermissionResultListener(listener)
            it.invokeOnCancellation {
                Shizuku.removeRequestPermissionResultListener(listener)
            }
            Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
        }

    private suspend fun awaitShizuku() = suspendCoroutineWithTimeout<Unit>(SHIZUKU_TIMEOUT) {
        var resumed = false
        val binderListener = object: Shizuku.OnBinderReceivedListener {
            override fun onBinderReceived() {
                Shizuku.removeBinderReceivedListener(this)
                if(resumed) return
                resumed = true
                it.resume(Unit)
            }
        }
        Shizuku.addBinderReceivedListenerSticky(binderListener)
    }

    private val getServiceMutex = Mutex()

    private suspend fun getServiceLocked() = suspendCoroutine<TapTapShizukuServiceRepository.ShizukuServiceResponse<ITapTapShizukuService>> { resume ->
        runBlocking {
            getServiceMutex.lock()
            var resumed = false
            serviceInstance?.let {
                if (resumed) return@let
                resume.resume(TapTapShizukuServiceRepository.ShizukuServiceResponse.Success(it))
                resumed = true
                getServiceMutex.unlock()
                return@runBlocking
            }
            val serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(component: ComponentName, binder: IBinder?) {
                    if (resumed) return
                    val result = if (binder != null && binder.pingBinder()) {
                        serviceInstance = ITapTapShizukuService.Stub.asInterface(binder)
                        serviceConnection = this
                        serviceInstance
                    } else {
                        null
                    }.let {
                        if (it != null) {
                            TapTapShizukuServiceRepository.ShizukuServiceResponse.Success(it)
                        } else {
                            TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed(
                                TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed.Reason.ShizukuFailedToConnect
                            )
                        }
                    }
                    resumed = true
                    resume.resume(result)
                    getServiceMutex.unlock()
                }

                override fun onServiceDisconnected(component: ComponentName) {
                    serviceInstance = null
                    serviceConnection = null
                }
            }
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        }
    }

    @Synchronized
    override fun unbindServiceIfNeeded() {
        serviceConnection?.let {
            Shizuku.unbindUserService(userServiceArgs, it, true)
        }
    }

    override fun <T> runWithServiceIfAvailable(block: (ITapTapShizukuService) -> T?): T? {
        return serviceInstance?.let {
            block(it)
        }
    }

    override fun onScopeClose(scope: Scope) {
        //If the service is running in the settings, we should only kill it if it's not required elsewhere
        if(shouldKillOnClose()) {
            unbindServiceIfNeeded()
            Shizuku.removeBinderDeadListener(binderDeadListener)
        }
    }

}