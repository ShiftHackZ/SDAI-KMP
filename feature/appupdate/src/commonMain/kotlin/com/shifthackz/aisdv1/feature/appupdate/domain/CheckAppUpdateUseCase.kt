package com.shifthackz.aisdv1.feature.appupdate.domain

import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate

/**
 * Optional launch check; failure must never prevent entering the application.
 *
 * @author Dmitriy Moroz
 */
fun interface CheckAppUpdateUseCase {
    /**
     * Returns an available update, or null when unavailable or temporarily unreachable.
     * Cancellation remains observable to the launch owner.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): AppUpdate?
}
