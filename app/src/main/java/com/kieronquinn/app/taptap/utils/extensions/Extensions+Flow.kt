package com.kieronquinn.app.taptap.utils.extensions

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

suspend fun <T> Flow<T>.await(clazz: Class<out T>): T {
    return first {
        it != null && it!!::class.java == clazz
    }
}

suspend fun <T> StateFlow<T>.awaitState(clazz: Class<out T>): T {
    if(value != null && value!!::class.java == clazz) return value
    return await(clazz)
}

inline fun <reified T> instantCombine(vararg flows: Flow<T>) = channelFlow {
    val array= Array(flows.size) {
        false to (null as T?) // first element stands for "present"
    }

    flows.forEachIndexed { index, flow ->
        launch {
            flow.collect { emittedElement ->
                array[index] = true to emittedElement
                send(array.filter { it.first }.map { it.second })
            }
        }
    }
}