package com.kieronquinn.app.taptap.utils

/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import java.io.IOException
import com.squareup.picasso.Picasso.LoadedFrom.DISK
import android.graphics.drawable.PictureDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import kotlin.jvm.Throws

//Based on https://github.com/jaredrummler/AndroidProcesses/blob/master/demo/src/main/java/com/jaredrummler/android/processes/sample/picasso/AppIconRequestHandler.java

class AppIconRequestHandler(context: Context) : RequestHandler() {

    private val pm: PackageManager
    private val dpi: Int
    private var defaultAppIcon: Bitmap? = null

    private val fullResDefaultActivityIcon: Bitmap
        get() {
            if (defaultAppIcon == null) {
                val drawable: Drawable?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    drawable = Resources.getSystem().getDrawableForDensity(
                        android.R.mipmap.sym_def_app_icon, dpi
                    )
                } else {
                    drawable = Resources.getSystem().getDrawable(
                        android.R.drawable.sym_def_app_icon
                    )
                }
                defaultAppIcon = drawableToBitmap(drawable)
            }
            return defaultAppIcon ?: drawableToBitmap(ColorDrawable(Color.TRANSPARENT))
        }

    init {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        dpi = am.launcherLargeIconDensity
        pm = context.packageManager
    }

    override fun canHandleRequest(data: Request): Boolean {
        return data.uri != null && TextUtils.equals(data.uri.scheme, SCHEME_PNAME)
    }

    @Throws(IOException::class)
    override fun load(request: Request, networkPolicy: Int): RequestHandler.Result? {
        try {
            return RequestHandler.Result(
                getFullResIcon(request.uri.toString().split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]),
                DISK
            )
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }

    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getFullResIcon(packageName: String): Bitmap {
        return getFullResIcon(pm.getApplicationInfo(packageName, 0))
    }

    private fun getFullResIcon(info: ApplicationInfo): Bitmap {
        try {
            val resources = pm.getResourcesForApplication(info.packageName)
            if (resources != null) {
                val iconId = info.icon
                if (iconId != 0) {
                    return getFullResIcon(resources, iconId)
                }
            }
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        return fullResDefaultActivityIcon
    }

    private fun getFullResIcon(resources: Resources, iconId: Int): Bitmap {
        val drawable: Drawable?
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                drawable = resources.getDrawableForDensity(iconId, dpi, null)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                drawable = resources.getDrawableForDensity(iconId, dpi)
            } else {
                drawable = resources.getDrawable(iconId)
            }
        } catch (e: Resources.NotFoundException) {
            return fullResDefaultActivityIcon
        }

        return drawableToBitmap(drawable)
    }

    fun drawableToBitmap(drawable: Drawable?): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is PictureDrawable) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            canvas.drawPicture(drawable.picture)
            return bitmap
        }
        var width = drawable?.intrinsicWidth ?: 0
        width = if (width > 0) width else 1
        var height = drawable?.intrinsicHeight ?: 0
        height = if (height > 0) height else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable?.draw(canvas)
        return bitmap
    }

    companion object {

        val SCHEME_PNAME = "pname"
    }

}