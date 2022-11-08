package com.kieronquinn.app.taptap.utils.foldable

import android.content.Context
import com.lge.display.DisplayManagerHelper

class LGFoldableProvider(context: Context): FoldableProvider {

    companion object {
        fun isAvailable(): Boolean {
            return try {
                Class.forName("com.lge.display.DisplayManagerHelper")
                true
            }catch (e: ClassNotFoundException){
                false
            }
        }
    }

    private val helper = if(isAvailable()){
        DisplayManagerHelper(context)
    }else null

    override fun isClosed(): Boolean {
        if(helper == null) return true
        return helper.coverState != DisplayManagerHelper.STATE_COVER_CLOSED
    }

}