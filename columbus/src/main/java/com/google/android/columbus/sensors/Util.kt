package com.google.android.columbus.sensors

object Util {

    fun getMaxId(input: ArrayList<Float>): Int {
        var currentMax = -3.402823E+38f
        var id = 0
        for(i in 0 until input.size){
            if(currentMax < input[i]){
                currentMax = input[i]
                id = i
            }
        }
        return id
    }

}