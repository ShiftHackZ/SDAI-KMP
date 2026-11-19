package com.shifthackz.aisdv1.feature.backup.archive

import com.shifthackz.aisdv1.domain.entity.NetworkUsageBucket
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.storage.db.persistent.entity.NetworkUsageEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
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

class BackupArchiveCreationTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private lateinit var fixture: BackupArchiveFixture

    @Before
    fun setup() {
        fixture = BackupArchiveFixture(temporary.newFolder("private").absolutePath.toPath())
    }

    @Test
    fun `given settings backup, archive is created, expected only selected settings section and no auth read`() =
        runTest {
            // Given
            val selection = BackupSelection(setOf(BackupCategory.SETTINGS))
            // When
            val export = fixture.store.create(selection) {}
            val staged = fixture.store.staged(export.handle)
            // Then
            assertEquals(listOf("settings"), staged.manifest.sections.map { it.id })
            assertEquals(123L, staged.manifest.createdAt)
            assertEquals(fixture.snapshot.settings, fixture.store.preferences(staged).settings)
            assertEquals(export, fixture.store.preparedExport(export.handle, export.filename))
            assertEquals(
                listOf("archive.sdaibackup"),
                FileSystem.SYSTEM.list(staged.path.parent!!).map { it.name })
            coVerify(exactly = 0) { fixture.authorization.getAuthorizationCredentials() }
            coVerify(exactly = 1) { fixture.staging.prepare() }
        }

    @Test
    fun `given provider backup, one provider selected, expected its portable credentials without other providers`() =
        runTest {
            // Given
            fixture.snapshot = BackupPreferences(
                mapOf(
                    "custom" to mapOf("serverUrl" to BackupValue.Text("https://host")),
                    "swarm_ui" to mapOf("serverUrl" to BackupValue.Text("https://other")),
                    "removed" to emptyMap()
                ), emptyMap()
            )
            coEvery { fixture.authorization.getAuthorizationCredentials() } returns AuthorizationCredentials.HttpBasic(
                "name",
                "password"
            )
            // When
            val export = fixture.store.create(
                BackupSelection(
                    setOf(BackupCategory.PROVIDERS),
                    setOf("custom")
                )
            ) {}
            val restored = fixture.store.preferences(fixture.store.staged(export.handle))
            // Then
            assertEquals(setOf("custom"), restored.providers.keys)
            assertEquals(
                BackupValue.Text("name"),
                restored.providers.getValue("custom")["basicLogin"]
            )
            assertEquals(
                BackupValue.Text("password"),
                restored.providers.getValue("custom")["basicPassword"]
            )
            assertTrue(restored.settings.isEmpty())
        }

    @Test
    fun `given empty provider selection, create is requested, expected rejection and private partial cleanup`() =
        runTest {
            // Given
            val selection = BackupSelection(setOf(BackupCategory.PROVIDERS))
            // When
            val failure = runCatching { fixture.store.create(selection) {} }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
        }

    @Test
    fun `given no selected categories, create is requested, expected rejection before staging allocation`() =
        runTest {
            // Given
            val selection = BackupSelection(emptySet())
            // When
            val failure = runCatching { fixture.store.create(selection) {} }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            verify(exactly = 0) { fixture.files.newId() }
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
        }

    @Test
    fun `given supported and removed network buckets, snapshot is exported, expected only portable counters`() =
        runTest {
            // Given
            val key = NetworkUsageBucket.entries.first().key
            fixture.counters =
                listOf(NetworkUsageEntity(key, 100), NetworkUsageEntity("removed", 200))
            // When
            val export = fixture.store.create(BackupSelection(setOf(BackupCategory.NETWORK))) {}
            val staged = fixture.store.staged(export.handle)
            val text = fixture.codec.openSection(staged.path, staged.manifest, "network")
                .use(fixture.codec::readJson)
            // Then
            assertEquals("{\"$key\":100}", text)
            assertEquals(1L, staged.manifest.sections.single().itemCount)
        }

    @Test
    fun `given invalid negative saved counter, backup is created, expected failure and no remaining partial files`() =
        runTest {
            // Given
            fixture.counters =
                listOf(NetworkUsageEntity(NetworkUsageBucket.entries.first().key, -1))
            // When
            val failure =
                runCatching { fixture.store.create(BackupSelection(setOf(BackupCategory.NETWORK))) {} }.exceptionOrNull()
            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
        }

    @Test
    fun `given snapshot read cancellation, backup creation is interrupted, expected original cancellation and partial cleanup`() =
        runTest {
            // Given
            val cancellation = CancellationException("canceled snapshot")
            coEvery { fixture.database.readSnapshot<Unit>(any()) } throws cancellation
            // When
            val failure =
                runCatching { fixture.store.create(BackupSelection(setOf(BackupCategory.SETTINGS))) {} }.exceptionOrNull()
            // Then
            assertSame(cancellation, failure)
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
        }

    @Test
    fun `given inventory with no gallery or network, selectable entries are loaded, expected zero categories disabled and no secrets read`() =
        runTest {
            // Given
            fixture.snapshot = fixture.snapshot.copy(providers = mapOf("removed" to emptyMap()))
            // When
            val inventory = fixture.store.inventory()
            // Then
            assertTrue(inventory.providers.isEmpty())
            assertEquals(
                setOf(BackupCategory.SETTINGS),
                inventory.entries.filter { it.isAvailable }.map { it.category }.toSet()
            )
            coVerify(exactly = 0) { fixture.authorization.getAuthorizationCredentials() }
        }

    @Test
    fun `given sparse gallery identities, selected gallery is exported, expected every row and metadata in stable order`() =
        runTest {
            // Given
            val earlier = fixture.row(5).copy(
                prompt = "first image", seed = "5", generationType = "img2img",
                originalImageBase64 = "iVBORw0KGgoAAAAB",
            )
            val later = fixture.row(71).copy(prompt = "second image", seed = "71")
            fixture.rows = listOf(later, earlier)
            val progress = mutableListOf<Long>()
            // When
            val export =
                fixture.store.create(BackupSelection(setOf(BackupCategory.GALLERY))) { progress += it.completed }
            val staged = fixture.store.staged(export.handle)
            val restored =
                fixture.codec.openSection(staged.path, staged.manifest, "gallery").use { source ->
                    List(2) { fixture.galleryCodec.read(source, "new-revision") }
                }
            // Then
            assertEquals(listOf(1L, 2L), progress)
            assertEquals(2L, staged.manifest.sections.single().itemCount)
            assertEquals(
                listOf(
                    earlier.copy(id = 0, imageRevision = "new-revision"),
                    later.copy(id = 0, imageRevision = "new-revision")
                ), restored
            )
        }

    @Test
    fun `given gallery backup in progress, user cancels after first image, expected partial archive removed and gallery unchanged`() =
        runTest {
            // Given
            fixture.rows = listOf(fixture.row(5), fixture.row(71))
            val cancellation = CancellationException("cancel gallery copy")
            // When
            val failure =
                runCatching { fixture.store.create(BackupSelection(setOf(BackupCategory.GALLERY))) { throw cancellation } }.exceptionOrNull()
            // Then
            assertSame(cancellation, failure)
            assertTrue(FileSystem.SYSTEM.list(fixture.root).isEmpty())
            assertEquals(listOf(5L, 71L), fixture.rows.map { it.id })
        }
}
