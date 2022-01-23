package com.kieronquinn.app.taptap.utils.extensions

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import net.dinglisch.android.tasker.TaskerIntent

fun Context.doesHavePermissions(vararg permissions: String): Boolean {
    return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
}

fun Context.getRequiredPermissions(vararg permission: String): Array<String> {
    return permission.filter { !doesHavePermissions(it) }.toTypedArray()
}

/**
 *  Checks if the user has previously specifically denied the permission, ie. they need to grant from
 *  settings.
 */
fun Activity.isPermissionDenied(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}

fun Context.doesHaveNotificationPolicyAccess(): Boolean {
    return (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted
}

fun Context.getPermissionName(permission: String) : CharSequence {
    return packageManager.getPermissionInfo(permission, 0).loadLabel(packageManager)
}

fun Context.doesHaveTaskerPermission(): Boolean {
    return TaskerIntent.testStatus(this) == TaskerIntent.Status.OK
}