package com.shifthackz.aisdv1.feature.backup.archive

import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import io.mockk.every
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BackupArchiveInspectionTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private lateinit var fixture: BackupArchiveFixture

    @Before
    fun setup() {
        fixture = BackupArchiveFixture(temporary.newFolder("private").absolutePath.toPath())
    }

    @Test
    fun `given selected backup, inspection completes, expected private immutable copy and source details retained`() =
        runTest {
            // Given
            val source =
                fixture.document("settings", fixture.fields.encode(fixture.snapshot.settings))
            val progress = mutableListOf<Long>()
            // When
            val inspection = fixture.store.inspect(source) { progress += it.completed }
            val staged = fixture.store.staged(inspection.handle)
            // Then
            assertEquals("selected.sdaibackup", inspection.filename)
            assertEquals(fixture.location, inspection.sourceLocation)
            assertEquals(
                setOf(BackupCategory.SETTINGS),
                inspection.entries.filter { it.isAvailable }.map { it.category }.toSet()
            )
            assertTrue(
                staged.path.toFile().readBytes()
                    .contentEquals(source.removePrefix("file://").toPath().toFile().readBytes())
            )
            assertTrue(progress.last() > 0)
            assertEquals(0, inspection.skippedCount)
        }

    @Test
    fun `given long document labels, backup is selected, expected bounded persisted name and location`() =
        runTest {
            // Given
            fixture.displayName = "n".repeat(600)
            fixture.location = "p".repeat(3000)
            val source = fixture.document("settings", "{}")
            // When
            val inspection = fixture.store.inspect(source) {}
            // Then
            assertEquals(512, inspection.filename.length)
            assertEquals(2048, inspection.sourceLocation.length)
        }

    @Test
    fun `given old staged archive without display metadata, summary is recovered, expected safe fallback labels`() =
        runTest {
            // Given
            val export = fixture.store.create(BackupSelection(setOf(BackupCategory.SETTINGS))) {}
            // When
            val inspection = fixture.store.summary(fixture.store.staged(export.handle))
            // Then
            assertEquals("SDAI.sdaibackup", inspection.filename)
            assertEquals("", inspection.sourceLocation)
        }

    @Test
    fun `given newer unknown section, compatible container is inspected, expected section skipped without decoding`() =
        runTest {
            // Given
            val source = fixture.document("future_feature", "not JSON")
            // When
            val inspection = fixture.store.inspect(source) {}
            // Then
            assertTrue(inspection.entries.isEmpty())
            assertEquals(1, inspection.skippedCount)
        }

    @Test
    fun `given gallery requires newer reader, archive is inspected, expected unavailable gallery without reading unsupported payload`() =
        runTest {
            // Given
            val source =
                fixture.document("gallery", "future bytes") { it.copy(requiredReaderVersion = 2) }
            // When
            val inspection = fixture.store.inspect(source) {}
            // Then
            assertFalse(inspection.entries.single().isAvailable)
            assertEquals(1, inspection.skippedCount)
        }

    @Test
    fun `given network uses future codec, archive is inspected, expected unavailable section without decoding`() =
        runTest {
            // Given
            val source = fixture.document("network", "future bytes") { it.copy(codec = "future") }
            // When
            val inspection = fixture.store.inspect(source) {}
            // Then
            assertFalse(inspection.entries.single().isAvailable)
            assertEquals(1, inspection.skippedCount)
        }

    @Test
    fun `given removed provider and field, compatible fields are filtered, expected supported values and skipped count`() =
        runTest {
            // Given
            val source = fixture.document(
                "providers",
                fixture.fields.encodeProviders(
                    mapOf(
                        "custom" to mapOf(
                            "serverUrl" to BackupValue.Text(
                                "host"
                            ), "removed" to BackupValue.Text("old")
                        ), "future" to mapOf("token" to BackupValue.Text("old"))
                    )
                )
            )
            every { fixture.preferences.filterSupported(any()) } answers {
                val value = firstArg<BackupPreferences>()
                value.copy(providers = value.providers.mapValues { (_, fields) -> fields.filterKeys { it == "serverUrl" } })
            }
            // When
            val inspection = fixture.store.inspect(source) {}
            // Then
            assertEquals(setOf("custom"), inspection.providers)
            assertEquals(2, inspection.skippedCount)
        }

    @Test
    fun `given unsupported setting, compatibility filter removes it, expected skipped item and disabled empty category`() =
        runTest {
            // Given
            val source = fixture.document(
                "settings",
                fixture.fields.encode(mapOf("removed" to BackupValue.Flag(true)))
            )
            every { fixture.preferences.filterSupported(any()) } returns BackupPreferences(
                emptyMap(),
                emptyMap()
            )
            // When
            val inspection = fixture.store.inspect(source) {}
            // Then
            assertFalse(inspection.entries.single().isAvailable)
            assertEquals(2, inspection.skippedCount)
        }

    @Test
    fun `given insufficient private space, source is selected, expected failure with staging removed and external source preserved`() =
        runTest {
            // Given
            val source = fixture.document("settings", "{}")
            every { fixture.files.availableBytes() } returns 1
            // When
            val failure = runCatching { fixture.store.inspect(source) {} }.exceptionOrNull()
            // Then
            assertEquals("Not enough storage for backup", failure?.message)
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
            assertTrue(FileSystem.SYSTEM.exists(source.removePrefix("file://").toPath()))
        }

    @Test
    fun `given document transport error, import starts, expected same failure and no partial staging`() =
        runTest {
            // Given
            val failure = IllegalStateException("document permission expired")
            every { fixture.files.openDocument(any()) } throws failure
            // When
            val caught =
                runCatching { fixture.store.inspect("content://provider/document") {} }.exceptionOrNull()
            // Then
            assertSame(failure, caught)
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
        }

    @Test
    fun `given import progress cancellation, staging copy is interrupted, expected same cancellation and source preserved`() =
        runTest {
            // Given
            val source = fixture.document("settings", "{}")
            val cancellation = CancellationException("user canceled")
            // When
            val failure =
                runCatching { fixture.store.inspect(source) { throw cancellation } }.exceptionOrNull()
            // Then
            assertSame(cancellation, failure)
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
            assertTrue(FileSystem.SYSTEM.exists(source.removePrefix("file://").toPath()))
        }

    @Test
    fun `given negative network bytes, selected archive is inspected, expected rejection before restoration`() =
        runTest {
            // Given
            val source = fixture.document("network", "{\"inference\":-1}")
            // When
            val failure = runCatching { fixture.store.inspect(source) {} }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
        }

    @Test
    fun `given gallery record count exceeds payload, archive is inspected, expected rejection and staging cleanup`() =
        runTest {
            // Given
            val source = fixture.galleryDocument(declaredCount = 2)
            // When
            val failure = runCatching { fixture.store.inspect(source) {} }.exceptionOrNull()
            // Then
            assertTrue(failure != null)
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
        }

    @Test
    fun `given valid gallery archive, inspection reads each image, expected progress matches declared count`() =
        runTest {
            // Given
            val source = fixture.galleryDocument(declaredCount = 1)
            val validated = mutableListOf<Long>()
            // When
            val inspection =
                fixture.store.inspect(source) { if (it.category == BackupCategory.GALLERY) validated += it.completed }
            // Then
            assertEquals(listOf(1L), validated)
            assertEquals(BackupCategory.GALLERY, inspection.entries.single().category)
            assertTrue(inspection.entries.single().isAvailable)
        }
}
