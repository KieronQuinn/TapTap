package com.kieronquinn.app.taptap.utils.extensions

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
suspend fun <T> ReceiveChannel<T>.debounce(
    wait: Long = 50,
    context: CoroutineContext = Dispatchers.Default
): ReceiveChannel<T> {
    return withContext(context) {
        produce {
            var lastTimeout: Job? = null
            consumeEach {
                lastTimeout?.cancel()
                lastTimeout = launch {
                    delay(wait)
                    send(it)
                }
            }
            lastTimeout?.join()
        }
    }
}