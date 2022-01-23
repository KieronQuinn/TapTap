package com.kieronquinn.app.taptap.utils.extensions

import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun TabLayout.selectTab(position: Int){
    selectTab(getTabAt(position))
}

fun TabLayout.onSelected(includeReselection: Boolean = false) = callbackFlow {
    val listener = object: TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            trySend(tab.position)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            //No-op
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            if(includeReselection){
                trySend(tab.position)
            }
        }
    }
    addOnTabSelectedListener(listener)
    awaitClose {
        removeOnTabSelectedListener(listener)
    }
}