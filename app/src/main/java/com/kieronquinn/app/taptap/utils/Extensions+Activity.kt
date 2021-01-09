package com.kieronquinn.app.taptap.utils

import android.app.Activity

fun Activity.restart(){
    finish()
    startActivity(intent)
}