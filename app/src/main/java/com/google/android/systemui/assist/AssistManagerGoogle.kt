package com.google.android.systemui.assist

import android.os.Bundle
import com.android.systemui.assist.AssistManager
import com.kieronquinn.app.taptap.utils.getDependency
import de.robv.android.xposed.XposedHelpers

public class AssistManagerGoogle(classLoader: ClassLoader) : AssistManager() {

    private val assistManagerClass = XposedHelpers.findClass("com.android.systemui.assist.AssistManager", classLoader)
    private val assistManagerGoogleClass = XposedHelpers.findClassIfExists("com.google.android.systemui.assist.AssistManagerGoogle", classLoader)
    private val assistManagerInstance = classLoader.getDependency(assistManagerGoogleClass ?: assistManagerClass)

    fun startAssist(bundle: Bundle){
        assistManagerClass.getMethod("startAssist", Bundle::class.java).invoke(assistManagerInstance, bundle)
    }

    fun dispatchOpaEnabledState(){
        //Only available on Google SystemUI
        try {
            assistManagerClass.getMethod("dispatchOpaEnabledState").invoke(assistManagerInstance)
        }catch (e: Exception){
            //Ignore
        }
    }

    fun addOpaEnabledListener(arg1: OpaEnabledListener) {
        //Not implemented
    }

}