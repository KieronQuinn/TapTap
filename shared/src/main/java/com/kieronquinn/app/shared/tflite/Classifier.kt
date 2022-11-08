package com.kieronquinn.app.shared.tflite

interface Classifier {

    fun predict(input: ArrayList<Float>, size: Int): ArrayList<ArrayList<Float>>
    fun predictArray(input: Array<FloatArray>, output: Array<FloatArray>)

}