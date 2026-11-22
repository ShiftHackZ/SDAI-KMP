package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials

/**
 * Determines provider conflicts and shared-credential eligibility without mutating storage.
 *
 * @author Dmitriy Moroz
 */
internal interface BackupProviderPlanner {
    /**
     * Builds a presence-aware selective merge and consent fingerprint without changing stored data.
     *
     * @param current Destination portable settings with persisted presence.
     * @param incoming Supported archive fields including selected portable Basic Auth.
     * @param selection Explicitly selected provider children.
     * @param credentials Trusted destination Basic Auth snapshot used to protect other consumers.
     * @author Dmitriy Moroz
     */
    fun plan(
        current: BackupPreferences,
        incoming: BackupPreferences,
        selection: BackupSelection,
        credentials: AuthorizationCredentials
    ): BackupProviderPlan
}
