package com.kieronquinn.app.taptap.utils.extensions

import android.app.Activity

fun Activity.restart(){
    finish()
    startActivity(intent)
}