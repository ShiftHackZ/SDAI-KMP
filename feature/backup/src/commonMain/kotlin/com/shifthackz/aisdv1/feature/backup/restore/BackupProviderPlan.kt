package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials

/**
 * Immutable selective provider merge prepared under the provider transition guard.
 *
 * @param providers Portable independent field replacements.
 * @param credentials Shared Basic Auth replacement, or null to retain destination credentials.
 * @param conflicts Configured providers whose existing values would change.
 * @param fingerprint Opaque consent revision binding both source and destination.
 * @param skippedCount Credential fields omitted to preserve unselected configured providers.
 * @param retainedCredentialProviders Unselected configured providers that prevented shared credential replacement.
 * @author Dmitriy Moroz
 */
internal data class BackupProviderPlan(
    val providers: Map<String, Map<String, BackupValue>>,
    val credentials: AuthorizationCredentials?,
    val conflicts: Set<String>,
    val fingerprint: String,
    val skippedCount: Int,
    val retainedCredentialProviders: Set<String> = emptySet(),
)
