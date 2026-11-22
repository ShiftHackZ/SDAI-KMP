package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import io.mockk.every
import io.mockk.mockk
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Protects every legacy provider from treating interrupted app-owned payloads as installed models.
 *
 * @param type Provider whose complete layout is checked before and after activation.
 * @author Dmitriy Moroz
 */
@RunWith(Parameterized::class)
class ModelDownloadMarkerTest(private val type: LocalAiModel.Type) {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `given owned model directory, interrupted marker accompanies complete looking files, expected model unavailable`() {
        // Given
        val model = LocalAiModel("catalog", type, "Model", "1 GB", emptyList())
        val directory = folder.newFolder(model.id)
        listOf(
            "transformer-packed-mflux",
            "text_encoder-mlx-4bit",
            "tokenizer",
            "vae"
        ).forEach { File(directory, it).mkdirs() }
        File(directory, ".download-in-progress").createNewFile()
        val descriptor =
            mockk<FileProviderDescriptor> { every { localModelDirPath } returns folder.root.path }
        val store = AndroidDownloadableModelFileStore(descriptor, mockk(relaxed = true))
        // When
        val isDownloaded = store.isDownloaded(model)
        // Then
        assertFalse(isDownloaded)
        assertTrue(directory.exists())
    }

    @Test
    fun `given owned model directory, activation removes pending marker, expected completed layout becomes available`() {
        // Given
        val model = LocalAiModel("catalog", type, "Model", "1 GB", emptyList())
        val directory = folder.newFolder(model.id)
        listOf(
            "transformer-packed-mflux",
            "text_encoder-mlx-4bit",
            "tokenizer",
            "vae"
        ).forEach { File(directory, it).mkdirs() }
        File(directory, "transformer-packed-mflux/quantization_config.json").writeText("{}")
        if (type == LocalAiModel.Type.Bonsai) File(directory, "scheduler").mkdirs()
        val pending = File(directory, ".download-in-progress").apply { createNewFile() }
        val descriptor =
            mockk<FileProviderDescriptor> { every { localModelDirPath } returns folder.root.path }
        val store = AndroidDownloadableModelFileStore(descriptor, mockk(relaxed = true))
        // When
        pending.delete()
        val isDownloaded = store.isDownloaded(model)
        // Then
        assertTrue(isDownloaded)
    }

    companion object {
        /**
         * Supplies providers that activate their downloads in the shared app-private model directory.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun providers() =
            LocalAiModel.Type.entries.filterNot { it == LocalAiModel.Type.Mnn }.map { arrayOf(it) }
    }
}
