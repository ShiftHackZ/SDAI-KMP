package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.feature.backup.archive.StagedBackup
import com.shifthackz.aisdv1.feature.backup.codec.BackupManifest
import com.shifthackz.aisdv1.feature.backup.codec.BackupSection
import com.shifthackz.aisdv1.feature.backup.codec.BinaryBackupArchiveCodec
import com.shifthackz.aisdv1.feature.backup.codec.DefaultCivitaiBackupCodec
import com.shifthackz.aisdv1.storage.db.persistent.dao.CivitaiUploadDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSectionEntity
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CivitaiBackupRestoreWriterTest {
    private val database = mockk<BackupStorage>()
    private val receipts = mockk<CivitaiUploadDao>()
    private val ledger = mockk<RestoreSessionDao>(relaxed = true)
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
    private val codec = spyk(BinaryBackupArchiveCodec(json))
    private val publicCodec = DefaultCivitaiBackupCodec(codec, json)
    private val row = CivitaiUploadEntity(
        91,
        12,
        4,
        "https://civitai.com/posts/123",
        200,
        100,
        "source-revision",
        "PUBLISHED"
    )
    private val source = Buffer()
    private val archive = StagedBackup(
        "archive", "/private/archive".toPath(),
        BackupManifest(
            "id",
            1,
            "1",
            "IOS",
            listOf(
                BackupSection(
                    "civitai",
                    storedBytes = 1,
                    decodedBytes = 1,
                    sha256 = "a".repeat(64),
                    itemCount = 1
                )
            )
        )
    )
    private val writer = RoomBackupRestoreWriter(
        database, codec, mockk(), mockk(), json,
        DefaultBackupImageFingerprint(), BackupImageBudget(1024, 100, 4096, 16), publicCodec
    )

    @Before
    fun setup() {
        every { database.restoreSessionDao() } returns ledger
        every { database.civitaiUploadDao() } returns receipts
        coEvery { ledger.completed(any()) } returns emptyList()
        coEvery { database.writeTransaction<Unit>(any()) } coAnswers { firstArg<suspend () -> Unit>()() }
        coEvery { receipts.find(any(), any(), any(), any()) } returns null
        coEvery { receipts.insert(any()) } returns 400
        every { codec.openSection(any(), any(), "civitai") } answers { source }
        publicCodec.write(source, row)
    }

    @Test
    fun `given new history receipt, restore completes, expected additive insert and atomic ledger then progress`() =
        runTest {
            // Given
            val progress = mutableListOf<BackupProgress>()
            // When
            writer.civitai("session", archive, progress::add)
            // Then
            coVerify { receipts.insert(row.copy(id = 0)) }
            coVerify { ledger.mark(RestoreSectionEntity("session", "civitai", 1, 0)) }
            assertEquals(1L, progress.single().completed)
        }

    @Test
    fun `given an existing receipt, history is restored, expected current publication retained and duplicate counted`() =
        runTest {
            // Given
            coEvery { receipts.find(12, 100, "source-revision", 4) } returns row.copy(id = 77)
            // When
            writer.civitai("session", archive) {}
            // Then
            coVerify(exactly = 0) { receipts.insert(any()) }
            coVerify { ledger.mark(RestoreSectionEntity("session", "civitai", 0, 1)) }
        }

    @Test
    fun `given committed history category, restore is retried, expected no inserts or duplicate progress`() =
        runTest {
            // Given
            coEvery { ledger.completed("session") } returns listOf("civitai")
            val progress = mutableListOf<BackupProgress>()
            // When
            writer.civitai("session", archive, progress::add)
            // Then
            coVerify(exactly = 0) { receipts.insert(any()) }
            assertTrue(progress.isEmpty())
        }

    @Test
    fun `given history commit failure, restore returns, expected failure and no acknowledged progress`() =
        runTest {
            // Given
            val error = IllegalStateException("commit failed")
            coEvery { database.writeTransaction<Unit>(any()) } coAnswers { firstArg<suspend () -> Unit>()(); throw error }
            val progress = mutableListOf<BackupProgress>()
            // When
            val failure =
                runCatching { writer.civitai("session", archive, progress::add) }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            assertTrue(progress.isEmpty())
        }

    @Test
    fun `given cancellation during receipt insert, restore is cancelled, expected cancellation propagated without category marker`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancel")
            coEvery { receipts.insert(any()) } throws cancellation
            // When
            val failure = runCatching { writer.civitai("session", archive) {} }.exceptionOrNull()
            // Then
            assertSame(cancellation, failure)
            coVerify(exactly = 0) { ledger.mark(any()) }
        }

    @Test
    fun `given undeclared extra history bytes, restore reads section, expected rejection before ledger completion`() =
        runTest {
            // Given
            source.writeByte(1)
            // When
            val failure = runCatching { writer.civitai("session", archive) {} }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { ledger.mark(any()) }
        }
}
