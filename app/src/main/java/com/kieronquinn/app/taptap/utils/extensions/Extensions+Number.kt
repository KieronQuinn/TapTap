package com.kieronquinn.app.taptap.utils.extensions

import kotlin.math.abs

fun <T> Collection<T>.closestValueBy(value: Double, selector: (T) -> Double) = minByOrNull { abs(value - selector(it)) }!!