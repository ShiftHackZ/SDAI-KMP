package com.shifthackz.aisdv1.feature.backup.archive

import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.feature.backup.codec.DefaultCivitaiBackupCodec
import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CivitaiBackupArchiveTest {
    @get:Rule
    val temporary = TemporaryFolder()

    @Test
    fun `given more than one history page, Civitai-only backup is inspected, expected all public receipts preserved and no other section`() =
        runTest {
            // Given
            val fixture = BackupArchiveFixture(temporary.newFolder().absolutePath.toPath())
            fixture.uploads = (1L..145L).map {
                CivitaiUploadEntity(
                    it,
                    it,
                    7,
                    "https://civitai.com/posts/$it",
                    it,
                    100,
                    "revision-$it",
                    "PUBLISHED"
                )
            }
            val progress = mutableListOf<Long>()
            // When
            val exported =
                fixture.store.create(BackupSelection(setOf(BackupCategory.CIVITAI))) { progress += it.completed }
            val inspection = fixture.store.inspect(exported.document) {}
            val staged = fixture.store.staged(inspection.handle)
            val publicCodec = DefaultCivitaiBackupCodec(fixture.codec, fixture.json)
            val restored =
                fixture.codec.openSection(staged.path, staged.manifest, "civitai").use { source ->
                    List(145) { publicCodec.read(source) }
                }
            // Then
            assertEquals(listOf("civitai"), staged.manifest.sections.map { it.id })
            assertEquals(145L, inspection.entries.single().itemCount)
            assertEquals(fixture.uploads.map { it.copy(id = 0) }, restored)
            assertEquals(listOf(64L, 128L, 145L), progress)
            coVerify(exactly = 0) { fixture.authorization.getAuthorizationCredentials() }
            coVerify(exactly = 4) { fixture.receipts.queryBackupPage(any(), 64) }
        }

    @Test
    fun `given empty local history, backup inventory is loaded, expected Civitai section visible and selectable`() =
        runTest {
            // Given
            val fixture = BackupArchiveFixture(temporary.newFolder().absolutePath.toPath())
            // When
            val entry =
                fixture.store.inventory().entries.single { it.category == BackupCategory.CIVITAI }
            // Then
            assertEquals(0L, entry.itemCount)
            assertTrue(entry.isAvailable)
        }

    @Test
    fun `given unselected Civitai history, settings backup is created, expected no history reads or receipt section`() =
        runTest {
            // Given
            val fixture = BackupArchiveFixture(temporary.newFolder().absolutePath.toPath())
            // When
            val export = fixture.store.create(BackupSelection(setOf(BackupCategory.SETTINGS))) {}
            val staged = fixture.store.staged(export.handle)
            // Then
            assertEquals(listOf("settings"), staged.manifest.sections.map { it.id })
            coVerify(exactly = 0) { fixture.receipts.queryBackupPage(any(), any()) }
        }

    @Test
    fun `given malformed history with valid framing, backup inspection runs, expected rejection before restore`() =
        runTest {
            // Given
            val fixture = BackupArchiveFixture(temporary.newFolder().absolutePath.toPath())
            val document = fixture.document("civitai", "{}")
            // When
            val failure = runCatching { fixture.store.inspect(document) {} }.exceptionOrNull()
            // Then
            assertTrue(failure != null)
        }

    @Test
    fun `given empty Civitai selected with settings, archive is created and inspected, expected no Civitai section on restore`() =
        runTest {
            // Given
            val fixture = BackupArchiveFixture(temporary.newFolder().absolutePath.toPath())
            val selected = BackupSelection(setOf(BackupCategory.CIVITAI, BackupCategory.SETTINGS))
            // When
            val export = fixture.store.create(selected) {}
            val inspection = fixture.store.inspect(export.document) {}
            val staged = fixture.store.staged(inspection.handle)
            // Then
            assertEquals(listOf("settings"), staged.manifest.sections.map { it.id })
            assertTrue(inspection.entries.none { it.category == BackupCategory.CIVITAI })
        }

    @Test
    fun `given only empty Civitai selected, archive is created and inspected, expected valid archive with no restore sections`() =
        runTest {
            // Given
            val fixture = BackupArchiveFixture(temporary.newFolder().absolutePath.toPath())
            // When
            val export = fixture.store.create(BackupSelection(setOf(BackupCategory.CIVITAI))) {}
            val inspection = fixture.store.inspect(export.document) {}
            // Then
            assertTrue(inspection.entries.isEmpty())
            assertTrue(fixture.store.staged(inspection.handle).manifest.sections.isEmpty())
        }

}
