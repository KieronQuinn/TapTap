package com.google.android.columbus

import com.kieronquinn.app.taptap.utils.lazy.LazyWrapper

open class ColumbusServiceWrapper(
    private val columbusSettings: ColumbusSettings,
    private val columbusService: LazyWrapper<ColumbusService>
) {

    inner class SettingsChangeListener: ColumbusSettings.ColumbusSettingsChangeListener {
        override fun onColumbusEnabledChange(enabled: Boolean){
            if(enabled){
                startService()
            }
        }
    }

    private val settingsChangeListener = SettingsChangeListener()
    private var started = false

    init {
        if(columbusSettings.isColumbusEnabled){
            startService()
        }else{
            columbusSettings.registerColumbusSettingsChangeListener(settingsChangeListener)
            columbusService.get()
        }
    }

    fun startService(){
        columbusSettings.unregisterColumbusSettingsChangeListener(settingsChangeListener)
        started = true
        columbusService.get()
    }

}