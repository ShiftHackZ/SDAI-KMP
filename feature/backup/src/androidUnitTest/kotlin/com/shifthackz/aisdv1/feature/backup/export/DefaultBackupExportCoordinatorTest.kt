package com.shifthackz.aisdv1.feature.backup.export

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportCompletion
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportIntent
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportStatus
import com.shifthackz.aisdv1.domain.gateway.backup.BackupExportStateStore
import com.shifthackz.aisdv1.domain.usecase.backup.ExportBackupUseCase
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultBackupExportCoordinatorTest {
    private val archives = mockk<BackupArchiveStore>(relaxed = true)
    private val records = mockk<BackupExportStateStore>(relaxed = true)
    private val export = mockk<ExportBackupUseCase>()
    private val backup = BackupExport(
        "file:///private/staged",
        "backup.sdaibackup",
        "11111111-1111-1111-1111-111111111111"
    )
    private var stored: BackupExportIntent? = null

    private fun TestScope.coordinator(): DefaultBackupExportCoordinator {
        every { records.read() } answers { stored }
        every { records.write(any()) } answers { stored = firstArg() }
        every { records.clear() } answers { stored = null }
        every { archives.preparedExport(backup.handle, backup.filename) } returns backup
        val dispatcher = StandardTestDispatcher(testScheduler)
        return DefaultBackupExportCoordinator(
            archives,
            records,
            export,
            mockk<DispatchersProvider> { every { io } returns dispatcher })
    }

    @Test
    fun `given recreated host, native callback arrives before async recovery, expected complete destination copy bound to staged source`() =
        runTest {
            // Given
            stored = BackupExportIntent(backup.handle, backup.filename)
            coEvery { export(backup.document, "destination") } returns Unit
            val owner = coordinator()
            // When
            owner.submit(BackupExportCompletion.DestinationSelected("destination", backup.handle))
            advanceUntilIdle()
            // Then
            assertEquals(BackupExportStatus.COMPLETE, stored?.status)
            assertEquals(backup, owner.observe().value.backup)
            coVerify(exactly = 1) { export(backup.document, "destination") }
            verify(exactly = 0) { archives.discard(any()) }
            owner.close()
        }

    @Test
    fun `given interrupted destination copy, graph restarts with durable destination, expected idempotent copy resumed and completion recorded`() =
        runTest {
            // Given
            stored = BackupExportIntent(
                backup.handle,
                backup.filename,
                BackupExportStatus.COPYING,
                "destination"
            )
            coEvery { export(backup.document, "destination") } returns Unit
            // When
            val owner = coordinator()
            advanceUntilIdle()
            // Then
            assertEquals(BackupExportStatus.COMPLETE, stored?.status)
            coVerify(exactly = 1) { export(backup.document, "destination") }
            owner.close()
        }

    @Test
    fun `given pending source, destination copy fails, expected source retained and explicit failure without success`() =
        runTest {
            // Given
            stored = BackupExportIntent(backup.handle, backup.filename)
            coEvery { export(any(), any()) } throws IllegalStateException("write failed")
            val owner = coordinator()
            // When
            owner.submit(BackupExportCompletion.DestinationSelected("destination", backup.handle))
            advanceUntilIdle()
            // Then
            assertEquals(BackupExportStatus.FAILED, stored?.status)
            assertEquals(BackupExportStatus.FAILED, owner.observe().value.status)
            assertEquals(backup, owner.observe().value.backup)
            verify(exactly = 0) { archives.discard(any()); records.clear() }
            owner.close()
        }

    @Test
    fun `given new export owns staging, old native cancellation arrives, expected newer source and association retained`() =
        runTest {
            // Given
            stored = BackupExportIntent(backup.handle, backup.filename)
            val owner = coordinator()
            // When
            owner.submit(BackupExportCompletion.Canceled("old-export"))
            advanceUntilIdle()
            // Then
            assertEquals(backup.handle, stored?.handle)
            assertEquals(BackupExportStatus.READY, owner.observe().value.status)
            verify(exactly = 0) { records.clear(); archives.discard(any()) }
            owner.close()
        }

    @Test
    fun `given native cancellation, result is queued before another prepare, expected canceled source acknowledged before new source created`() =
        runTest {
            // Given
            stored = BackupExportIntent(backup.handle, backup.filename)
            val owner = coordinator()
            advanceUntilIdle()
            // When
            owner.submit(BackupExportCompletion.Canceled(backup.handle))
            // Then
            assertEquals(BackupExportStatus.PROCESSING, owner.observe().value.status)
            advanceUntilIdle()
            assertNull(stored)
            assertEquals(BackupExportStatus.NONE, owner.observe().value.status)
            verify(exactly = 1) { archives.discard(backup.handle) }
            owner.close()
        }

    @Test
    fun `given active export, screen is disposed while copying, expected graph owns completion and only explicit cancel interrupts copy`() =
        runTest {
            // Given
            stored = BackupExportIntent(backup.handle, backup.filename)
            val finish = CompletableDeferred<Unit>()
            coEvery { export(any(), any()) } coAnswers { finish.await() }
            val owner = coordinator()
            owner.submit(BackupExportCompletion.DestinationSelected("destination", backup.handle))
            runCurrent()
            // When
            owner.cancel()
            advanceUntilIdle()
            // Then
            assertEquals(BackupExportStatus.CANCELED, stored?.status)
            assertEquals(backup, owner.observe().value.backup)
            verify(exactly = 0) { archives.discard(any()) }
            owner.close()
        }

    @Test
    fun `given missing export association, native destination result arrives, expected explicit failure without empty saved success`() =
        runTest {
            // Given
            val owner = coordinator()
            // When
            owner.submit(
                BackupExportCompletion.DestinationSelected(
                    "destination",
                    "missing-export"
                )
            )
            advanceUntilIdle()
            // Then
            assertEquals(BackupExportStatus.FAILED, owner.observe().value.status)
            coVerify(exactly = 0) { export(any(), any()) }
            owner.close()
        }

    @Test
    fun `given complete export record recovered after failed checked write, fresh picker chooses new destination, expected retained bytes copied again`() =
        runTest {
            // Given
            stored = BackupExportIntent(
                backup.handle,
                backup.filename,
                BackupExportStatus.COMPLETE,
                "old-destination"
            )
            coEvery { export(any(), any()) } returns Unit
            val owner = coordinator()
            // When
            owner.submit(
                BackupExportCompletion.DestinationSelected(
                    "new-destination",
                    backup.handle
                )
            )
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) { export(backup.document, "new-destination") }
            assertEquals(BackupExportStatus.COMPLETE, stored?.status)
            assertEquals("new-destination", stored?.destination)
            owner.close()
        }

    @Test
    fun `given previously completed export, fresh picker truncates same URI, expected exact source bytes copied again`() =
        runTest {
            // Given
            stored = BackupExportIntent(
                backup.handle,
                backup.filename,
                BackupExportStatus.COMPLETE,
                "destination"
            )
            coEvery { export(any(), any()) } returns Unit
            val owner = coordinator()
            // When
            owner.submit(BackupExportCompletion.DestinationSelected("destination", backup.handle))
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) { export(backup.document, "destination") }
            assertEquals(BackupExportStatus.COMPLETE, stored?.status)
            owner.close()
        }

}
