package com.android.systemui.statusbar

class NavigationBarController(private val actualNavigationBarController: Any, private val clazz: Class<*>) {

    fun touchAutoDim(state: Int){
        clazz.getMethod("touchAutoDim", Integer.TYPE).invoke(actualNavigationBarController, state)
    }

}