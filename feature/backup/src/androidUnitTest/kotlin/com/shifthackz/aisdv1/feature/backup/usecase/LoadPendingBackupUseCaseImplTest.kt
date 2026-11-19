package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.gateway.backup.BackupChangesGateway
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.feature.backup.archive.BackupStagingCleaner
import com.shifthackz.aisdv1.feature.backup.restore.DefaultBackupSessionCodec
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSessionEntity
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadPendingBackupUseCaseImplTest {
    private val database = mockk<BackupStorage>()
    private val ledger = mockk<RestoreSessionDao>(relaxed = true)
    private val preferences = mockk<BackupPreferencesGateway>(relaxed = true)
    private val changes = mockk<BackupChangesGateway>(relaxed = true)
    private val archives = mockk<BackupArchiveStore>(relaxed = true)
    private val sessions = DefaultBackupSessionCodec(Json)
    private var stage = BackupStartupStage.RESTORE
    private var pending = RestoreSessionEntity(
        "session",
        "handle",
        sessions.encode(BackupSelection(setOf(BackupCategory.GALLERY))),
        requiresConfiguration = true
    )
    private val useCase = LoadPendingBackupUseCaseImpl(
        database,
        preferences,
        archives,
        sessions,
        mockk<ConfigurationStore>(relaxed = true),
        mockk<AuthorizationStore>(relaxed = true),
        ProviderSelectionGuard(),
        mockk<DispatchersProvider> { every { io } returns Dispatchers.Unconfined },
        mockk<BackupStagingCleaner>(relaxed = true),
        changes
    )

    @Before
    fun setup() {
        every { database.restoreSessionDao() } returns ledger
        coEvery { ledger.pending() } answers { if (pending.requiresConfiguration) listOf(pending) else emptyList() }
        coEvery { ledger.completed(any()) } returns listOf("gallery")
        coEvery { ledger.finish(any()) } answers { pending = pending.copy(isComplete = true) }
        coEvery { ledger.acknowledgeConfiguration(any()) } answers {
            pending = pending.copy(requiresConfiguration = false)
        }
        every { preferences.readStartupStage() } answers { stage }
        every { preferences.writeStartupStage(any()) } answers { stage = firstArg() }
    }

    @Test
    fun `given first launch last section committed before crash, session finish not recorded, expected configuration handoff without reimport`() =
        runTest {
            // Given
            assertFalse(pending.isComplete)
            // When
            val recovery = useCase()
            // Then
            assertNull(recovery)
            assertEquals(BackupStartupStage.CONFIGURATION, stage)
            assertTrue(pending.isComplete)
            assertFalse(pending.requiresConfiguration)
            coVerify(exactly = 0) { archives.staged(any()) }
            coVerify(exactly = 1) { archives.discard("handle") }
        }

    @Test
    fun `given completed first launch import, configuration write fails then startup retries, expected completion intent retained until checked acknowledgment`() =
        runTest {
            // Given
            pending = pending.copy(isComplete = true)
            every { preferences.writeStartupStage(any()) } throws IllegalStateException("checked write failed")
            var hasFailed = false
            // When
            try {
                useCase()
            } catch (_: IllegalStateException) {
                hasFailed = true
            }
            // Then
            assertTrue(hasFailed)
            assertTrue(pending.requiresConfiguration)
            coVerify(exactly = 0) { ledger.acknowledgeConfiguration(any()); archives.discard(any()) }
            every { preferences.writeStartupStage(any()) } answers { stage = firstArg() }
            assertNull(useCase())
            assertEquals(BackupStartupStage.CONFIGURATION, stage)
            assertFalse(pending.requiresConfiguration)
            coVerify(exactly = 0) { archives.staged(any()) }
        }

    @Test
    fun `given replayed settings write without ledger marker, pending recovery succeeds, expected retained readers refreshed after association recovery`() =
        runTest {
            // Given
            every { preferences.recoverPendingWrite() } returns setOf(BackupCategory.SETTINGS)
            // When
            useCase()
            // Then
            coVerify { changes.notifyChanged(setOf(BackupCategory.SETTINGS)) }
        }

    @Test
    fun `given setup already complete and restore handoff remains pending, startup recovers committed sections, expected completion acknowledged without stage downgrade`() =
        runTest {
            // Given
            stage = BackupStartupStage.COMPLETE
            pending = pending.copy(isComplete = true)
            // When
            val recovery = useCase()
            // Then
            assertNull(recovery)
            assertEquals(BackupStartupStage.COMPLETE, stage)
            assertFalse(pending.requiresConfiguration)
            coVerify(exactly = 0) { preferences.writeStartupStage(any()) }
        }

}
