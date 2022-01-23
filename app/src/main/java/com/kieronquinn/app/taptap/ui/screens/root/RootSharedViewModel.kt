package com.kieronquinn.app.taptap.ui.screens.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class RootSharedViewModel: ViewModel() {

    abstract val appReady: Flow<Boolean>
    abstract fun postDecisionMade()

}

class RootSharedViewModelImpl: RootSharedViewModel() {

    companion object {
        private const val SPLASH_MIN_DELAY = 1500L
    }

    private val decisionMade = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val splashTimeout = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply {
        viewModelScope.launch {
            delay(SPLASH_MIN_DELAY)
            emit(Unit)
        }
    }

    override val appReady = combine(decisionMade, splashTimeout) { _, _ -> true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override fun postDecisionMade() {
        viewModelScope.launch {
            decisionMade.emit(Unit)
        }
    }

}