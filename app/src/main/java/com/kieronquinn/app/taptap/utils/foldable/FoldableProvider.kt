package com.kieronquinn.app.taptap.utils.foldable

import android.content.Context

interface FoldableProvider {

    companion object {
        fun isCompatible(context: Context): Boolean {
            if(SidecarProvider.isDeviceFoldable(context)) return true
            if(LGFoldableProvider.isAvailable()) return true
            return false
        }

        fun getProvider(context: Context): FoldableProvider? {
            return when {
                SidecarProvider.isDeviceFoldable(context) -> SidecarProvider(context)
                LGFoldableProvider.isAvailable() -> LGFoldableProvider(context)
                else -> null
            }
        }
    }

    fun isClosed(): Boolean

}