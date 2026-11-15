package com.shifthackz.aisdv1.feature.auth.transaction

import kotlinx.serialization.Serializable

/**
 * Credential staging record stored through the platform authorization store.
 * Android encrypts this store; iOS uses its existing preferences storage. Existing credential keys
 * remain unchanged until configuration commits.
 *
 * @param version Encoding version; unknown versions remain unreadable rather than being discarded.
 * @param id Candidate configuration association.
 * @param previousId Previously confirmed association, empty for legacy installations.
 * @param type Candidate legacy credential discriminator.
 * @param raw Candidate legacy credential JSON; never copied to plaintext configuration storage.
 * @author Dmitriy Moroz
 */
@Serializable
internal data class PendingAuthorizationUpdate(
    val version: Int = 1,
    val id: String,
    val previousId: String,
    val type: String,
    val raw: String,
)
