package com.kieronquinn.app.taptap.components.columbus.sensors

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface ServiceEventEmitter {

    sealed class ServiceEvent {
        object Started: ServiceEvent()
        data class Failed(val reason: FailureReason): ServiceEvent() {
            interface FailureReason {
                val contentRes: Int
            }
        }
    }

    val serviceEvent: Flow<ServiceEvent>
    suspend fun postServiceEvent(serviceEvent: ServiceEvent)

}

class ServiceEventEmitterImpl: ServiceEventEmitter {

    private val _serviceEvent = MutableSharedFlow<ServiceEventEmitter.ServiceEvent>()
    override val serviceEvent = _serviceEvent.asSharedFlow()
    override suspend fun postServiceEvent(serviceEvent: ServiceEventEmitter.ServiceEvent) {
        _serviceEvent.emit(serviceEvent)
    }

}