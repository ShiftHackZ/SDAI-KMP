package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.feature.backup.archive.StagedBackup
import com.shifthackz.aisdv1.feature.backup.codec.BackupArchiveCodec
import com.shifthackz.aisdv1.feature.backup.codec.BackupManifest
import com.shifthackz.aisdv1.feature.backup.codec.BackupSection
import com.shifthackz.aisdv1.feature.backup.codec.GalleryBackupCodec
import com.shifthackz.aisdv1.feature.backup.io.BackupFileStore
import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSectionEntity
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import io.mockk.coEvery
import io.mockk.coVerify
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
    private val budget = BackupImageBudget(24 * 1024 * 1024, 16_777_216L, 128L * 1024 * 1024, 256)
    private val database = mockk<BackupStorage>()
    private val ledger = mockk<RestoreSessionDao>(relaxed = true)
    private val codec = mockk<BackupArchiveCodec>()
    private val gallery = mockk<GalleryBackupCodec>()
    private val files = mockk<BackupFileStore> { every { newId() } returns "revision" }
    private val savedImages = mockk<GenerationResultDao> {
        coEvery { queryImageIdsAfter(any(), any()) } returns emptyList()
    }
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
            every { database.generationResultDao() } returns savedImages
            coEvery { ledger.completed(any()) } returns emptyList()
            every { codec.openSection(any(), any(), any()) } answers { Buffer() }
            every { gallery.read(any(), any()) } returns null
            coEvery { database.writeTransaction<Unit>(any()) } coAnswers {
                firstArg<suspend () -> Unit>()()
                throw IllegalStateException("commit failed")
            }
            val writer = RoomBackupRestoreWriter(
                database,
                codec,
                gallery,
                files,
                Json,
                DefaultBackupImageFingerprint(),
                budget,
                mockk(),
            )
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
            every { database.generationResultDao() } returns savedImages
            coEvery { ledger.completed(any()) } returns emptyList()
            every { codec.openSection(any(), any(), any()) } answers { Buffer() }
            every { gallery.read(any(), any()) } returns null
            var hasCommitted = false
            coEvery { database.writeTransaction<Unit>(any()) } coAnswers {
                firstArg<suspend () -> Unit>()()
                hasCommitted = true
            }
            val writer = RoomBackupRestoreWriter(
                database,
                codec,
                gallery,
                files,
                Json,
                DefaultBackupImageFingerprint(),
                budget,
                mockk(),
            )
            val progress = mutableListOf<BackupProgress>()
            // When
            writer.gallery("session", archive) { assertTrue(hasCommitted); progress += it }
            // Then
            assertEquals(1, progress.size)
            assertEquals(0L, progress.single().completed)
        }

    @Test
    fun `given existing hidden gallery image, backup has same bytes with different metadata, expected existing row preserved and import skipped`() =
        runTest {
            // Given
            val existing = row("AQIDBA==", id = 71, isHidden = true)
            val incoming =
                existing.copy(id = 0, prompt = "different prompt", liked = false, hidden = false)
            val rows = mutableListOf(existing)
            val outcomes = mutableListOf<RestoreSectionEntity>()
            configureGallery(rows, outcomes)
            every { gallery.read(any(), any()) } returns incoming
            val writer = RoomBackupRestoreWriter(
                database,
                codec,
                gallery,
                files,
                Json,
                DefaultBackupImageFingerprint(),
                budget,
                mockk(),
            )

            // When
            writer.gallery("fresh-session", archive) {}

            // Then
            assertEquals(listOf(existing), rows)
            assertEquals(0L, outcomes.single().importedCount)
            assertEquals(1L, outcomes.single().skippedCount)
            coVerify(exactly = 0) { savedImages.insert(any()) }
        }

    @Test
    fun `given gallery backup restored once, same backup is restored as new session, expected gallery size unchanged`() =
        runTest {
            // Given
            val rows = mutableListOf<GenerationResultEntity>()
            val outcomes = mutableListOf<RestoreSectionEntity>()
            configureGallery(rows, outcomes)
            every { gallery.read(any(), any()) } answers { row("AQIDBA==") }
            val writer = RoomBackupRestoreWriter(
                database,
                codec,
                gallery,
                files,
                Json,
                DefaultBackupImageFingerprint(),
                budget,
                mockk(),
            )
            writer.gallery("first-session", archive) {}

            // When
            writer.gallery("second-session", archive) {}

            // Then
            assertEquals(1, rows.size)
            assertEquals(listOf(1L, 0L), outcomes.map { it.importedCount })
            assertEquals(listOf(0L, 1L), outcomes.map { it.skippedCount })
            coVerify(exactly = 1) { savedImages.insert(any()) }
        }

    @Test
    fun `given archive contains duplicate outputs, restore reaches later copy, expected one fresh gallery row and one skipped entry`() =
        runTest {
            // Given
            val rows = mutableListOf<GenerationResultEntity>()
            val outcomes = mutableListOf<RestoreSectionEntity>()
            configureGallery(rows, outcomes)
            every { gallery.read(any(), any()) } returnsMany listOf(
                row("AQIDBA=="),
                row("AQIDBA==").copy(prompt = "another metadata row")
            )
            val doubled =
                archive.copy(manifest = archive.manifest.copy(sections = archive.manifest.sections.map {
                    it.copy(itemCount = 2)
                }))
            val writer = RoomBackupRestoreWriter(
                database,
                codec,
                gallery,
                files,
                Json,
                DefaultBackupImageFingerprint(),
                budget,
                mockk(),
            )

            // When
            writer.gallery("session", doubled) {}

            // Then
            assertEquals(1, rows.size)
            assertEquals(1L, outcomes.single().importedCount)
            assertEquals(1L, outcomes.single().skippedCount)
        }

    @Test
    fun `given existing gallery, backup contains distinct output bytes, expected new row added beside original`() =
        runTest {
            // Given
            val existing = row("AQIDBA==", id = 71)
            val rows = mutableListOf(existing)
            val outcomes = mutableListOf<RestoreSectionEntity>()
            configureGallery(rows, outcomes)
            every { gallery.read(any(), any()) } returns row("AQIDBQ==")
            val writer = RoomBackupRestoreWriter(
                database,
                codec,
                gallery,
                files,
                Json,
                DefaultBackupImageFingerprint(),
                budget,
                mockk(),
            )

            // When
            writer.gallery("session", archive) {}

            // Then
            assertEquals(2, rows.size)
            assertEquals(existing, rows.first())
            assertEquals(1L, outcomes.single().importedCount)
            assertEquals(0L, outcomes.single().skippedCount)
        }

    @Test
    fun `given oversized wrapped Base64 representation, backup contains same admissible image, expected fallback detects duplicate`() =
        runTest {
            // Given
            val rows = mutableListOf(row("AQID\nBA==", id = 71))
            val outcomes = mutableListOf<RestoreSectionEntity>()
            configureGallery(rows, outcomes)
            every { gallery.read(any(), any()) } returns row("AQIDBA==")
            val writer = RoomBackupRestoreWriter(
                database, codec, gallery, files, Json,
                DefaultBackupImageFingerprint(), budget.copy(maxEncodedBytes = 4), mockk()
            )

            // When
            writer.gallery("session", archive) {}

            // Then
            assertEquals(1, rows.size)
            assertEquals(0L, outcomes.single().importedCount)
            assertEquals(1L, outcomes.single().skippedCount)
            coVerify(atLeast = 1) { savedImages.queryImageChunk(71, any(), any()) }
        }

    @Test
    fun `given gallery spans multiple identity pages, duplicate exists after first page, expected image skipped without altering stored rows`() =
        runTest {
            // Given
            val rows =
                (1L..130L).map { id -> row(if (id == 130L) "AQIDBA==" else "AQIDBQ==", id = id) }
                    .toMutableList()
            val original = rows.toList()
            val outcomes = mutableListOf<RestoreSectionEntity>()
            configureGallery(rows, outcomes)
            every { gallery.read(any(), any()) } returns row("AQIDBA==")
            val writer = RoomBackupRestoreWriter(
                database,
                codec,
                gallery,
                files,
                Json,
                DefaultBackupImageFingerprint(),
                budget,
                mockk(),
            )
            // When
            writer.gallery("session", archive) {}
            // Then
            assertEquals(original, rows)
            assertEquals(1L, outcomes.single().skippedCount)
            assertEquals(0L, outcomes.single().importedCount)
            coVerify { savedImages.queryImageIdsAfter(128, 128) }
            coVerify(exactly = 0) { savedImages.insert(any()) }
        }

    @Test
    fun `given gallery category already committed, session retries, expected no gallery scan or duplicate insertion`() =
        runTest {
            // Given
            val rows = mutableListOf(row("AQIDBA==", id = 71))
            val outcomes = mutableListOf<RestoreSectionEntity>()
            configureGallery(rows, outcomes)
            coEvery { ledger.completed("session") } returns listOf("gallery")
            val writer = RoomBackupRestoreWriter(
                database,
                codec,
                gallery,
                files,
                Json,
                DefaultBackupImageFingerprint(),
                budget,
                mockk(),
            )
            val progress = mutableListOf<BackupProgress>()
            // When
            writer.gallery("session", archive, progress::add)
            // Then
            assertEquals(1, rows.size)
            assertTrue(outcomes.isEmpty())
            assertTrue(progress.isEmpty())
            coVerify(exactly = 0) {
                savedImages.queryImageIdsAfter(
                    any(),
                    any()
                ); savedImages.insert(any()); ledger.mark(any())
            }
        }

    /**
     * Supplies the production writer with a paged image store and an observed commit ledger.
     *
     * @param rows Mutable persisted rows; fresh insert identities are assigned independently of the archive.
     * @param outcomes Committed category reports recorded by the fake transaction boundary.
     * @author Dmitriy Moroz
     */
    private fun configureGallery(
        rows: MutableList<GenerationResultEntity>,
        outcomes: MutableList<RestoreSectionEntity>
    ) {
        every { database.restoreSessionDao() } returns ledger
        every { database.generationResultDao() } returns savedImages
        coEvery { ledger.completed(any()) } returns emptyList()
        coEvery { ledger.mark(any()) } answers { outcomes += firstArg<RestoreSectionEntity>() }
        every { codec.openSection(any(), any(), any()) } answers { Buffer() }
        coEvery { database.writeTransaction<Unit>(any()) } coAnswers { firstArg<suspend () -> Unit>()() }
        coEvery { savedImages.queryImageIdsAfter(any(), any()) } answers {
            val afterId = firstArg<Long>()
            rows.filter { it.id > afterId }.sortedBy { it.id }.take(secondArg()).map { it.id }
        }
        coEvery { savedImages.queryImageTextForFingerprint(any(), any()) } answers {
            rows.single { it.id == firstArg<Long>() }.imageBase64.takeIf { it.encodeToByteArray().size <= secondArg<Long>() }
        }
        coEvery { savedImages.queryImageChunk(any(), any(), any()) } answers {
            val text = rows.single { it.id == firstArg<Long>() }.imageBase64.encodeToByteArray()
            val start = secondArg<Long>().toInt() - 1
            if (start >= text.size) byteArrayOf() else text.copyOfRange(
                start,
                minOf(text.size, start + thirdArg<Int>())
            )
        }
        coEvery { savedImages.insert(any()) } answers {
            rows += firstArg<GenerationResultEntity>().copy(id = (rows.maxOfOrNull { it.id }
                ?: 0) + 1)
            rows.last().id
        }
    }

    /**
     * Builds gallery rows with different output-byte identities while keeping generation metadata usable.
     *
     * @param image Output Base64 content used by the duplicate-checking unit under test.
     * @param id Existing identity, or zero for an archive-decoded row.
     * @param isHidden Whether the existing image belongs only to generation history.
     * @author Dmitriy Moroz
     */
    private fun row(image: String, id: Long = 0, isHidden: Boolean = false) =
        GenerationResultEntity(
            id, image, "", 100, "txt2img", "prompt", "negative", 1, 1, 20, 7.5f,
            false, "Euler", "42", "9", 0.25f, 0.5f, isHidden, true, "model", "revision",
        )

}
