package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.AuthenticatedConnection

/**
 * Captures URL/model/credentials under the same guard as configuration commits.
 * Network operations run after this guard is released.
 *
 * @author Dmitriy Moroz
 */
interface AuthenticatedConnectionProvider {
    /**
     * Returns consistent request inputs, rejecting a stale explicit connection-test destination.
     *
     * @param automatic1111Url Optional expected AUTOMATIC1111 URL supplied by connection setup.
     * @param swarmUiUrl Optional expected Swarm UI URL supplied by connection setup.
     * @author Dmitriy Moroz
     */
    suspend fun snapshot(
        automatic1111Url: String? = null,
        swarmUiUrl: String? = null
    ): AuthenticatedConnection
}
