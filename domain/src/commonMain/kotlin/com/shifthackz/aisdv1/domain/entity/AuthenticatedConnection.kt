package com.shifthackz.aisdv1.domain.entity

import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials

/**
 * Immutable connection inputs captured together before network work starts.
 *
 * @param automatic1111Url Configured AUTOMATIC1111 prefix.
 * @param swarmUiUrl Configured Swarm UI prefix.
 * @param swarmUiModel Selected Swarm model from the same configuration.
 * @param credentials Confirmed protected credentials; never serialized or logged.
 * @param revision Captured provider-selection revision.
 * @author Dmitriy Moroz
 */
data class AuthenticatedConnection(
    val automatic1111Url: String,
    val swarmUiUrl: String,
    val swarmUiModel: String,
    val credentials: AuthorizationCredentials,
    val revision: Long,
) {
    /**
     * Omits endpoints, model names and credentials from incidental diagnostic formatting.
     *
     * @author Dmitriy Moroz
     */
    override fun toString(): String = "AuthenticatedConnection(revision=$revision)"
}
