package com.samsung.android.backtap

import kotlin.math.sqrt

class DataQueue {

    private val mData = FloatArray(400)
    private var mElements = 0
    private var mHead = 0
    private var mMeanSquare = 0.0f
    private var mTail = 0

    var mean = 0.0f
        private set

    var std = 0.0f
        private set

    private fun checkIfEmpty(): Boolean {
        return mElements == 0
    }

    fun checkIfFull(): Boolean {
        return mElements == 400
    }

    fun checkIfNotFull(): Boolean {
        return !checkIfFull()
    }

    fun dequeue(): Float {
        val v = mHead
        val f = mData[v]
        val v1 = v + 1
        mHead = v1
        if (v1 == 400) {
            mHead = 0
        }
        --mElements
        return f
    }

    fun dequeue(z: Boolean) {
        val f = this.dequeue()
        if (z) {
            updateMeanAndStdForDequeue(f)
        }
    }

    fun enqueue(f: Float) {
        val v = mTail
        mData[v] = f
        mTail = if (v == 0x18F) 0 else v + 1
        ++mElements
    }

    fun enqueue(f: Float, z: Boolean) {
        this.enqueue(f)
        if (z) {
            updateMeanAndStdForEnqueue(f)
        }
    }

    val newestElement: Float
        get() = if (mTail == 0) mData[0x18F] else mData[mTail - 1]

    fun getValueAt(v: Int): Float {
        return mData[(mHead + v) % 400]
    }

    fun reset() {
        mHead = 0
        mTail = 0
        mean = 0.0f
        mMeanSquare = 0.0f
        std = 0.0f
        mElements = 0
    }

    private fun updateMeanAndStdForDequeue(f: Float) {
        if (checkIfEmpty()) {
            mean = 0.0f
            mMeanSquare = 0.0f
            std = 0.0f
            return
        }
        val v = mElements
        val f1 = (mean * (v + 1).toFloat() - f) / v.toFloat()
        mean = f1
        val f2 = (mMeanSquare * (v + 1).toFloat() - f * f) / v.toFloat()
        mMeanSquare = f2
        std = sqrt((f2 - f1 * f1).toDouble()).toFloat()
    }

    private fun updateMeanAndStdForEnqueue(f: Float) {
        val v = mElements
        val f1 = (mean * (v - 1).toFloat() + f) / v.toFloat()
        mean = f1
        val f2 = (mMeanSquare * (v - 1).toFloat() + f * f) / v.toFloat()
        mMeanSquare = f2
        std = sqrt((f2 - f1 * f1).toDouble()).toFloat()
    }

}