package com.shifthackz.aisdv1.feature.appupdate.domain

import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate

/**
 * Queries the distribution that owns this installation. Transport and decoding failures propagate.
 * Unsupported distributions perform no network or SDK operations.
 *
 * @author Dmitriy Moroz
 */
interface AppUpdateGateway {
    /**
     * Returns a newer compatible release, or null when this installation is current.
     *
     * @author Dmitriy Moroz
     */
    suspend fun check(): AppUpdate?
}
