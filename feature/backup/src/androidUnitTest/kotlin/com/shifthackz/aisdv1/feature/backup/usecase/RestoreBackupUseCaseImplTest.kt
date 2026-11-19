package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupEntry
import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreRequest
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreResult
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.gateway.GenerationRestoreGate
import com.shifthackz.aisdv1.domain.gateway.backup.BackupChangesGateway
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.feature.backup.archive.StagedBackup
import com.shifthackz.aisdv1.feature.backup.codec.BackupManifest
import com.shifthackz.aisdv1.feature.backup.codec.BackupSection
import com.shifthackz.aisdv1.feature.backup.codec.DefaultBackupPreferencesCodec
import com.shifthackz.aisdv1.feature.backup.restore.DefaultBackupProviderPlanner
import com.shifthackz.aisdv1.feature.backup.restore.DefaultBackupRestoreGuard
import com.shifthackz.aisdv1.feature.backup.restore.DefaultBackupSessionCodec
import com.shifthackz.aisdv1.feature.backup.restore.BackupRestoreWriter
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSessionEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSectionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerifyOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class RestoreBackupUseCaseImplTest {
    private val archives = mockk<BackupArchiveStore>()
    private val preferences = mockk<BackupPreferencesGateway>(relaxed = true)
    private val configuration = mockk<ConfigurationStore>(relaxed = true)
    private val authorization = mockk<AuthorizationStore>(relaxed = true)
    private val writer = mockk<BackupRestoreWriter>()
    private val database = mockk<BackupStorage>()
    private val ledger = mockk<RestoreSessionDao>()
    private val work = mockk<BackgroundWorkObserver>()
    private val changes = mockk<BackupChangesGateway>(relaxed = true)
    private val gate = object : GenerationRestoreGate {
        override suspend fun <T> generating(block: suspend () -> T): T = block()
        override suspend fun <T> restoring(block: suspend () -> T): T = block()
    }
    private val sessions = DefaultBackupSessionCodec(Json)
    private val committed = mutableListOf<RestoreSectionEntity>()
    private val intents = mutableMapOf<String, RestoreSessionEntity>()
    private val request = BackupRestoreRequest(
        "11111111-1111-1111-1111-111111111111",
        BackupSelection(setOf(BackupCategory.GALLERY, BackupCategory.NETWORK)),
        "22222222-2222-2222-2222-222222222222"
    )
    private val archive = StagedBackup(
        request.handle,
        "/private/archive".toPath(),
        BackupManifest(
            "archive",
            1,
            "1",
            "IOS",
            BackupCategory.entries.map {
                BackupSection(
                    it.key,
                    storedBytes = 1,
                    decodedBytes = 1,
                    sha256 = "a".repeat(64),
                    itemCount = 1
                )
            })
    )
    private val summary = BackupInspection(
        request.handle,
        1,
        "1",
        "IOS",
        BackupCategory.entries.map { BackupEntry(it, 1) },
        setOf("custom")
    )
    private val useCase = RestoreBackupUseCaseImpl(
        archives,
        preferences,
        configuration,
        authorization,
        ProviderSelectionGuard(),
        DefaultBackupRestoreGuard(),
        DefaultBackupProviderPlanner(DefaultBackupPreferencesCodec(Json)),
        writer,
        sessions,
        database,
        work,
        changes,
        gate,
        mockk<DispatchersProvider> { every { io } returns Dispatchers.Unconfined })

    @Before
    fun setup() {
        every { database.restoreSessionDao() } returns ledger
        coEvery { archives.staged(request.handle) } returns archive
        every { archives.summary(archive) } returns summary
        every { archives.preferences(archive) } returns BackupPreferences(emptyMap(), emptyMap())
        every { preferences.read() } returns BackupPreferences(emptyMap(), emptyMap())
        coEvery { authorization.getAuthorizationCredentials() } returns AuthorizationCredentials.None
        every { work.hasActiveTasks() } returns false
        coEvery { ledger.get(any()) } answers { intents[firstArg()] }
        coEvery { ledger.pending() } answers { intents.values.filter { !it.isComplete } }
        coEvery { ledger.completed(any()) } answers {
            committed.filter { it.sessionId == firstArg<String>() }.map { it.category }
        }
        coEvery { ledger.results(any()) } answers { committed.filter { it.sessionId == firstArg<String>() } }
        coEvery { ledger.insert(any()) } answers {
            val value = firstArg<RestoreSessionEntity>(); intents[value.id] = value
        }
        coEvery { ledger.mark(any()) } answers { committed += firstArg<RestoreSectionEntity>() }
        coEvery { ledger.finish(any()) } answers {
            val id = firstArg<String>(); intents[id]?.let {
            intents[id] = it.copy(isComplete = true)
        }; Unit
        }
        coEvery {
            writer.gallery(
                any(),
                any(),
                any()
            )
        } coAnswers { ledger.mark(RestoreSectionEntity(firstArg(), "gallery", 1)) }
        coEvery { writer.network(any(), any()) } coAnswers {
            ledger.mark(
                RestoreSectionEntity(
                    firstArg(),
                    "network",
                    1
                )
            )
        }
    }

    @Test
    fun `given gallery restore committed before interruption, same session retries, expected gallery not added twice`() =
        runTest {
            // Given
            intents[request.sessionId] = RestoreSessionEntity(
                request.sessionId,
                request.handle,
                sessions.encode(request.selection)
            )
            committed += RestoreSectionEntity(request.sessionId, "gallery", 3)
            // When
            val result = useCase(request) {} as BackupRestoreResult.Finished
            // Then
            coVerify(exactly = 0) { writer.gallery(any(), any(), any()) }
            coVerify(exactly = 1) { writer.network(request.sessionId, archive) }
            assertEquals(3L, result.importedCounts[BackupCategory.GALLERY])
            assertFalse(result.hasFailed)
        }

    @Test
    fun `given two additive categories, second write fails, expected partial report preserving first commit`() =
        runTest {
            // Given
            coEvery { writer.network(any(), any()) } throws IllegalStateException("storage failure")
            // When
            val result = useCase(request) {} as BackupRestoreResult.Finished
            // Then
            assertTrue(result.hasFailed)
            assertEquals(setOf(BackupCategory.GALLERY), result.completed)
            assertEquals(1L, result.importedCounts[BackupCategory.GALLERY])
            coVerify(exactly = 0) { ledger.finish(any()) }
        }

    @Test
    fun `given failed second category, user retries same session, expected only failed category applied`() =
        runTest {
            // Given
            coEvery { writer.network(any(), any()) } throws IllegalStateException("storage failure")
            useCase(request) {}
            coEvery { writer.network(any(), any()) } coAnswers {
                ledger.mark(
                    RestoreSectionEntity(
                        firstArg(),
                        "network",
                        1
                    )
                )
            }
            // When
            val result = useCase(request) {} as BackupRestoreResult.Finished
            // Then
            coVerify(exactly = 1) { writer.gallery(any(), any(), any()) }
            assertFalse(result.hasFailed)
            assertEquals(setOf(BackupCategory.GALLERY, BackupCategory.NETWORK), result.completed)
            assertEquals(2, committed.size)
        }

    @Test
    fun `given gallery only restore, protected auth storage is unreadable, expected no credential access`() =
        runTest {
            // Given
            coEvery { authorization.getAuthorizationCredentials() } throws IllegalStateException("unavailable")
            // When
            val result = useCase(request) {} as BackupRestoreResult.Finished
            // Then
            assertFalse(result.hasFailed)
            coVerify(exactly = 0) { authorization.getAuthorizationCredentials() }
        }

    @Test
    fun `given conflicting provider and gallery selected, no consent exists, expected no mutation in any category`() =
        runTest {
            // Given
            every { preferences.read() } returns BackupPreferences(
                mapOf(
                    "custom" to mapOf(
                        "serverUrl" to BackupValue.Text(
                            "current"
                        )
                    )
                ), emptyMap()
            )
            every { archives.preferences(archive) } returns BackupPreferences(
                mapOf(
                    "custom" to mapOf(
                        "serverUrl" to BackupValue.Text("incoming")
                    )
                ), emptyMap()
            )
            val selected = request.copy(
                selection = BackupSelection(
                    setOf(
                        BackupCategory.PROVIDERS,
                        BackupCategory.GALLERY
                    ), setOf("custom")
                )
            )
            // When
            val result = useCase(selected) {} as BackupRestoreResult.Conflict
            // Then
            assertEquals(setOf("custom"), result.providers)
            coVerify(exactly = 0) { ledger.insert(any()); writer.gallery(any(), any(), any()) }
        }

    @Test
    fun `given completed additive import, explicit new restore session starts, expected data added again`() =
        runTest {
            // Given
            useCase(request) {}
            val repeated = request.copy(sessionId = "33333333-3333-3333-3333-333333333333")
            // When
            val result = useCase(repeated) {} as BackupRestoreResult.Finished
            // Then
            coVerify(exactly = 2) { writer.gallery(any(), any(), any()) }
            coVerify(exactly = 2) { writer.network(any(), any()) }
            assertFalse(result.hasFailed)
            assertEquals(4, committed.size)
        }

    @Test
    fun `given pending provider batch, restore retries on same screen, expected exact batch recovered before auth read`() =
        runTest {
            // Given
            val selected = request.copy(
                selection = BackupSelection(
                    setOf(BackupCategory.PROVIDERS),
                    setOf("custom")
                )
            )
            // When
            useCase(selected) {}
            // Then
            coVerifyOrder { configuration.retryPendingConfiguration(); preferences.recoverPendingWrite(); authorization.getAuthorizationCredentials() }
        }

    @Test
    fun `given settings committed, next category is canceled, expected cancellation propagated and observers notified`() =
        runTest {
            // Given
            val selected = request.copy(
                selection = BackupSelection(
                    setOf(
                        BackupCategory.SETTINGS,
                        BackupCategory.GALLERY
                    )
                )
            )
            var wasCanceled = false
            // When
            try {
                useCase(selected) {
                    if (it.category == BackupCategory.GALLERY) throw CancellationException(
                        "user canceled"
                    )
                }
            } catch (_: CancellationException) {
                wasCanceled = true
            }
            // Then
            assertTrue(wasCanceled)
            coVerify { changes.notifyChanged(setOf(BackupCategory.SETTINGS)) }
            coVerify(exactly = 0) { writer.gallery(any(), any(), any()); ledger.finish(any()) }
        }

    @Test
    fun `given first launch final section committed, configuration navigation is not acknowledged, expected journal remains recoverable`() =
        runTest {
            // Given
            val selected = request.copy(shouldProceedToConfiguration = true)
            // When
            useCase(selected) {}
            // Then
            assertTrue(intents.getValue(request.sessionId).isComplete)
            assertTrue(intents.getValue(request.sessionId).requiresConfiguration)
        }

    @Test
    fun `given committed settings and primary cancellation, observer publication fails, expected original cancellation with suppressed notification failure`() =
        runTest {
            // Given
            val selected = request.copy(
                selection = BackupSelection(
                    setOf(
                        BackupCategory.SETTINGS,
                        BackupCategory.GALLERY
                    )
                )
            )
            val cancellation = CancellationException("cancel")
            val notificationFailure = IllegalStateException("notification failed")
            coEvery { changes.notifyChanged(any()) } throws notificationFailure
            var caught: Throwable? = null
            // When
            try {
                useCase(selected) { if (it.category == BackupCategory.GALLERY) throw cancellation }
            } catch (failure: Throwable) {
                caught = failure
            }
            // Then
            assertSame(cancellation, caught)
            assertEquals(
                listOf(notificationFailure.message),
                caught?.suppressedExceptions?.map { it.message })
        }

    @Test
    fun `given completed restore and no primary failure, observer publication fails, expected failure propagated and retry intent retained`() =
        runTest {
            // Given
            val notificationFailure = IllegalStateException("notification failed")
            coEvery { changes.notifyChanged(any()) } throws notificationFailure
            var caught: Throwable? = null
            // When
            try {
                useCase(request) {}
            } catch (failure: Throwable) {
                caught = failure
            }
            // Then
            assertEquals(notificationFailure.message, caught?.message)
            assertTrue(caught is IllegalStateException)
            coVerify(exactly = 0) { ledger.finish(request.sessionId) }
            assertFalse(intents.getValue(request.sessionId).isComplete)
        }

}
