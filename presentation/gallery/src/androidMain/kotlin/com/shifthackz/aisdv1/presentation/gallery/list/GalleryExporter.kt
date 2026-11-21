package com.shifthackz.aisdv1.presentation.gallery.list

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryIdsUseCase
import com.shifthackz.aisdv1.presentation.components.utils.OriginalImageFileExporter
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryMemoryBudget
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import com.shifthackz.aisdv1.core.localization.formatter.formatGenerationDuration
import org.json.JSONObject

/**
 * Exports original PNG/JPEG/WebP bytes plus per-image generation metadata in a ZIP.
 * Processes one image at a time without bitmap decoding; cancellation removes the partial archive.
 *
 * @param fileProviderDescriptor Private export directory.
 * @param galleryReads Revision-checked bounded image and metadata reads.
 * @param getAllGalleryIdsUseCase Ordered lightweight IDs.
 * @param originals Streaming original file writer.
 * @param budget Maximum single encoded image admitted for export.
 * @author Dmitriy Moroz
 */
internal class GalleryExporter(
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val galleryReads: GalleryReadGateway,
    private val getAllGalleryIdsUseCase: GetAllGalleryIdsUseCase,
    private val originals: OriginalImageFileExporter,
    private val budget: GalleryMemoryBudget,
) : GalleryExportService {

    /**
     * Streams selected original images and generation metadata into one ZIP.
     * Failure or cancellation removes the partial archive and temporary image files.
     *
     * @param ids Selected IDs, or all existing gallery IDs when null.
     * @author Dmitriy Moroz
     */
    override suspend fun export(ids: List<Long>?): GalleryExportResult {
        val identities = ids?.distinct() ?: getAllGalleryIdsUseCase()
        val directory = File(fileProviderDescriptor.imagesCacheDirPath).apply { mkdirs() }
        val attempt = UUID.randomUUID().toString()
        val output = File(directory, "export_$attempt.zip")
        try {
            ZipOutputStream(output.outputStream().buffered()).use { archive ->
                for (id in identities) {
                    currentCoroutineContext().ensureActive()
                    val item = galleryReads.metadata(listOf(id)).first()
                    val encoded =
                        galleryReads.image(id, item.imageRevision, false, budget.encodedChars)
                            ?: throw IllegalStateException()
                    val image = originals.export(encoded, "${attempt}_$id")
                    try {
                        archive.putNextEntry(ZipEntry("$id.${image.extension}"))
                        image.inputStream().use { input ->
                            val buffer = ByteArray(8192)
                            while (true) {
                                currentCoroutineContext().ensureActive()
                                val count = input.read(buffer)
                                if (count < 0) break
                                archive.write(buffer, 0, count)
                            }
                        }
                        archive.closeEntry()
                    } finally {
                        image.delete()
                    }
                    val metadata = JSONObject().put("id", item.id).put("created_at", item.createdAt)
                        .put("device_name", item.deviceName ?: JSONObject.NULL)
                        .put("os_version", item.osVersion ?: JSONObject.NULL)
                        .put("provider_key", item.providerKey ?: JSONObject.NULL)
                        .put("acceleration", item.acceleration ?: JSONObject.NULL)
                        .put(
                            "generation_duration_millis",
                            item.generationDurationMillis ?: JSONObject.NULL
                        )
                        .put(
                            "generation_duration",
                            item.generationDurationMillis?.let(::formatGenerationDuration)
                                ?: JSONObject.NULL
                        )
                        .put("generation_type", item.type.key).put("model_name", item.modelName)
                        .put("prompt", item.prompt).put("negative_prompt", item.negativePrompt)
                        .put("width", item.width).put("height", item.height)
                        .put("sampling_steps", item.samplingSteps)
                        .put("cfg_scale", item.cfgScale.toDouble())
                        .put("restore_faces", item.shouldRestoreFaces)
                        .put("sampler", item.sampler).put("seed", item.seed)
                        .put("sub_seed", item.subSeed)
                        .put("sub_seed_strength", item.subSeedStrength.toDouble())
                        .put("denoising_strength", item.denoisingStrength.toDouble())
                        .put("hidden", item.isHidden).put("liked", item.isLiked)
                    archive.putNextEntry(ZipEntry("$id.json"))
                    archive.write(metadata.toString().toByteArray(Charsets.UTF_8))
                    archive.closeEntry()
                }
            }
            return GalleryExportResult(output.absolutePath)
        } catch (failure: Throwable) {
            output.delete()
            throw failure
        }
    }
}
