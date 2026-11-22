package com.shifthackz.aisdv1.domain.gateway.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage

/**
 * Strict portable preference persistence; transport and storage errors propagate to the caller.
 *
 * @author Dmitriy Moroz
 */
interface BackupPreferencesGateway {
    /**
     * Recovers an already-started checked batch and identifies changed portable preference categories.
     * Authorization association and provider revision changes also report PROVIDERS, even without endpoint changes.
     * The caller must reconcile protected credentials before publishing these invalidations.
     *
     * @author Dmitriy Moroz
     */
    fun recoverPendingWrite(): Set<BackupCategory>

    /**
     * Filters removed fields and unknown enum values while rejecting malformed known scalar types.
     *
     * @param preferences Portable fields decoded from an archive.
     * @author Dmitriy Moroz
     */
    fun filterSupported(preferences: BackupPreferences): BackupPreferences

    /**
     * Reads meaningful provider or partial-setup groups with persisted field presence, plus effective app settings.
     * Auto-written empty/default provider snapshots do not make a provider configured.
     *
     * @author Dmitriy Moroz
     */
    fun read(): BackupPreferences

    /**
     * Atomically persists selected provider fields without selecting a provider.
     *
     * @param providers Known provider fields to replace; absent values retain their destination state.
     * @param authorizationCommitId New protected Basic Auth association, or null to preserve the existing association.
     * @author Dmitriy Moroz
     */
    fun writeProviders(
        providers: Map<String, Map<String, BackupValue>>,
        authorizationCommitId: String?
    )

    /**
     * Atomically persists known user settings; unsupported fields must be ignored.
     *
     * @param settings Typed allowlisted field replacements.
     * @author Dmitriy Moroz
     */
    fun writeSettings(settings: Map<String, BackupValue>)

    /**
     * Reads the durable first-launch stage without changing completed existing installations.
     *
     * @author Dmitriy Moroz
     */
    fun readStartupStage(): BackupStartupStage

    /**
     * Persists post-onboarding progression before its route is replaced.
     *
     * @param stage New durable startup stage.
     * @author Dmitriy Moroz
     */
    fun writeStartupStage(stage: BackupStartupStage)
}
