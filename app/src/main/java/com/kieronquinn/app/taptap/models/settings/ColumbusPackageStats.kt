package com.kieronquinn.app.taptap.models.settings

/**
 *  Data structure of the package stats stored in `Settings.Secure` for columbus, which can be
 *  abused to know if a package has had Columbus access granted (useful for Snapchat)
 *
 *  Refer to `Settings.Secure.columbus_package_stats`, eg:
 *
 *  `[{"packageName":"com.snapchat.android","shownCount":2,"lastDeny":0}]`
 */
class ColumbusPackageStats {

    var packageName: String? = null

    //We don't care about the shown count so skip that

    var lastDeny: Int? = null

}

fun Array<ColumbusPackageStats>.hasGrantedColumbusForPackage(packageName: String): Boolean {
    return any { it.packageName == packageName && it.lastDeny == 0 }
}