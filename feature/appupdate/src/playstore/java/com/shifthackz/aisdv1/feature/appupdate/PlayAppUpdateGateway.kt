package com.shifthackz.aisdv1.feature.appupdate

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.shifthackz.aisdv1.domain.gateway.AppUpdateGateway
import com.shifthackz.aisdv1.domain.entity.AppUpdate
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Delegates availability and eligibility to Play without contacting GitHub.
 *
 * @param manager Process-scoped Play update client.
 * @author Dmitriy Moroz
 */
internal class PlayAppUpdateGateway(private val manager: AppUpdateManager) : AppUpdateGateway {
    override suspend fun check(): AppUpdate? = suspendCancellableCoroutine { continuation ->
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (continuation.isActive) {
                val hasUpdate = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                        info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                continuation.resume(
                    if (hasUpdate && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
                        AppUpdate(info.availableVersionCode().toString(), null) else null
                )
            }
        }.addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWithException(error)
        }
    }
}
