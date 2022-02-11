/**
 *  Adapted from Shizuku-API as this is not included in the SDK
 *  https://github.com/RikkaApps/Shizuku-API/blob/master/server-shared/src/main/java/rikka/shizuku/server/api/SystemService.java
 *
 *  Modified to be extensions from IActivityManager for use in the services.
 */

package com.kieronquinn.app.taptap.utils.extensions

import android.app.IActivityManager
import android.app.IActivityManager23
import android.content.IContentProvider
import android.os.Build
import android.os.IBinder
import dev.rikka.tools.refine.Refine

fun IActivityManager.getContentProviderExternalCompat(
    name: String?,
    userId: Int,
    token: IBinder?,
    tag: String?
): IContentProvider? {
    val provider: IContentProvider?
    if (Build.VERSION.SDK_INT >= 29) {
        provider = getContentProviderExternal(name, userId, token, tag)?.provider
    } else if (Build.VERSION.SDK_INT >= 26) {
        provider = getContentProviderExternal(name, userId, token)?.provider
    } else {
        provider = Refine.unsafeCast<IActivityManager23>(this)
            .getContentProviderExternal(name, userId, token).provider
    }
    return provider
}

