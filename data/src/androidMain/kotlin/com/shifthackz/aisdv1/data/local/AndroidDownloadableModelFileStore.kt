package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.domain.gateway.MnnModelStore

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import java.io.File

/**
 * Resolves provider model layouts and routes MNN deletion through its immutable-root lease owner.
 *
 * @param fileProviderDescriptor App-private root for downloaded model payloads.
 * @param mnnModels Immutable MNN roots and active attempt leases.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidDownloadableModelFileStore(
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val mnnModels: MnnModelStore,
) : DownloadableModelFileStore {

    /**
     * Checks the provider-specific complete model layout; custom legacy entries remain selectable before path validation.
     *
     * @param model Catalog or supported custom identity whose provider-specific owned layout is resolved.
     *
     * @author Dmitriy Moroz
     */
    override fun isDownloaded(model: LocalAiModel): Boolean {
        if (File(getLocalModelDirectory(model.id), ".download-in-progress").exists()) return false
        return try {
            when (model.id) {
                LocalAiModel.CustomMnn.id -> mnnModels.isDownloaded(model.id)


                LocalAiModel.CustomOnnx.id,
                LocalAiModel.CustomMediaPipe.id,
                LocalAiModel.CustomSdxl.id,
                LocalAiModel.CustomCoreMl.id,
                LocalAiModel.CustomBonsai.id,
                    -> true

                else -> when (model.type) {
                    LocalAiModel.Type.Mnn -> mnnModels.isDownloaded(model.id)
                    LocalAiModel.Type.ONNX -> {
                        val files = getLocalModelFiles(model.id).filter { it.isDirectory }
                        files.size == 4
                    }

                    LocalAiModel.Type.MediaPipe -> {
                        val files = getLocalModelFiles(model.id)
                        files.isNotEmpty()
                    }

                    LocalAiModel.Type.Sdxl -> {
                        val files = getLocalModelFiles(model.id)
                        files.isNotEmpty()
                    }

                    LocalAiModel.Type.CoreMl -> {
                        val files = getLocalModelFiles(model.id)
                        files.isNotEmpty()
                    }

                    LocalAiModel.Type.Bonsai -> {
                        getLocalModelDirectory(model.id).hasBonsaiModelLayout()
                    }
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resolves the app-accessible model root or the concrete single-file checkpoint expected by its runtime.
     *
     * @param model Catalog or supported custom identity whose provider-specific owned layout is resolved.
     *
     * @author Dmitriy Moroz
     */
    override fun resolvePath(model: LocalAiModel): String {
        if (model.type == LocalAiModel.Type.Mnn) return mnnModels.path(model.id)
        val directory = getLocalModelDirectory(model.id)
        if (model.type != LocalAiModel.Type.Sdxl) return directory.path

        return resolveSingleFilePath(directory.path)
    }

    /**
     * Selects the largest supported checkpoint in a directory, preserving the input when no checkpoint is found.
     *
     * @param path raw file or directory path used by the operation.
     *
     * @author Dmitriy Moroz
     */
    override fun resolveSingleFilePath(path: String): String {
        val file = File(path)
        if (file.isFile) return file.path

        return file
            .listFiles()
            ?.filter { file -> file.isFile && file.extension.lowercase() in singleFileModelExtensions }
            ?.maxByOrNull(File::length)
            ?.path
            ?: file.path
    }

    /**
     * Removes only the owned model payload; MNN removal waits for active inference leases.
     *
     * @param id identifier of the target entity.
     *
     * @author Dmitriy Moroz
     */
    override fun delete(id: String) {
        if (id.startsWith("mnn_") || id == LocalAiModel.CustomMnn.id) mnnModels.delete(id)
        else getLocalModelDirectory(id).deleteRecursively()
    }

    /**
     * Resolves the catalog model directory under the application-owned model root.
     *
     * @param id identifier of the target entity.
     *
     * @author Dmitriy Moroz
     */
    private fun getLocalModelDirectory(id: String): File {
        return File("${fileProviderDescriptor.localModelDirPath}/$id")
    }

    /**
     * Lists files immediately inside the owned model directory, returning an empty list when it is absent.
     *
     * @param id identifier of the target entity.
     *
     * @author Dmitriy Moroz
     */
    private fun getLocalModelFiles(id: String): List<File> {
        val localModelDir = getLocalModelDirectory(id)
        if (!localModelDir.exists()) return emptyList()
        return localModelDir.listFiles()?.toList() ?: emptyList()
    }

    private companion object {
        val singleFileModelExtensions = setOf("ckpt", "gguf", "safetensors")
    }
}

private const val TEXT_ENCODER_MLX_DIRECTORY = "text_encoder-mlx-4bit"
private const val TEXT_ENCODER_LEGACY_DIRECTORY = "text_encoder"
private const val TRANSFORMER_DIRECTORY = "transformer-packed-mflux"
private const val TOKENIZER_DIRECTORY = "tokenizer"
private const val VAE_DIRECTORY = "vae"
private const val SCHEDULER_DIRECTORY = "scheduler"
private const val RESOURCES_DIRECTORY = "Resources"
private const val EXTRACTED_DIRECTORY = "extracted"
private const val MAX_NESTED_SEARCH_DEPTH = 4

private fun File.hasBonsaiModelLayout(): Boolean {
    if (!exists()) return false

    directBonsaiCandidates()
        .any(File::isBonsaiRoot)
        .let { found -> if (found) return true }

    val rootDepth = toPath().nameCount
    return walkTopDown()
        .filter(File::isDirectory)
        .filter { candidate ->
            candidate.toPath().nameCount - rootDepth <= MAX_NESTED_SEARCH_DEPTH
        }
        .any(File::isBonsaiRoot)
}

private fun File.directBonsaiCandidates(): List<File> = listOf(
    this,
    File(this, RESOURCES_DIRECTORY),
    File(this, EXTRACTED_DIRECTORY),
    File(File(this, EXTRACTED_DIRECTORY), RESOURCES_DIRECTORY),
)

private fun File.isBonsaiRoot(): Boolean {
    val quantizationConfig = File(
        File(this, TRANSFORMER_DIRECTORY),
        "quantization_config.json",
    )
    val requiredDirectories = listOf(
        File(this, TOKENIZER_DIRECTORY),
        File(this, VAE_DIRECTORY),
        File(this, SCHEDULER_DIRECTORY),
    )

    return quantizationConfig.isFile &&
            listOf(TEXT_ENCODER_MLX_DIRECTORY, TEXT_ENCODER_LEGACY_DIRECTORY)
                .map { name -> File(this, name) }
                .any(File::isDirectory) &&
            requiredDirectories.all(File::isDirectory)
}
