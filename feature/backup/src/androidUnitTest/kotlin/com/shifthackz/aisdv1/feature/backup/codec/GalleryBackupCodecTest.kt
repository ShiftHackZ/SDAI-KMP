package com.shifthackz.aisdv1.feature.backup.codec

import com.shifthackz.aisdv1.domain.gateway.backup.BackupImageValidator
import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryBackupCodecTest {
    private val validator =
        mockk<BackupImageValidator> { every { this@mockk.invoke(any()) } returns Unit }
    private val codec =
        DefaultGalleryBackupCodec(
            BinaryBackupArchiveCodec(Json { encodeDefaults = true }),
            Json { encodeDefaults = true; ignoreUnknownKeys = true },
            validator,
            BackupImageBudget(24 * 1024 * 1024, 16_777_216L, 128L * 1024 * 1024, 256)
        )
    private val row = GenerationResultEntity(
        17,
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aGXYAAAAASUVORK5CYII=",
        "",
        100,
        "txt2img",
        "prompt",
        "negative",
        1,
        1,
        20,
        7.5f,
        false,
        "Euler",
        "42",
        "9",
        0.25f,
        0.5f,
        true,
        true,
        "model",
        "old revision",
        deviceName = "Google Pixel 7 Pro",
        osVersion = "Android 17 (API 37)",
        providerKey = "local_mnn",
        acceleration = "OpenCL",
        generationDurationMillis = 3_661_002,
    )

    @Test
    fun `given a published gallery image, backup round trips, expected Civitai URL retained with fresh local identity`() {
        // Given
        val published = row.copy(civitaiUrl = "https://civitai.com/posts/123")
        val source = Buffer()
        codec.write(source, published)
        // When
        val restored = codec.read(source, "restored")!!
        // Then
        assertEquals(published.civitaiUrl, restored.civitaiUrl)
        assertEquals(0L, restored.id)
        assertEquals("restored", restored.imageRevision)
    }

    @Test
    fun `given old gallery backup metadata, Civitai URL is absent, expected null link`() {
        // Given
        val metadata = "{}"
        // When
        val record = Json.decodeFromString<GalleryBackupRecord>(metadata)
        // Then
        assertNull(record.civitaiUrl)
    }

    @Test
    fun `given malicious gallery link, backup is decoded, expected unsafe URL rejected`() {
        // Given
        val source = Buffer()
        codec.write(source, row.copy(civitaiUrl = "javascript:alert(1)"))
        // When
        val failure =
            assertThrows(IllegalArgumentException::class.java) { codec.read(source, "restored") }
        // Then
        assertEquals("Invalid Civitai post URL", failure.message)
    }

    @Test
    fun `given legacy gallery metadata, optional provenance fields are absent, expected unknown values remain null`() {
        // Given
        val metadata = "{}"
        // When
        val record = Json.decodeFromString<GalleryBackupRecord>(metadata)
        // Then
        assertNull(record.deviceName)
        assertNull(record.osVersion)
        assertNull(record.providerKey)
        assertNull(record.acceleration)
        assertNull(record.generationDurationMillis)
    }

    @Test
    fun `given negative generation duration, archive is restored, expected invalid metadata rejection`() {
        // Given
        val source = Buffer()
        codec.write(source, row.copy(generationDurationMillis = -1))
        // When
        val failure =
            assertThrows(IllegalArgumentException::class.java) { codec.read(source, "new") }
        // Then
        assertEquals("Invalid generation duration", failure.message)
    }

    @Test
    fun `given saved gallery row, binary record round trips, expected fresh identity and preserved history flags`() {
        // Given
        val source = Buffer()
        codec.write(source, row)
        // When
        val imported = codec.read(source, "new revision")!!
        // Then
        assertEquals(0L, imported.id)
        assertEquals("new revision", imported.imageRevision)
        assertEquals(row.copy(id = 0, imageRevision = "new revision"), imported)
        verify(exactly = 2) { validator(any()) }
    }

    @Test
    fun `given future generation type, next known gallery record follows, expected unknown row skipped without desynchronizing stream`() {
        // Given
        val source = Buffer()
        codec.write(source, row.copy(generationType = "future"))
        codec.write(source, row)
        // When
        val unknown = codec.read(source, "ignored")
        val known = codec.read(source, "new")
        // Then
        assertNull(unknown)
        assertEquals(row.prompt, known?.prompt)
        assertEquals(true, source.exhausted())
    }

    @Test
    fun `given archived image bytes, native decoder rejects input, expected gallery validation failure`() {
        // Given
        val source = Buffer()
        codec.write(source, row)
        every { validator(any()) } throws IllegalArgumentException("Image decoding failed")
        // When
        val failure =
            assertThrows(IllegalArgumentException::class.java) { codec.read(source, "new") }
        // Then
        assertEquals("Image decoding failed", failure.message)
    }

    @Test
    fun `given OpenAI compatible gallery row, zero sampling steps round trip, expected saved metadata restored`() {
        // Given
        val original = row.copy(samplingSteps = 0)
        val source = Buffer()
        codec.write(source, original)
        // When
        val restored = codec.read(source, "imported")
        // Then
        assertEquals(original.copy(id = 0, imageRevision = "imported"), restored)
    }

    @Test
    fun `given oversized archived image record count, reader reaches image frame, expected rejection before allocating or reading payload`() {
        // Given
        val source = Buffer()
        BinaryBackupArchiveCodec(Json).writeJson(source, "{}")
        source.writeIntLe(24 * 1024 * 1024 + 1)
        // When
        val failure =
            assertThrows(IllegalArgumentException::class.java) { codec.read(source, "revision") }
        // Then
        assertEquals("Backup image exceeds encoded record budget", failure.message)
        assertTrue(source.exhausted())
        verify(exactly = 0) { validator(any()) }
    }

    @Test
    fun `given image bytes exceed a configured export budget, writer encodes row, expected rejection before native validation or image allocation`() {
        // Given
        val constrained = DefaultGalleryBackupCodec(
            BinaryBackupArchiveCodec(Json), Json, validator,
            BackupImageBudget(12, 16_777_216, 128L * 1024 * 1024, 256)
        )
        // When
        val failure =
            assertThrows(IllegalArgumentException::class.java) { constrained.write(Buffer(), row) }
        // Then
        assertEquals("Backup image exceeds encoded record budget", failure.message)
        verify(exactly = 0) { validator(any()) }
    }

    @Test
    fun `given individually admitted output and input images, combined gallery record exceeds budget, expected matching export and import rejection`() {
        // Given
        val admitted = Buffer().also { codec.write(it, row) }
        val metadata = BinaryBackupArchiveCodec(Json).readJson(admitted)
        val byteCount = admitted.readIntLe()
        val constrained = DefaultGalleryBackupCodec(
            BinaryBackupArchiveCodec(Json), Json, validator,
            BackupImageBudget(byteCount, 16_777_216, 128L * 1024 * 1024, 256)
        )
        val oversizedPair = Buffer()
        BinaryBackupArchiveCodec(Json).writeJson(oversizedPair, metadata)
        oversizedPair.writeIntLe(byteCount)
        oversizedPair.write(admitted.readByteArray(byteCount.toLong()))
        oversizedPair.writeIntLe(byteCount)
        // When
        val exportFailure = assertThrows(IllegalArgumentException::class.java) {
            constrained.write(
                Buffer(),
                row.copy(originalImageBase64 = row.imageBase64)
            )
        }
        val importFailure = assertThrows(IllegalArgumentException::class.java) {
            constrained.read(
                oversizedPair,
                "revision"
            )
        }
        // Then
        assertEquals("Backup image exceeds encoded record budget", exportFailure.message)
        assertEquals(exportFailure.message, importFailure.message)
    }

    @Test
    fun `given saved alternate Base64 alphabet, backup writer normalizes image bytes, expected equivalent standard image restored`() {
        // Given
        val alternate = row.imageBase64.replace('/', '_').replace('+', '-')
        val source = Buffer()
        // When
        codec.write(source, row.copy(imageBase64 = alternate))
        val restored = codec.read(source, "imported")
        // Then
        assertEquals(row.copy(id = 0, imageRevision = "imported"), restored)
    }

}
