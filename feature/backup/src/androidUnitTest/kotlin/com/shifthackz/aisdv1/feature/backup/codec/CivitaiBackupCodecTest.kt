package com.shifthackz.aisdv1.feature.backup.codec

import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import kotlinx.serialization.json.JsonObject
import org.junit.Test

class CivitaiBackupCodecTest {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
    private val framing = BinaryBackupArchiveCodec(json)
    private val codec = DefaultCivitaiBackupCodec(framing, json)
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

    @Test
    fun `given a published receipt, archive round trips, expected public metadata preserved with fresh receipt ID`() {
        // Given
        val buffer = Buffer()
        codec.write(buffer, row)
        // When
        val restored = codec.read(buffer)
        // Then
        assertEquals(row.copy(id = 0), restored)
        assertTrue(buffer.exhausted())
    }

    @Test
    fun `given an interrupted upload, history is restored, expected uncertain status preventing automatic repost`() {
        // Given
        val buffer = Buffer()
        codec.write(buffer, row.copy(url = null, status = "UPLOADING"))
        // When
        val restored = codec.read(buffer)!!
        // Then
        assertEquals("UNCERTAIN", restored.status)
        assertNull(restored.url)
    }

    @Test
    fun `given failed and uncertain receipts, archive round trips, expected their durable states retained`() {
        // Given
        val rows = listOf("FAILED", "UNCERTAIN").map { row.copy(url = null, status = it) }
        val buffer = Buffer()
        rows.forEach { codec.write(buffer, it) }
        // When
        val restored = rows.map { codec.read(buffer) }
        // Then
        assertEquals(rows.map { it.copy(id = 0) }, restored)
    }

    @Test
    fun `given a future receipt status, a known record follows, expected future receipt skipped without losing framing`() {
        // Given
        val buffer = Buffer()
        codec.write(buffer, row.copy(status = "FUTURE"))
        codec.write(buffer, row)
        // When
        val first = codec.read(buffer)
        val second = codec.read(buffer)
        // Then
        assertNull(first)
        assertEquals(row.copy(id = 0), second)
        assertTrue(buffer.exhausted())
    }

    @Test
    fun `given receipt serialization, public history is written, expected allowlisted fields without local ID or credentials`() {
        // Given
        val buffer = Buffer()
        codec.write(buffer, row)
        // When
        val text = framing.readJson(buffer)
        val keys = Json.parseToJsonElement(text).toString()
        // Then
        assertFalse(keys.contains("accessToken"))
        assertFalse(keys.contains("refreshToken"))
        assertFalse(keys.contains("apiKey"))
        assertEquals(
            setOf(
                "imageId",
                "accountId",
                "url",
                "createdAt",
                "imageCreatedAt",
                "imageRevision",
                "status"
            ),
            (Json.parseToJsonElement(text) as JsonObject).keys
        )
    }

    @Test
    fun `given unknown credential fields in an archive, receipt is decoded and exported, expected credentials discarded`() {
        // Given
        val buffer = Buffer()
        val raw = json.encodeToString(
            CivitaiBackupRecord(
                12,
                4,
                "https://civitai.com/posts/123",
                200,
                100,
                "source-revision",
                "PUBLISHED"
            )
        )
        framing.writeJson(
            buffer,
            raw.dropLast(1) + ",\"accessToken\":\"must-not-survive\",\"refreshToken\":\"must-not-survive\"}"
        )
        // When
        val restored = codec.read(buffer)!!
        val exported = Buffer()
        codec.write(exported, restored)
        // Then
        assertFalse(framing.readJson(exported).contains("must-not-survive"))
    }

    @Test
    fun `given invalid receipt identities metadata or links, archive is decoded, expected each invalid record rejected`() {
        // Given
        val invalid = listOf(
            row.copy(imageId = 0),
            row.copy(accountId = -1),
            row.copy(createdAt = -1),
            row.copy(imageCreatedAt = -1),
            row.copy(imageRevision = ""),
            row.copy(imageRevision = "x".repeat(257)),
            row.copy(url = null),
            row.copy(url = "http://civitai.com/posts/123"),
            row.copy(url = "https://civitai.com.evil/posts/123"),
            row.copy(url = "javascript:alert(1)"),
        )
        // When
        val failures = invalid.map { candidate ->
            val buffer = Buffer()
            codec.write(buffer, candidate)
            runCatching { codec.read(buffer) }.exceptionOrNull()
        }
        // Then
        assertTrue(failures.all { it is IllegalArgumentException })
    }

    @Test
    fun `given truncated receipt frame, history is decoded, expected failure without partial record`() {
        // Given
        val buffer = Buffer().writeIntLe(100).writeUtf8("{}")
        // When
        val failure = runCatching { codec.read(buffer) }.exceptionOrNull()
        // Then
        assertNotNull(failure)
    }
}
