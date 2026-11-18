package com.shifthackz.aisdv1.presentation.components.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

/**
 * Checks Android runtime permissions and delegates prompts to caller-owned Activity launchers.
 *
 * @author Dmitriy Moroz
 */
object PermissionUtil {

    /**
     * Checks legacy read/write storage grants on Android versions before API 33.
     * API 33+ returns false without launching obsolete storage permissions.
     *
     * @param context Context used to inspect the current permission grants.
     * @param onLaunch Receives missing legacy permissions for an Activity-owned request launcher.
     * @return True only when both legacy permissions are already granted on a supported OS version.
     *
     * @author Dmitriy Moroz
     */
    fun checkStoragePermission(
        context: Context,
        onLaunch: (missingPermissions: Array<String>) -> Unit = {},
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return false
        }
        val missingPermissions = buildList {
            Manifest.permission.READ_EXTERNAL_STORAGE.takeIf {
                ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }?.let(::add)

            Manifest.permission.WRITE_EXTERNAL_STORAGE.takeIf {
                ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }?.let(::add)
        }
        if (missingPermissions.isEmpty()) return true
        onLaunch(missingPermissions.toTypedArray())
        return false
    }

    /**
     * Checks notification permission, requesting it through the caller only on Android 13 and newer.
     *
     * @param context Context used to inspect the notification grant.
     * @param onLaunch Receives the missing notification permission for an Activity-owned launcher.
     * @return True when permission is granted or not required by this OS version.
     *
     * @author Dmitriy Moroz
     */
    fun checkNotificationPermission(
        context: Context,
        onLaunch: (missingPermission: String) -> Unit = {},
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onLaunch(Manifest.permission.POST_NOTIFICATIONS)
            return false
        }
        return true
    }

}
