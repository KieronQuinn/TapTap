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
import com.kieronquinn.app.taptap.service.shizuku.TapTapShizukuShellService
import com.kieronquinn.app.taptap.shizuku.ITapTapShizukuService
import com.kieronquinn.app.taptap.shizuku.ITapTapShizukuShellService
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
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

interface TapTapShizukuServiceRepository {

    sealed class ShizukuServiceResponse<T> {
        data class Success<T>(val result: T) : ShizukuServiceResponse<T>()
        data class Failed<T>(val reason: Reason) : ShizukuServiceResponse<T>() {
            sealed class Reason(@StringRes override val contentRes: Int): ServiceEventEmitter.ServiceEvent.Failed.FailureReason {
                object ShizukuNotRunning : Reason(R.string.notification_service_error_content_shizuku_not_running)
                object ShizukuFailedToConnect : Reason(R.string.notification_service_error_content_shizuku_not_running)
                object ShizukuPermissionDenied : Reason(R.string.notification_service_error_content_shizuku_permission_denied)
                data class Custom(override val contentRes: Int): Reason(contentRes)
            }
        }
    }

    suspend fun <T> runWithService(block: suspend (ITapTapShizukuService) -> T): ShizukuServiceResponse<T>
    suspend fun <T> runWithShellService(block: suspend (ITapTapShizukuShellService) -> T): ShizukuServiceResponse<T>
    fun <T> runWithServiceIfAvailable(block: (ITapTapShizukuService) -> T?): T?
    fun <T> runWithShellServiceIfAvailable(block: (ITapTapShizukuShellService) -> T?): T?
    fun unbindServiceIfNeeded()
    fun unbindShellServiceIfNeeded()

}

class TapTapShizukuServiceRepositoryImpl(
    context: Context,
    scope: Scope,
    private val isColumbus: Boolean
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

    private val userServiceArgsShell: Shizuku.UserServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(
            context,
            TapTapShizukuShellService::class.java
        )
    ).apply {
        processNameSuffix("shizukushellservice")
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
    private var shellServiceInstance: ITapTapShizukuShellService? = null
    private var serviceConnection: ServiceConnection? = null
    private var shellServiceConnection: ServiceConnection? = null
    private val runMutex = Mutex()

    override suspend fun <T> runWithService(block: suspend (ITapTapShizukuService) -> T): TapTapShizukuServiceRepository.ShizukuServiceResponse<T> {
        return runWithService(::serviceInstance, ITapTapShizukuService.Stub::asInterface, ::userServiceArgs, ::serviceConnection, block)
    }

    override suspend fun <T> runWithShellService(block: suspend (ITapTapShizukuShellService) -> T): TapTapShizukuServiceRepository.ShizukuServiceResponse<T> {
        return runWithService(::shellServiceInstance, ITapTapShizukuShellService.Stub::asInterface, ::userServiceArgsShell, ::shellServiceConnection, block)
    }

    private suspend fun <T, S> runWithService(
        serviceInstance: KMutableProperty0<S?>,
        asInterface: (IBinder) -> S,
        userServiceArgs: KProperty0<Shizuku.UserServiceArgs>,
        cachedServiceConnection: KMutableProperty0<ServiceConnection?>,
        block: suspend (S) -> T
    ): TapTapShizukuServiceRepository.ShizukuServiceResponse<T> = runMutex.withLock {
        awaitShizuku() ?: return TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed(
            TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed.Reason.ShizukuNotRunning
        )
        val permissionResult = requestPermissionIfRequired()
        if(permissionResult is TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed){
            return TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed(permissionResult.reason)
        }
        val service = withContext(Dispatchers.IO) {
            getServiceLocked(serviceInstance, asInterface, userServiceArgs, cachedServiceConnection)
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

    private suspend fun <S> getServiceLocked(
        serviceInstance: KMutableProperty0<S?>,
        asInterface: (IBinder) -> S,
        userServiceArgs: KProperty0<Shizuku.UserServiceArgs>,
        cachedServiceConnection: KMutableProperty0<ServiceConnection?>
    ) = suspendCoroutine<TapTapShizukuServiceRepository.ShizukuServiceResponse<S>> { resume ->
        runBlocking {
            getServiceMutex.lock()
            var resumed = false
            serviceInstance.get()?.let {
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
                        val service = asInterface(binder)
                        serviceInstance.set(service)
                        cachedServiceConnection.set(this)
                        service
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
                    resume.resume(result as TapTapShizukuServiceRepository.ShizukuServiceResponse<S>)
                    getServiceMutex.unlock()
                }

                override fun onServiceDisconnected(component: ComponentName) {
                    serviceInstance.set(null)
                    cachedServiceConnection.set(null)
                }
            }
            Shizuku.bindUserService(userServiceArgs.get(), serviceConnection)
        }
    }

    @Synchronized
    override fun unbindServiceIfNeeded() {
        serviceConnection?.let {
            Shizuku.unbindUserService(userServiceArgs, it, true)
        }
    }

    @Synchronized
    override fun unbindShellServiceIfNeeded() {
        shellServiceConnection?.let {
            Shizuku.unbindUserService(userServiceArgsShell, it, true)
        }
    }

    override fun <T> runWithServiceIfAvailable(block: (ITapTapShizukuService) -> T?): T? {
        return serviceInstance?.let {
            block(it)
        }
    }

    override fun <T> runWithShellServiceIfAvailable(block: (ITapTapShizukuShellService) -> T?): T? {
        return shellServiceInstance?.let {
            block(it)
        }
    }

    override fun onScopeClose(scope: Scope) {
        if(isColumbus) {
            unbindServiceIfNeeded()
        }else{
            unbindShellServiceIfNeeded()
        }
        Shizuku.removeBinderDeadListener(binderDeadListener)
    }

}