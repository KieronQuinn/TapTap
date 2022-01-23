package com.kieronquinn.app.taptap.utils.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FlowQueue<T> {

    private val flow = MutableSharedFlow<Unit>()
    private val queue = ArrayDeque<T>()

    fun asFlow(): Flow<Unit> = flow
    fun asQueue(): ArrayDeque<T> = queue

    suspend fun add(item: T) {
        queue.addLast(item)
        flow.emit(Unit)
    }

}