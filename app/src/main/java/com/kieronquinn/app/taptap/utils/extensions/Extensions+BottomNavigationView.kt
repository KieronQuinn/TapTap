package com.kieronquinn.app.taptap.utils.extensions

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce

private const val CLICK_DEBOUNCE = 250L

fun BottomNavigationView.onItemClicked() = callbackFlow {
    val listenerReselected = NavigationBarView.OnItemReselectedListener {
        trySend(it.itemId)
    }
    val listenerSelected = NavigationBarView.OnItemSelectedListener {
        trySend(it.itemId)
        true
    }
    setOnItemReselectedListener(listenerReselected)
    setOnItemSelectedListener(listenerSelected)
    awaitClose {
        setOnItemSelectedListener(null)
        setOnItemReselectedListener(null)
    }
}.debounce(CLICK_DEBOUNCE)