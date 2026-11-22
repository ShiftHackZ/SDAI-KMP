package com.shifthackz.aisdv1.storage.db.persistent

import androidx.room.Room
import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSessionEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSectionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Exercises generated Room SQL to protect receipt columns, identity indexes and restore-ledger contracts.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], manifest = Config.NONE)
class CivitaiStorageTest {
    private lateinit var database: PersistentDatabase
    private val receipt = CivitaiUploadEntity(
        imageId = 12, accountId = 7, url = "https://civitai.com/posts/123", createdAt = 200,
        imageCreatedAt = 100, imageRevision = "revision", status = "PUBLISHED",
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            PersistentDatabase::class.java
        )
            .allowMainThreadQueries().build()
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun `given public receipt, Room persists and queries it, expected every contract column round trips`() =
        runTest {
            // Given
            val dao = database.civitaiUploadDao()
            // When
            val id = dao.insert(receipt)
            val restored = dao.find(12, 100, "revision", 7)
            // Then
            assertEquals(receipt.copy(id = id), restored)
            assertEquals(1L, dao.count())
            assertEquals(listOf(id), dao.observeIds().first())
            assertEquals(listOf(restored), dao.observePage(30, 0).first())
        }

    @Test
    fun `given multiple account attempts, receipt is queried, expected published original preferred and owner guards respected`() =
        runTest {
            // Given
            val dao = database.civitaiUploadDao()
            val published = dao.insert(receipt)
            dao.insert(
                receipt.copy(
                    accountId = 8,
                    createdAt = 300,
                    status = "UNCERTAIN",
                    url = null
                )
            )
            // When
            val preferred = dao.find(12, 100, "revision", null)
            val other = dao.find(12, 100, "revision", 8)
            // Then
            assertEquals(published, preferred?.id)
            assertEquals("UNCERTAIN", other?.status)
            assertNull(dao.find(12, 101, "revision", null))
            assertNull(dao.find(12, 100, "replacement", null))
            assertNull(dao.find(12, 100, "revision", 9))
        }

    @Test
    fun `given equal publication timestamps, viewport and backup pages are read, expected stable tie ordering and keyset boundaries`() =
        runTest {
            // Given
            val dao = database.civitaiUploadDao()
            val ids = (1L..3L).map { dao.insert(receipt.copy(imageId = it)) }
            // When
            val viewport = dao.observePage(2, 1).first()
            val backup = dao.queryBackupPage(ids.first(), 1)
            // Then
            assertEquals(ids.reversed(), dao.observeIds().first())
            assertEquals(ids.reversed().drop(1), viewport.map { it.id })
            assertEquals(listOf(ids[1]), backup.map { it.id })
            assertTrue(dao.queryBackupPage(ids.last(), 64).isEmpty())
        }

    @Test
    fun `given same account and original, receipt is replaced, expected unique composite index prevents duplicate history`() =
        runTest {
            // Given
            val dao = database.civitaiUploadDao()
            val id = dao.insert(receipt.copy(status = "UPLOADING", url = null))
            // When
            dao.insert(receipt.copy(id = id))
            dao.insert(receipt.copy(createdAt = 300))
            // Then
            assertEquals(1L, dao.count())
            assertEquals("PUBLISHED", dao.find(12, 100, "revision", 7)?.status)
        }

    @Test
    fun `given saved generation, Civitai URL is recorded, expected full row and metadata projection retain provenance`() =
        runTest {
            // Given
            val dao = database.generationResultDao()
            val original = GenerationResultEntity(
                12,
                "output",
                "input",
                100,
                "txt2img",
                "cat",
                "blur",
                512,
                512,
                20,
                7f,
                false,
                "Euler",
                "42",
                "",
                0f,
                0f,
                false,
                imageRevision = "revision",
                providerKey = "local_mnn",
                acceleration = "Metal",
                deviceName = "Original device",
                osVersion = "iOS 27",
                generationDurationMillis = 1234
            )
            dao.insert(original)
            // When
            dao.setCivitaiUrl(12, receipt.url!!)
            val metadata = dao.queryMetadata(listOf(12)).single()
            // Then
            assertEquals(
                original.copy(civitaiUrl = receipt.url),
                dao.queryBackupPage(0, 1).single()
            )
            assertEquals(receipt.url, metadata.civitaiUrl)
            assertEquals(original.providerKey, metadata.providerKey)
            assertEquals(original.deviceName, metadata.deviceName)
            assertEquals(original.generationDurationMillis, metadata.generationDurationMillis)
        }

    @Test
    fun `given restore session and category ledger, database round trips intent and counts, expected exact contract fields`() =
        runTest {
            // Given
            val dao = database.restoreSessionDao()
            val session = RestoreSessionEntity("session", "archive.sdai", "[\"civitai\"]")
            val section = RestoreSectionEntity("session", "civitai", 4, 2, "provider")
            // When
            dao.insert(session)
            dao.mark(section)
            // Then
            assertEquals(session, dao.get("session"))
            assertEquals(listOf(session), dao.pending())
            assertEquals(listOf("civitai"), dao.completed("session"))
            assertEquals(listOf(section), dao.results("session"))
            assertTrue(dao.results("other").isEmpty())
        }

    @Test
    fun `given restore requiring configuration, sections finish then handoff is acknowledged, expected pending until both flags clear`() =
        runTest {
            // Given
            val dao = database.restoreSessionDao()
            dao.insert(
                RestoreSessionEntity(
                    "session",
                    "archive.sdai",
                    "[]",
                    requiresConfiguration = true
                )
            )
            // When
            dao.finish("session")
            val pendingHandoff = dao.pending().single()
            dao.acknowledgeConfiguration("session")
            // Then
            assertTrue(pendingHandoff.isComplete)
            assertTrue(pendingHandoff.requiresConfiguration)
            assertTrue(dao.pending().isEmpty())
            assertEquals(false, dao.get("session")?.requiresConfiguration)
        }

    @Test
    fun `given committed restore identities, duplicate intent and ledger are inserted, expected original rows protected by constraints`() =
        runTest {
            // Given
            val dao = database.restoreSessionDao()
            val session = RestoreSessionEntity("session", "archive.sdai", "[]")
            val section = RestoreSectionEntity("session", "civitai", 4, 2)
            dao.insert(session)
            dao.mark(section)
            // When
            val duplicateSession =
                runCatching { dao.insert(session.copy(archiveHandle = "other.sdai")) }.exceptionOrNull()
            val duplicateSection =
                runCatching { dao.mark(section.copy(importedCount = 99)) }.exceptionOrNull()
            // Then
            assertTrue(duplicateSession != null)
            assertTrue(duplicateSection != null)
            assertEquals(session, dao.get("session"))
            assertEquals(listOf(section), dao.results("session"))
        }
}
