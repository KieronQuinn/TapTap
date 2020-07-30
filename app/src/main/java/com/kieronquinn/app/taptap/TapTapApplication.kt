package com.kieronquinn.app.taptap

import android.app.Application
import com.kieronquinn.app.taptap.utils.AppIconRequestHandler
import com.squareup.picasso.Picasso

class TapTapApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Picasso.setSingletonInstance(Picasso.Builder(this)
                .addRequestHandler(AppIconRequestHandler(this))
                .build())
    }

}