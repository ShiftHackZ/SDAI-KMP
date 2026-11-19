package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.feature.backup.archive.StagedBackup
import com.shifthackz.aisdv1.feature.backup.codec.BackupArchiveCodec
import com.shifthackz.aisdv1.feature.backup.codec.BackupManifest
import com.shifthackz.aisdv1.feature.backup.codec.BackupSection
import com.shifthackz.aisdv1.feature.backup.codec.GalleryBackupCodec
import com.shifthackz.aisdv1.feature.backup.io.BackupFileStore
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomBackupRestoreWriterTest {
    private val database = mockk<BackupStorage>()
    private val ledger = mockk<RestoreSessionDao>(relaxed = true)
    private val codec = mockk<BackupArchiveCodec>()
    private val gallery = mockk<GalleryBackupCodec>()
    private val files = mockk<BackupFileStore> { every { newId() } returns "revision" }
    private val archive = StagedBackup(
        "handle",
        "/private/archive".toPath(),
        BackupManifest(
            "id",
            1,
            "1",
            "IOS",
            listOf(
                BackupSection(
                    "gallery",
                    storedBytes = 1,
                    decodedBytes = 1,
                    sha256 = "a".repeat(64),
                    itemCount = 1
                )
            )
        )
    )

    @Test
    fun `given gallery transaction, rows process then commit fails, expected no durable completion progress`() =
        runTest {
            // Given
            every { database.restoreSessionDao() } returns ledger
            coEvery { ledger.completed(any()) } returns emptyList()
            every { codec.openSection(any(), any(), any()) } answers { Buffer() }
            every { gallery.read(any(), any()) } returns null
            coEvery { database.writeTransaction<Unit>(any()) } coAnswers {
                firstArg<suspend () -> Unit>()()
                throw IllegalStateException("commit failed")
            }
            val writer = RoomBackupRestoreWriter(database, codec, gallery, files, Json)
            val progress = mutableListOf<BackupProgress>()
            var hasFailed = false
            // When
            try {
                writer.gallery("session", archive, progress::add)
            } catch (_: IllegalStateException) {
                hasFailed = true
            }
            // Then
            assertTrue(hasFailed)
            assertTrue(progress.isEmpty())
        }

    @Test
    fun `given gallery transaction, category commit completes, expected progress published after transaction returns`() =
        runTest {
            // Given
            every { database.restoreSessionDao() } returns ledger
            coEvery { ledger.completed(any()) } returns emptyList()
            every { codec.openSection(any(), any(), any()) } answers { Buffer() }
            every { gallery.read(any(), any()) } returns null
            var hasCommitted = false
            coEvery { database.writeTransaction<Unit>(any()) } coAnswers {
                firstArg<suspend () -> Unit>()()
                hasCommitted = true
            }
            val writer = RoomBackupRestoreWriter(database, codec, gallery, files, Json)
            val progress = mutableListOf<BackupProgress>()
            // When
            writer.gallery("session", archive) { assertTrue(hasCommitted); progress += it }
            // Then
            assertEquals(1, progress.size)
            assertEquals(0L, progress.single().completed)
        }
}
