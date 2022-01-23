package com.kieronquinn.app.taptap.components.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface TapTapServiceRouter {

    val serviceStartBus: Flow<Unit>
    val serviceStopBus: Flow<Unit>
    suspend fun onServiceStarted()
    suspend fun onServiceStopped()

}

class TapTapServiceRouterImpl: TapTapServiceRouter {

    override val serviceStartBus = MutableSharedFlow<Unit>()
    override val serviceStopBus = MutableSharedFlow<Unit>()

    override suspend fun onServiceStarted() {
        serviceStartBus.emit(Unit)
    }

    override suspend fun onServiceStopped() {
        serviceStopBus.emit(Unit)
    }

}