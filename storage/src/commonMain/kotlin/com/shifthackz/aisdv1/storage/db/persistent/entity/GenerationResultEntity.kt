package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifthackz.aisdv1.storage.db.persistent.contract.GenerationResultContract

/**
 * Stores original image payloads, generation parameters and provenance in the gallery.
 *
 * @param id Persistent gallery identity, or zero before insertion.
 * @param imageBase64 Original encoded output image as Base64.
 * @param originalImageBase64 Optional encoded input as Base64, empty for text generation.
 * @param createdAt Creation timestamp in epoch milliseconds.
 * @param generationType Stable txt2img/img2img operation key.
 * @param prompt Positive generation prompt.
 * @param negativePrompt Negative generation prompt.
 * @param width Output width in pixels.
 * @param height Output height in pixels.
 * @param samplingSteps Requested sampling iteration count.
 * @param cfgScale Classifier-free guidance scale.
 * @param restoreFaces Whether face restoration was requested.
 * @param sampler Sampling algorithm identifier.
 * @param seed Resolved provider seed text.
 * @param subSeed Variation seed text.
 * @param subSeedStrength Variation seed influence.
 * @param denoisingStrength Image-to-image denoising influence.
 * @param hidden Whether the output is obscured in the gallery.
 * @param liked Whether the output is a favorite.
 * @param modelName Recorded model name, independent of currently selected model.
 * @param imageRevision Content identity used to invalidate bounded preview caches.
 * @param providerKey Captured provider catalog key, or null for legacy images.
 * @param acceleration Captured execution mode, or null when unknown.
 * @param deviceName Originating device vendor and model, retained across transfers; null for legacy images.
 * @param osVersion Originating OS version, including Android API level; null for legacy images.
 * @param generationDurationMillis Nonnegative elapsed generation time before persistence/export, or null for legacy images.
 * @author Dmitriy Moroz
 */
@Entity(
    tableName = GenerationResultContract.TABLE,
    indices = [
        Index(
            value = [GenerationResultContract.CREATED_AT],
            name = GenerationResultContract.CREATED_AT_INDEX,
        ),
    ],
)
data class GenerationResultEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = GenerationResultContract.ID)
    val id: Long,
    @ColumnInfo(name = GenerationResultContract.IMAGE_BASE_64)
    val imageBase64: String,
    @ColumnInfo(name = GenerationResultContract.ORIGINAL_IMAGE_BASE_64)
    val originalImageBase64: String,
    @ColumnInfo(name = GenerationResultContract.CREATED_AT)
    val createdAt: Long,
    @ColumnInfo(name = GenerationResultContract.GENERATION_TYPE)
    val generationType: String,
    @ColumnInfo(name = GenerationResultContract.PROMPT)
    val prompt: String,
    @ColumnInfo(name = GenerationResultContract.NEGATIVE_PROMPT)
    val negativePrompt: String,
    @ColumnInfo(name = GenerationResultContract.WIDTH)
    val width: Int,
    @ColumnInfo(name = GenerationResultContract.HEIGHT)
    val height: Int,
    @ColumnInfo(name = GenerationResultContract.SAMPLING_STEPS)
    val samplingSteps: Int,
    @ColumnInfo(name = GenerationResultContract.CFG_SCALE)
    val cfgScale: Float,
    @ColumnInfo(name = GenerationResultContract.RESTORE_FACES)
    val restoreFaces: Boolean,
    @ColumnInfo(name = GenerationResultContract.SAMPLER)
    val sampler: String,
    @ColumnInfo(name = GenerationResultContract.SEED)
    val seed: String,
    @ColumnInfo(name = GenerationResultContract.SUB_SEED, defaultValue = "")
    val subSeed: String,
    @ColumnInfo(name = GenerationResultContract.SUB_SEED_STRENGTH, defaultValue = "${0f}")
    val subSeedStrength: Float,
    @ColumnInfo(name = GenerationResultContract.DENOISING_STRENGTH, defaultValue = "${0f}")
    val denoisingStrength: Float,
    @ColumnInfo(name = GenerationResultContract.HIDDEN, defaultValue = "0")
    val hidden: Boolean,
    @ColumnInfo(name = GenerationResultContract.LIKED, defaultValue = "0")
    val liked: Boolean = false,
    @ColumnInfo(name = GenerationResultContract.MODEL_NAME, defaultValue = "")
    val modelName: String = "",
    /**
     * Content revision; legacy rows retain an empty value until replaced.
     *
     * @author Dmitriy Moroz
     */
    @ColumnInfo(name = GenerationResultContract.IMAGE_REVISION, defaultValue = "''")
    val imageRevision: String = "",
    /**
     * Recorded provider, absent for legacy rows.
     *
     * @author Dmitriy Moroz
     */
    @ColumnInfo(name = "provider_key") val providerKey: String? = null,
    /**
     * Recorded execution mode, absent when unavailable.
     *
     * @author Dmitriy Moroz
     */
    @ColumnInfo(name = "acceleration") val acceleration: String? = null,
    @ColumnInfo(name = "device_name") val deviceName: String? = null,
    @ColumnInfo(name = "os_version") val osVersion: String? = null,
    @ColumnInfo(name = "generation_duration_millis") val generationDurationMillis: Long? = null,
)
