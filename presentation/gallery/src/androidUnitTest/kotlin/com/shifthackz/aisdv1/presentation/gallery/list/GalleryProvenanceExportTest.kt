package com.shifthackz.aisdv1.presentation.gallery.list

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.presentation.components.utils.OriginalImageFileExporter
import com.shifthackz.aisdv1.presentation.gallery.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.presentation.gallery.mocks.mockGalleryMemoryBudget
import com.shifthackz.aisdv1.presentation.gallery.mocks.toGalleryMetadata
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Base64
import java.util.zip.ZipFile

/**
 * Inspects ZIPs produced by the production gallery exporter, including original pixels and typed metadata.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class GalleryProvenanceExportTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private val descriptor = mockk<FileProviderDescriptor>()
    private val reads = mockk<GalleryReadGateway>()
    private val originals = mockk<OriginalImageFileExporter>()
    private val exporter =
        GalleryExporter(descriptor, reads, mockk(), originals, mockGalleryMemoryBudget)
    private val encoded =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aGXYAAAAASUVORK5CYII="

    @Test
    fun `given selected gallery image with provenance, ZIP is exported, expected original bytes and complete machine readable metadata`() =
        runTest {
            // Given
            val image = mockAiGenerationResult.copy(
                providerKey = "local_mnn",
                modelName = "SD 1.5",
                acceleration = "OpenCL",
                deviceName = "Pixel 7 Pro",
                osVersion = "Android 17 (API 37)",
                generationDurationMillis = 3_662_123
            ).toGalleryMetadata()
            val bytes = Base64.getDecoder().decode(encoded)
            every { descriptor.imagesCacheDirPath } returns temporary.root.absolutePath
            coEvery { reads.metadata(listOf(image.id)) } returns listOf(image)
            coEvery { reads.image(image.id, image.imageRevision, false, any()) } returns encoded
            coEvery { originals.export(encoded, any()) } answers {
                temporary.newFile("source.png").apply { writeBytes(bytes) }
            }
            // When
            val result = exporter.export(listOf(image.id))
            // Then
            ZipFile(result.filePath).use { archive ->
                assertArrayEquals(
                    bytes,
                    archive.getInputStream(archive.getEntry("${image.id}.png")).readBytes()
                )
                val json = JSONObject(
                    archive.getInputStream(archive.getEntry("${image.id}.json")).reader().readText()
                )
                assertEquals("local_mnn", json.getString("provider_key"))
                assertEquals("SD 1.5", json.getString("model_name"))
                assertEquals("OpenCL", json.getString("acceleration"))
                assertEquals("Pixel 7 Pro", json.getString("device_name"))
                assertEquals("Android 17 (API 37)", json.getString("os_version"))
                assertEquals(3_662_123L, json.getLong("generation_duration_millis"))
                assertEquals("01:01:02", json.getString("generation_duration"))
            }
        }

    @Test
    fun `given legacy gallery image, ZIP is exported, expected absent provenance encoded as JSON null instead of invented values`() =
        runTest {
            // Given
            val image = mockAiGenerationResult.toGalleryMetadata()
            every { descriptor.imagesCacheDirPath } returns temporary.root.absolutePath
            coEvery { reads.metadata(listOf(image.id)) } returns listOf(image)
            coEvery { reads.image(image.id, image.imageRevision, false, any()) } returns encoded
            coEvery { originals.export(encoded, any()) } answers {
                temporary.newFile("source.png")
                    .apply { writeBytes(Base64.getDecoder().decode(encoded)) }
            }
            // When
            val result = exporter.export(listOf(image.id))
            // Then
            ZipFile(result.filePath).use { archive ->
                val json = JSONObject(
                    archive.getInputStream(archive.getEntry("${image.id}.json")).reader().readText()
                )
                listOf(
                    "provider_key",
                    "acceleration",
                    "device_name",
                    "os_version",
                    "generation_duration_millis",
                    "generation_duration"
                ).forEach { key ->
                    assertTrue(json.has(key))
                    assertTrue(json.isNull(key))
                }
            }
        }
}
