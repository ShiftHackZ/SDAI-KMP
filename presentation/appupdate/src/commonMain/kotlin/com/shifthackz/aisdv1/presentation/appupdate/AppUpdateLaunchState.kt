package com.shifthackz.aisdv1.presentation.appupdate

import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-lived launch notification state shared by native host recreations.
 *
 * @author Dmitriy Moroz
 */
interface AppUpdateLaunchState {
    /**
     * Pending optional update; null after dismissal or when no update is available.
     *
     * @author Dmitriy Moroz
     */
    val update: StateFlow<AppUpdate?>

    /**
     * Discovers updates once, permitting another attempt only if host cancellation interrupted it.
     *
     * @author Dmitriy Moroz
     */
    suspend fun checkOnce()

    /**
     * Consumes this notification for the rest of the process lifetime.
     *
     * @author Dmitriy Moroz
     */
    fun dismiss()
}
