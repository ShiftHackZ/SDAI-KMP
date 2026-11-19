package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreRequest
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreResult
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreNotice
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.GenerationRestoreGate
import com.shifthackz.aisdv1.domain.gateway.backup.BackupChangesGateway
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.usecase.backup.RestoreBackupUseCase
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.feature.backup.restore.BackupProviderPlanner
import com.shifthackz.aisdv1.feature.backup.restore.BackupRestoreGuard
import com.shifthackz.aisdv1.feature.backup.restore.BackupRestoreWriter
import com.shifthackz.aisdv1.feature.backup.restore.BackupSessionCodec
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSessionEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSectionEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Applies explicitly selected categories with guarded provider consent and durable category-level retries.
 * @param archives Immutable staged archive reader.
 * @param preferences Strict selective preference writer.
 * @param configuration Existing checked configuration quarantine owner.
 * @param authorization Protected Basic Auth association owner.
 * @param selectionGuard Serializes conflict comparison and credential/configuration publication.
 * @param restoreGuard Serializes whole restore sessions.
 * @param planner Computes independent-provider conflicts without changing selection.
 * @param writer Atomic additive Room imports.
 * @param sessions Nonsecret selection encoder.
 * @param database Durable intent and committed-category ledger.
 * @param work Rejects replacement while generation work is active.
 * @param changes Refreshes retained settings, locale and storage readers after durable commits.
 * @param generationGate Rejects restore during generation and defers new generation until restoration finishes.
 * @param dispatchers Runs durable work on IO.
 * @author Dmitriy Moroz
 */
internal class RestoreBackupUseCaseImpl(
    private val archives: BackupArchiveStore,
    private val preferences: BackupPreferencesGateway,
    private val configuration: ConfigurationStore,
    private val authorization: AuthorizationStore,
    private val selectionGuard: ProviderSelectionGuard,
    private val restoreGuard: BackupRestoreGuard,
    private val planner: BackupProviderPlanner,
    private val writer: BackupRestoreWriter,
    private val sessions: BackupSessionCodec,
    private val database: BackupStorage,
    private val work: BackgroundWorkObserver,
    private val changes: BackupChangesGateway,
    private val generationGate: GenerationRestoreGate,
    private val dispatchers: DispatchersProvider,
) : RestoreBackupUseCase {

    override suspend fun invoke(
        request: BackupRestoreRequest,
        onProgress: (BackupProgress) -> Unit
    ): BackupRestoreResult = withContext(dispatchers.io) {
        restoreGuard.exclusive {
            generationGate.restoring {
                require(request.sessionId.matches(Regex("[0-9a-fA-F-]{36}"))) { "Invalid restore session" }
                require(request.selection.categories.isNotEmpty()) { "Select restore data" }
                val archive = archives.staged(request.handle)
                val summary = archives.summary(archive)
                require(request.selection.categories.all { category -> summary.entries.any { it.category == category && it.isAvailable } }) { "Unavailable backup section" }
                require(summary.providers.containsAll(request.selection.providers)) { "Unavailable backup provider" }
                val incoming = archives.preferences(archive)
                val ledger = database.restoreSessionDao()
                val selectionJson = sessions.encode(request.selection)
                val recoveredPreferences = mutableSetOf<BackupCategory>()
                val operationResult = runCatching {
                    val existing = ledger.get(request.sessionId)
                    check(existing == null || existing.archiveHandle == request.handle && existing.selection == selectionJson) { "Restore session belongs to another request" }
                    check(
                        ledger.pending()
                            .none { it.id != request.sessionId }) { "Finish or cancel the pending restore first" }
                    var hasFailed = false
                    val conflict = selectionGuard.withSelection {
                        check(!work.hasActiveTasks()) { "Wait for generation to finish before restoring" }
                        val hasRecoveredConfiguration = configuration.retryPendingConfiguration()
                        val recovered = preferences.recoverPendingWrite()
                        val hasProviderSection =
                            BackupCategory.PROVIDERS in request.selection.categories && "providers" !in ledger.completed(
                                request.sessionId
                            )
                        val credentials =
                            if (hasProviderSection) authorization.getAuthorizationCredentials() else AuthorizationCredentials.None
                        if (hasRecoveredConfiguration) recoveredPreferences += BackupCategory.PROVIDERS
                        recoveredPreferences += recovered
                        val plan = if (hasProviderSection) planner.plan(
                            preferences.read(),
                            incoming,
                            request.selection,
                            credentials
                        ) else null
                        if (plan != null && plan.conflicts.isNotEmpty() && plan.fingerprint != request.confirmedFingerprint) {
                            return@withSelection BackupRestoreResult.Conflict(
                                plan.conflicts,
                                plan.fingerprint
                            )
                        }
                        if (existing == null) ledger.insert(
                            RestoreSessionEntity(
                                request.sessionId,
                                request.handle,
                                selectionJson,
                                requiresConfiguration = request.shouldProceedToConfiguration
                            )
                        )
                        if (plan != null) {
                            currentCoroutineContext().ensureActive()
                            try {
                                withContext(NonCancellable) {
                                    if (plan.credentials != null) authorization.storeAuthorizationCredentials(
                                        plan.credentials
                                    ) { commitId ->
                                        preferences.writeProviders(plan.providers, commitId)
                                    } else preferences.writeProviders(plan.providers, null)
                                    ledger.mark(
                                        RestoreSectionEntity(
                                            request.sessionId,
                                            "providers",
                                            plan.providers.size.toLong(),
                                            plan.skippedCount.toLong(),
                                            plan.retainedCredentialProviders.sorted()
                                                .joinToString(",")
                                        )
                                    )
                                }
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                hasFailed = true
                            }
                        }
                        null
                    }
                    if (conflict != null) return@runCatching conflict
                    if (!hasFailed) {
                        for (category in listOf(
                            BackupCategory.SETTINGS,
                            BackupCategory.GALLERY,
                            BackupCategory.NETWORK
                        )) {
                            if (category !in request.selection.categories || category.key in ledger.completed(
                                    request.sessionId
                                )
                            ) continue
                            currentCoroutineContext().ensureActive()
                            onProgress(
                                BackupProgress(
                                    category,
                                    0,
                                    archive.manifest.sections.single { it.id == category.key }.itemCount
                                )
                            )
                            try {
                                when (category) {
                                    BackupCategory.SETTINGS -> withContext(NonCancellable) {
                                        preferences.writeSettings(incoming.settings)
                                        ledger.mark(
                                            RestoreSectionEntity(
                                                request.sessionId,
                                                "settings",
                                                incoming.settings.size.toLong()
                                            )
                                        )
                                    }

                                    BackupCategory.GALLERY -> writer.gallery(
                                        request.sessionId,
                                        archive,
                                        onProgress
                                    )

                                    BackupCategory.NETWORK -> writer.network(
                                        request.sessionId,
                                        archive
                                    )

                                    BackupCategory.PROVIDERS -> Unit
                                }
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                hasFailed = true; break
                            }
                        }
                    }
                    val results = ledger.results(request.sessionId)
                    val completed =
                        BackupCategory.entries.filter { category -> results.any { it.category == category.key } }
                            .toSet()
                    BackupRestoreResult.Finished(
                        completed,
                        summary.skippedCount + results.sumOf { it.skippedCount }.toInt(),
                        hasFailed,
                        results.mapNotNull { result ->
                            BackupCategory.entries.firstOrNull { it.key == result.category }
                                ?.let { it to result.importedCount }
                        }.toMap(),
                        results.mapNotNull { result ->
                            result.retainedCredentialProviders.split(',').filter { it.isNotBlank() }
                                .toSet().takeIf { it.isNotEmpty() }
                                ?.let { BackupRestoreNotice.SharedCredentialsRetained(it) }
                        }
                    )
                }
                val publicationResult = runCatching {
                    withContext(NonCancellable) {
                        val committed = ledger.results(request.sessionId)
                        val completedCategories =
                            BackupCategory.entries.filter { category -> committed.any { it.category == category.key } }
                                .toSet()
                        val changed = completedCategories + recoveredPreferences
                        if (changed.isNotEmpty()) changes.notifyChanged(changed)
                        if (completedCategories.containsAll(request.selection.categories)) ledger.finish(
                            request.sessionId
                        )
                    }
                }
                val operationFailure = operationResult.exceptionOrNull()
                val publicationFailure = publicationResult.exceptionOrNull()
                if (operationFailure != null) {
                    if (publicationFailure != null && publicationFailure !== operationFailure) operationFailure.addSuppressed(
                        publicationFailure
                    )
                    throw operationFailure
                }
                publicationResult.getOrThrow()
                operationResult.getOrThrow()
            }
        }
    }
}
