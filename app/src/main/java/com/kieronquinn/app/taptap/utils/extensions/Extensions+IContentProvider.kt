/**
 *  Adapted from Shizuku-Server as this is not included in the SDK
 *  https://github.com/RikkaApps/Shizuku/blob/master/server/src/main/java/rikka/shizuku/server/api/IContentProviderUtils.java
 *
 *  Modified to be an extension from IContentProvider for use in the services.
 */

package com.kieronquinn.app.taptap.utils.extensions

import android.content.AttributionSource
import android.content.IContentProvider
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.system.Os

fun IContentProvider.queryCompat(
    callingPkg: String?,
    uri: Uri
): Cursor {
    return when {
        Build.VERSION.SDK_INT >= 31 -> {
            query(AttributionSource.Builder(Os.getuid()).setPackageName(callingPkg).build(), uri, null, null, null)
        }
        Build.VERSION.SDK_INT >= 30 -> {
            query(callingPkg, null as String?, uri, null, null, null)
        }
        Build.VERSION.SDK_INT >= 26 -> {
            query(callingPkg, uri, null, null, null)
        }
        else -> {
            query(callingPkg, uri, null, null, null, null, null)
        }
    }
}