package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.storage.db.persistent.dao.CivitaiUploadDao
import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CivitaiUploadLocalDataSourceTest {
    private val dao = mockk<CivitaiUploadDao>()
    private val preferences = mockk<KeyValueStore>(relaxed = true)
    private val source = CivitaiUploadLocalDataSourceImpl(dao, preferences)
    private val row = CivitaiUploadEntity(
        91,
        12,
        7,
        "https://civitai.com/posts/123",
        200,
        100,
        "revision",
        "PUBLISHED"
    )

    @Before
    fun setup() {
        every { preferences.getBoolean("civitai_history_migrated", any()) } returns true
        every { dao.observePage(any(), any()) } returns flowOf(listOf(row))
        every { dao.observeIds() } returns flowOf(listOf(91))
    }

    @Test
    fun `given viewport bounds, history page requested, expected exact limit offset and original receipt metadata`() =
        runTest {
            // Given
            val offset = 95
            // When
            val page = source.observePage(30, offset).first()
            // Then
            verify(exactly = 1) { dao.observePage(30, offset) }
            assertEquals(91L, page.single().id)
            assertEquals(row.url, page.single().url)
            assertEquals(row.imageRevision, page.single().imageRevision)
        }

    @Test
    fun `given invalid page requests, history page collected, expected rejected bounds before storage reads`() =
        runTest {
            // Given
            val requests = listOf(0 to 0, 121 to 0, 30 to -1)
            // When
            val failures = requests.map { (limit, offset) ->
                runCatching {
                    source.observePage(limit, offset).first()
                }.exceptionOrNull()
            }
            // Then
            assertTrue(failures.all { it is IllegalArgumentException })
            verify(exactly = 0) { dao.observePage(any(), any()) }
        }

    @Test
    fun `given new phase for existing original, receipt saved, expected stable lazy-list identity retained`() =
        runTest {
            // Given
            coEvery { dao.find(12, 100, "revision", 7) } returns row
            coEvery { dao.insert(any()) } returns 91
            val receipt =
                CivitaiUpload(12, 7, null, 300, 100, "revision", CivitaiUploadStatus.UNCERTAIN)
            // When
            val saved = source.save(receipt)
            // Then
            assertEquals(91L, saved.id)
            coVerify { dao.insert(row.copy(createdAt = 300, url = null, status = "UNCERTAIN")) }
        }

    @Test
    fun `given legacy receipt list, history opens, expected migration committed before ordinary preferences cleared`() =
        runTest {
            // Given
            every { preferences.getBoolean("civitai_history_migrated", any()) } returns false
            every {
                preferences.getString(
                    "civitai_uploads",
                    any()
                )
            } returns "[{\"imageId\":12,\"accountId\":7,\"url\":null,\"createdAt\":200,\"imageCreatedAt\":100,\"imageRevision\":\"revision\",\"status\":\"UNCERTAIN\"}]"
            coEvery { dao.find(any(), any(), any(), any()) } returns null
            var hasSavedReceipt = false
            coEvery { dao.insert(any()) } answers { hasSavedReceipt = true; 91 }
            every { preferences.putSettingsDurably(any(), any(), any()) } answers {
                assertTrue(
                    hasSavedReceipt
                )
            }
            // When
            val ids = source.observeIds().first()
            // Then
            assertEquals(listOf(91L), ids)
            verify {
                preferences.putSettingsDurably(
                    mapOf("civitai_uploads" to "[]"),
                    mapOf("civitai_history_migrated" to true),
                    emptyMap()
                )
            }
        }

    @Test
    fun `given migration storage failure, history opens, expected failure propagated and legacy data retained`() =
        runTest {
            // Given
            every { preferences.getBoolean("civitai_history_migrated", any()) } returns false
            every {
                preferences.getString(
                    "civitai_uploads",
                    any()
                )
            } returns "[{\"imageId\":12,\"accountId\":7,\"createdAt\":200,\"imageCreatedAt\":100,\"imageRevision\":\"revision\",\"status\":\"UNCERTAIN\"}]"
            coEvery { dao.find(any(), any(), any(), any()) } returns null
            val error = IllegalStateException("storage unavailable")
            coEvery { dao.insert(any()) } throws error
            // When
            val failure = runCatching { source.observeIds().first() }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            verify(exactly = 0) { preferences.putSettingsDurably(any(), any(), any()) }
        }

    @Test
    fun `given already migrated history, IDs observed, expected no legacy preference parsing or writes`() =
        runTest {
            // Given
            every {
                preferences.getString(
                    any(),
                    any()
                )
            } throws IllegalStateException("must not read legacy list")
            // When
            val ids = source.observeIds().first()
            // Then
            assertEquals(listOf(91L), ids)
            verify(exactly = 0) { preferences.putSettingsDurably(any(), any(), any()) }
        }
}
