package com.shifthackz.aisdv1.feature.backup.archive

import com.shifthackz.aisdv1.feature.backup.io.BackupFileStore
import com.shifthackz.aisdv1.domain.gateway.backup.BackupExportStateStore
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportIntent
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSessionEntity
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultBackupStagingCleanerTest {
    @get:Rule
    val temporary = TemporaryFolder()

    private val files = mockk<BackupFileStore>()
    private val database = mockk<BackupStorage>()
    private val exports = mockk<BackupExportStateStore>(relaxed = true)
    private val ledger = mockk<RestoreSessionDao>()
    private lateinit var root: Path
    private lateinit var cleaner: BackupStagingCleaner

    @Before
    fun setup() {
        root = temporary.newFolder("backup").absolutePath.toPath()
        every { files.root } returns root
        every { database.restoreSessionDao() } returns ledger
        coEvery { ledger.pending() } returns emptyList()
        every { exports.read() } returns null
        cleaner = DefaultBackupStagingCleaner(files, database, exports)
    }

    private fun staged(handle: String): Path {
        val directory = root / handle
        FileSystem.SYSTEM.createDirectories(directory)
        (directory / "archive.sdaibackup").toFile().writeText("private archive")
        return directory
    }

    @Test
    fun `given previous process staging, startup cleanup runs, expected only unreferenced generated directories removed`() =
        runTest {
            // Given
            val orphan = staged(ORPHAN)
            val retained = staged(RETAINED)
            val unknown = staged("unrelated-entry")
            val external = temporary.newFile("source.sdaibackup").apply { writeText("user source") }
            coEvery { ledger.pending() } returns listOf(
                RestoreSessionEntity(
                    "pending",
                    RETAINED,
                    "selection"
                )
            )

            // When
            cleaner.prepare()

            // Then
            assertFalse(FileSystem.SYSTEM.exists(orphan))
            assertTrue(FileSystem.SYSTEM.exists(retained / "archive.sdaibackup"))
            assertTrue(FileSystem.SYSTEM.exists(unknown / "archive.sdaibackup"))
            assertEquals("user source", external.readText())
        }

    @Test
    fun `given staging initialized, later active operation creates files, expected repeated recovery calls never rescan them`() =
        runTest {
            // Given
            cleaner.prepare()
            val active = staged(ORPHAN)

            // When
            cleaner.prepare()

            // Then
            assertTrue(FileSystem.SYSTEM.exists(active / "archive.sdaibackup"))
            coVerify(exactly = 1) { ledger.pending() }
        }

    @Test
    fun `given startup cleanup in progress, concurrent create awaits admission, expected new files created only after cleanup`() =
        runTest {
            // Given
            val old = staged(ORPHAN)
            val release = CompletableDeferred<Unit>()
            coEvery { ledger.pending() } coAnswers {
                release.await()
                emptyList()
            }
            val startup = async { cleaner.prepare() }
            runCurrent()
            var hasCreated = false

            // When
            val create = async {
                cleaner.prepare()
                staged(RETAINED)
                hasCreated = true
            }
            runCurrent()

            // Then
            assertFalse(hasCreated)
            release.complete(Unit)
            startup.await()
            create.await()
            assertFalse(FileSystem.SYSTEM.exists(old))
            assertTrue(FileSystem.SYSTEM.exists(root / RETAINED / "archive.sdaibackup"))
            coVerify(exactly = 1) { ledger.pending() }
        }

    @Test
    fun `given orphan directory contains external symlink, startup cleanup runs, expected linked user source remains untouched`() =
        runTest {
            // Given
            val orphan = staged(ORPHAN)
            val external = temporary.newFolder("user-model").apply {
                resolve("model.bin").writeText("original model")
            }
            Files.createSymbolicLink((orphan / "linked-model").toFile().toPath(), external.toPath())

            // When
            cleaner.prepare()

            // Then
            assertFalse(FileSystem.SYSTEM.exists(orphan))
            assertEquals("original model", external.resolve("model.bin").readText())
        }

    @Test
    fun `given staging root is a symbolic link, startup cleanup runs, expected rejection without external deletion`() =
        runTest {
            // Given
            val external = temporary.newFolder("outside")
            val outsideArchive =
                external.resolve(ORPHAN).apply { mkdir(); resolve("archive").writeText("keep") }
            val link = temporary.root.resolve("linked-backup")
            Files.createSymbolicLink(link.toPath(), external.toPath())
            every { files.root } returns link.absolutePath.toPath()

            // When
            val failure = try {
                cleaner.prepare()
                null
            } catch (error: IllegalStateException) {
                error
            }

            // Then
            assertTrue(failure != null)
            assertEquals("keep", outsideArchive.resolve("archive").readText())
        }

    @Test
    fun `given restore ledger is unreadable, startup cleanup fails then retries, expected files retained until ownership can be read`() =
        runTest {
            // Given
            val orphan = staged(ORPHAN)
            coEvery { ledger.pending() } throws IllegalStateException("ledger unavailable")

            // When
            val failure = try {
                cleaner.prepare()
                null
            } catch (error: IllegalStateException) {
                error
            }

            // Then
            assertTrue(failure != null)
            assertTrue(FileSystem.SYSTEM.exists(orphan))
            coEvery { ledger.pending() } returns emptyList()
            cleaner.prepare()
            assertFalse(FileSystem.SYSTEM.exists(orphan))
            coVerify(exactly = 2) { ledger.pending() }
        }

    @Test
    fun `given native destination picker owns export, startup cleanup runs, expected pending export source retained`() =
        runTest {
            // Given
            val retained = staged(RETAINED)
            val orphan = staged(ORPHAN)
            every { exports.read() } returns BackupExportIntent(RETAINED, "backup.sdaibackup")
            // When
            cleaner.prepare()
            // Then
            assertTrue(FileSystem.SYSTEM.exists(retained / "archive.sdaibackup"))
            assertFalse(FileSystem.SYSTEM.exists(orphan))
        }

    private companion object {
        const val ORPHAN = "11111111-1111-1111-1111-111111111111"
        const val RETAINED = "22222222-2222-2222-2222-222222222222"
    }
}
