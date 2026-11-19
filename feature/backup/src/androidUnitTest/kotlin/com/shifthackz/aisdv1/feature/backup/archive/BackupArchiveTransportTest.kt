package com.shifthackz.aisdv1.feature.backup.archive

import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import io.mockk.every
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BackupArchiveTransportTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private lateinit var fixture: BackupArchiveFixture

    @Before
    fun setup() {
        fixture = BackupArchiveFixture(temporary.newFolder("private").absolutePath.toPath())
    }

    @Test
    fun `given staged backup, destination is selected, expected exact archive bytes copied and source retained`() =
        runTest {
            // Given
            val export = fixture.store.create(BackupSelection(setOf(BackupCategory.SETTINGS))) {}
            val destination = temporary.newFile().toPath()
            // When
            fixture.store.export(export.document, "file://$destination")
            // Then
            assertTrue(
                destination.toFile().readBytes()
                    .contentEquals(fixture.files.documentPath(export.document).toFile().readBytes())
            )
            assertEquals(export.handle, fixture.store.staged(export.handle).handle)
        }

    @Test
    fun `given destination permission failure, export is requested, expected failure propagated and staged source retained`() =
        runTest {
            // Given
            val export = fixture.store.create(BackupSelection(setOf(BackupCategory.SETTINGS))) {}
            val failure = IllegalStateException("cannot write destination")
            every { fixture.files.writeDocument(any()) } throws failure
            // When
            val caught = runCatching {
                fixture.store.export(
                    export.document,
                    "destination"
                )
            }.exceptionOrNull()
            // Then
            assertSame(failure, caught)
            assertEquals(export.handle, fixture.store.staged(export.handle).handle)
        }

    @Test
    fun `given export destination cancellation, copy is requested, expected cancellation propagated and source retained`() =
        runTest {
            // Given
            val export = fixture.store.create(BackupSelection(setOf(BackupCategory.SETTINGS))) {}
            val cancellation = CancellationException("canceled")
            every { fixture.files.writeDocument(any()) } throws cancellation
            // When
            val failure = runCatching {
                fixture.store.export(
                    export.document,
                    "destination"
                )
            }.exceptionOrNull()
            // Then
            assertSame(cancellation, failure)
            assertTrue(FileSystem.SYSTEM.exists(fixture.files.documentPath(export.document)))
        }

    @Test
    fun `given archive outside app staging, export is requested, expected rejection without destination mutation`() =
        runTest {
            // Given
            val external = temporary.newFile("archive.sdaibackup").apply { writeText("external") }
            val destination = temporary.newFile().apply { writeText("untouched") }
            // When
            val failure = runCatching {
                fixture.store.export(
                    "file://${external.absolutePath}",
                    "file://${destination.absolutePath}"
                )
            }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            assertEquals("untouched", destination.readText())
            assertEquals("external", external.readText())
        }

    @Test
    fun `given staged backup URL, user discards operation, expected only private staging removed`() =
        runTest {
            // Given
            val export = fixture.store.create(BackupSelection(setOf(BackupCategory.SETTINGS))) {}
            val external = temporary.newFile("source").apply { writeText("keep") }
            // When
            fixture.store.discard(export.document)
            // Then
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
            assertEquals("keep", external.readText())
        }

    @Test
    fun `given invalid traversal handle, staging cleanup is requested, expected external files untouched`() {
        // Given
        val external = temporary.newFile("source").apply { writeText("keep") }
        // When
        val failure = runCatching { fixture.store.discard("../source") }.exceptionOrNull()
        // Then
        assertTrue(failure is IllegalArgumentException)
        assertEquals("keep", external.readText())
    }

    @Test
    fun `given missing staged archive, pending export is recovered, expected explicit failure`() {
        // Given
        val handle = "00000000-0000-0000-0000-000000000099"
        // When
        val failure =
            runCatching { fixture.store.preparedExport(handle, "backup") }.exceptionOrNull()
        // Then
        assertEquals("Pending backup source is unavailable", failure?.message)
    }
}
