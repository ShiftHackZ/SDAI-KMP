package com.shifthackz.aisdv1.presentation.appupdate

import com.shifthackz.aisdv1.domain.usecase.appupdate.CheckAppUpdateUseCase
import com.shifthackz.aisdv1.domain.entity.AppUpdate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-lived launch state, retained across host recreation and navigation.
 * Discovery runs at most once; dismissal lasts until the next process launch.
 *
 * @param checkUpdate Optional, time-bounded discovery policy.
 * @author Dmitriy Moroz
 */
internal class AppUpdateLaunchStateImpl(private val checkUpdate: CheckAppUpdateUseCase) :
    AppUpdateLaunchState {
    private var hasChecked = false
    private val mutableUpdate = MutableStateFlow<AppUpdate?>(null)
    override val update = mutableUpdate.asStateFlow()

    /**
     * Runs discovery once after the application reaches its first usable screen.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun checkOnce() {
        if (hasChecked) return
        hasChecked = true
        try {
            mutableUpdate.value = checkUpdate()
        } catch (error: CancellationException) {
            hasChecked = false
            throw error
        }
    }

    /**
     * Consumes the launch notification after dismissal or opening its destination.
     *
     * @author Dmitriy Moroz
     */
    override fun dismiss() {
        mutableUpdate.value = null
    }
}
