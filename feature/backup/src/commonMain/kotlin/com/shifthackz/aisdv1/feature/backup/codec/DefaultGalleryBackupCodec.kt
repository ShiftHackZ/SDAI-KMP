package com.shifthackz.aisdv1.feature.backup.codec

import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity
import com.shifthackz.aisdv1.domain.gateway.backup.BackupImageValidator
import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.Base64.PaddingOption
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Encodes one gallery row at a time without loading the whole gallery.
 * @param archive Length-prefixed metadata codec.
 * @param json Forward-compatible typed metadata serializer.
 * @param imageValidator Budgeted native probe and sampled content validator.
 * @param budget Shared encoded-record admission applied before allocating encoded image buffers.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalEncodingApi::class)
internal class DefaultGalleryBackupCodec(
    private val archive: BackupArchiveCodec,
    private val json: Json,
    private val imageValidator: BackupImageValidator,
    private val budget: BackupImageBudget,
) : GalleryBackupCodec {
    override fun write(sink: BufferedSink, row: GenerationResultEntity) {
        val record = GalleryBackupRecord(
            createdAt = row.createdAt,
            generationType = row.generationType,
            prompt = row.prompt,
            negativePrompt = row.negativePrompt,
            width = row.width,
            height = row.height,
            samplingSteps = row.samplingSteps,
            cfgScale = row.cfgScale,
            isRestoreFaces = row.restoreFaces,
            sampler = row.sampler,
            seed = row.seed,
            subSeed = row.subSeed,
            subSeedStrength = row.subSeedStrength,
            denoisingStrength = row.denoisingStrength,
            isHidden = row.hidden,
            isLiked = row.liked,
            modelName = row.modelName,
        )
        archive.writeJson(sink, json.encodeToString(record))
        val outputBytes = writeImage(sink, row.imageBase64, false, budget.maxEncodedBytes)
        writeImage(sink, row.originalImageBase64, true, budget.maxEncodedBytes - outputBytes)
    }

    override fun read(source: BufferedSource, revision: String): GenerationResultEntity? {
        val record = json.decodeFromString<GalleryBackupRecord>(archive.readJson(source))
        val (image, outputBytes) = readImage(source, false, budget.maxEncodedBytes)
        val (original, _) = readImage(source, true, budget.maxEncodedBytes - outputBytes)
        if (record.generationType !in setOf("txt2img", "img2img")) return null
        require(record.width > 0 && record.height > 0 && record.samplingSteps >= 0 && record.cfgScale.isFinite()) { "Invalid gallery metadata" }
        require(record.subSeedStrength.isFinite() && record.denoisingStrength.isFinite()) { "Invalid gallery metadata" }
        return GenerationResultEntity(
            id = 0,
            imageBase64 = image,
            originalImageBase64 = original,
            imageRevision = revision,
            createdAt = record.createdAt,
            generationType = record.generationType,
            prompt = record.prompt,
            negativePrompt = record.negativePrompt,
            width = record.width,
            height = record.height,
            samplingSteps = record.samplingSteps,
            cfgScale = record.cfgScale,
            restoreFaces = record.isRestoreFaces,
            sampler = record.sampler,
            seed = record.seed,
            subSeed = record.subSeed,
            subSeedStrength = record.subSeedStrength,
            denoisingStrength = record.denoisingStrength,
            hidden = record.isHidden,
            liked = record.isLiked,
            modelName = record.modelName,
        )
    }

    /**
     * Admits encoded text before allocating binary bytes and emits one validated image frame.
     * @param sink Current gallery section writer.
     * @param value Saved Base64 image text, including supported alternate alphabet or whitespace.
     * @param canBeEmpty Whether this is the optional original input image.
     * @param remainingBytes Encoded-byte allowance left in this output/input record.
     * @author Dmitriy Moroz
     */
    private fun writeImage(
        sink: BufferedSink,
        value: String,
        canBeEmpty: Boolean,
        remainingBytes: Int
    ): Int {
        // Admit the Base64 text before allocating its decoded byte array or a normalized copy.
        val maxChars = ((remainingBytes.toLong() + 2) / 3) * 4
        require(value.length.toLong() <= maxChars) { "Backup image exceeds encoded record budget" }
        val normalized =
            if (value.any { it.isWhitespace() || it == '-' || it == '_' }) buildString(value.length) {
                value.forEach { character ->
                    if (!character.isWhitespace()) append(
                        when (character) {
                            '-' -> '+'; '_' -> '/'; else -> character
                        }
                    )
                }
            } else value
        val bytes =
            if (normalized.isEmpty()) byteArrayOf() else Base64.Default.withPadding(PaddingOption.PRESENT_OPTIONAL)
                .decode(normalized)
        require(bytes.size <= remainingBytes) { "Backup image exceeds encoded record budget" }
        validateImage(bytes, canBeEmpty)
        sink.writeIntLe(bytes.size)
        sink.write(bytes)
        return bytes.size
    }

    /**
     * Checks the frame length before allocation and returns Base64 text with its consumed binary byte count.
     * @param source Bounded gallery section reader.
     * @param canBeEmpty Whether the optional original input may be absent.
     * @param remainingBytes Encoded-byte allowance left after preceding image frames.
     * @author Dmitriy Moroz
     */
    private fun readImage(
        source: BufferedSource,
        canBeEmpty: Boolean,
        remainingBytes: Int
    ): Pair<String, Int> {
        val count = source.readIntLe()
        require(count in 0..remainingBytes) { "Backup image exceeds encoded record budget" }
        val bytes = source.readByteArray(count.toLong())
        validateImage(bytes, canBeEmpty)
        return Base64.encode(bytes) to count
    }

    /**
     * Requires supported encoded formats and native budgeted content validation before accepting a frame.
     * @param bytes Already-admitted encoded image bytes.
     * @param canBeEmpty Whether a missing original input is allowed.
     * @author Dmitriy Moroz
     */
    private fun validateImage(bytes: ByteArray, canBeEmpty: Boolean) {
        if (bytes.isEmpty() && canBeEmpty) return
        require(bytes.size in 12..budget.maxEncodedBytes) { "Invalid image payload" }
        val isPng = bytes.take(8) == listOf(137, 80, 78, 71, 13, 10, 26, 10).map(Int::toByte)
        val isJpeg =
            bytes[0] == 0xff.toByte() && bytes[1] == 0xd8.toByte() && bytes[2] == 0xff.toByte()
        val isWebp = bytes.copyOfRange(0, 4).decodeToString() == "RIFF" && bytes.copyOfRange(8, 12)
            .decodeToString() == "WEBP"
        require(isPng || isJpeg || isWebp) { "Unsupported image payload" }
        imageValidator(bytes)
    }

}
