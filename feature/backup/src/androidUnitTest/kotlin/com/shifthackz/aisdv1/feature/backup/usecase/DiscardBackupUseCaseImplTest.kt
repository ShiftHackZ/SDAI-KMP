package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.gateway.backup.BackupChangesGateway
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.feature.backup.restore.DefaultBackupRestoreGuard
import com.shifthackz.aisdv1.feature.backup.restore.DefaultBackupSessionCodec
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSessionEntity
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscardBackupUseCaseImplTest {
    private val archives = mockk<BackupArchiveStore>(relaxed = true)
    private val database = mockk<BackupStorage>()
    private val ledger = mockk<RestoreSessionDao>(relaxed = true)
    private val preferences = mockk<BackupPreferencesGateway>(relaxed = true)
    private val configuration = mockk<ConfigurationStore>(relaxed = true)
    private val authorization = mockk<AuthorizationStore>(relaxed = true)
    private val changes = mockk<BackupChangesGateway>(relaxed = true)
    private val sessions = DefaultBackupSessionCodec(Json)
    private val useCase = DiscardBackupUseCaseImpl(
        archives,
        database,
        preferences,
        configuration,
        authorization,
        ProviderSelectionGuard(),
        DefaultBackupRestoreGuard(),
        mockk<DispatchersProvider> { every { io } returns Dispatchers.Unconfined },
        sessions,
        changes
    )

    private fun pending(category: BackupCategory) {
        every { database.restoreSessionDao() } returns ledger
        coEvery { ledger.pending() } returns listOf(
            RestoreSessionEntity(
                "session",
                "handle",
                sessions.encode(BackupSelection(setOf(category)))
            )
        )
    }

    @Test
    fun `given quarantined settings without ledger marker, user abandons restore, expected recovered settings published before abandonment completes`() =
        runTest {
            // Given
            pending(BackupCategory.SETTINGS)
            every { preferences.recoverPendingWrite() } returns setOf(BackupCategory.SETTINGS)
            var hasReconciled = false
            var hasNotified = false
            coEvery { authorization.getAuthorizationCredentials() } coAnswers {
                hasReconciled = true; AuthorizationCredentials.None
            }
            coEvery { changes.notifyChanged(setOf(BackupCategory.SETTINGS)) } coAnswers {
                assertTrue(
                    hasReconciled
                ); hasNotified = true
            }
            coEvery { ledger.finish("session") } coAnswers { assertTrue(hasNotified) }
            // When
            useCase("handle")
            // Then
            coVerifyOrder {
                preferences.recoverPendingWrite(); authorization.getAuthorizationCredentials(); changes.notifyChanged(
                setOf(BackupCategory.SETTINGS)
            ); ledger.finish("session"); archives.discard("handle")
            }
        }

    @Test
    fun `given provider association replay without ordinary field changes, user abandons restore, expected provider readers refreshed`() =
        runTest {
            // Given
            pending(BackupCategory.GALLERY)
            every { preferences.recoverPendingWrite() } returns setOf(BackupCategory.PROVIDERS)
            // When
            useCase("handle")
            // Then
            coVerify { changes.notifyChanged(setOf(BackupCategory.PROVIDERS)) }
        }

    @Test
    fun `given previously recovered write without marker, notification fails during abandonment, expected recoverable journal and source retained`() =
        runTest {
            // Given
            pending(BackupCategory.SETTINGS)
            val failure = IllegalStateException("notify failed")
            coEvery { changes.notifyChanged(any()) } throws failure
            var caught: Throwable? = null
            // When
            try {
                useCase("handle")
            } catch (error: Throwable) {
                caught = error
            }
            // Then
            assertEquals(failure.message, caught?.message)
            assertTrue(caught is IllegalStateException)
            coVerify(exactly = 0) { ledger.finish(any()); archives.discard(any()) }
        }
}
