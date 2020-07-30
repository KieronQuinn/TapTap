package com.android.systemui.tuner


class TunerService {

    interface Tunable {
        fun onTuningChanged(arg1: String?, arg2: String?)
    }

    fun addTunable(arg1: Tunable, arg2: Array<String?>){
        //Not implemented
    }

}