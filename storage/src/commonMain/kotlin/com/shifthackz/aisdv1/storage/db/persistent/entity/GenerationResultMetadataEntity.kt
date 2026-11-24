package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import com.shifthackz.aisdv1.storage.db.persistent.contract.GenerationResultContract

/**
 * Gallery parameter projection with no output or input payload columns.
 *
 * @param id Persisted gallery identity.
 * @param imageRevision Opaque image content identity, independent of flags.
 * @param createdAt Creation time in epoch milliseconds.
 * @param generationType Generation operation that determines available tabs.
 * @param prompt Positive generation prompt.
 * @param negativePrompt Negative generation prompt.
 * @param width Original output width in pixels.
 * @param height Original output height in pixels.
 * @param samplingSteps Generation sampling step count.
 * @param cfgScale Classifier-free guidance scale.
 * @param shouldRestoreFaces Whether face restoration was requested.
 * @param sampler Selected sampling algorithm.
 * @param seed Provider seed text.
 * @param subSeed Variation seed text.
 * @param subSeedStrength Variation seed strength.
 * @param denoisingStrength Image-to-image denoising strength.
 * @param isHidden Whether grid/detail output is obscured.
 * @param isLiked Whether the gallery result is liked.
 * @param modelName Recorded model display name.
 * @param providerKey Captured provider catalog key; null for images created before provenance was recorded.
 * @param acceleration Execution mode used for this image; null when unknown, "None" for remote providers.
 * @param deviceName Originating device vendor and model, retained across transfers; null for legacy images.
 * @param osVersion Originating OS version, including Android API level; null for legacy images.
 * @param generationDurationMillis Elapsed generation milliseconds, including provider setup and queueing, excluding gallery/export writes; null for legacy images.
 * @param upscalerModelName Dedicated postprocessing checkpoint, absent for unmodified images.
 * @param civitaiUrl Public post URL after a successful Civitai publication; null for unpublished images.
 * @author Dmitriy Moroz
 */
data class GenerationResultMetadataEntity(
    @ColumnInfo(name = GenerationResultContract.ID)
    val id: Long,
    @ColumnInfo(name = GenerationResultContract.IMAGE_REVISION)
    val imageRevision: String,
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
    val shouldRestoreFaces: Boolean,
    @ColumnInfo(name = GenerationResultContract.SAMPLER)
    val sampler: String,
    @ColumnInfo(name = GenerationResultContract.SEED)
    val seed: String,
    @ColumnInfo(name = GenerationResultContract.SUB_SEED)
    val subSeed: String,
    @ColumnInfo(name = GenerationResultContract.SUB_SEED_STRENGTH)
    val subSeedStrength: Float,
    @ColumnInfo(name = GenerationResultContract.DENOISING_STRENGTH)
    val denoisingStrength: Float,
    @ColumnInfo(name = GenerationResultContract.HIDDEN)
    val isHidden: Boolean,
    @ColumnInfo(name = GenerationResultContract.LIKED)
    val isLiked: Boolean,
    @ColumnInfo(name = GenerationResultContract.MODEL_NAME)
    val modelName: String,
    @ColumnInfo(name = GenerationResultContract.PROVIDER_KEY)
    val providerKey: String? = null,
    @ColumnInfo(name = GenerationResultContract.ACCELERATION)
    val acceleration: String? = null,
    @ColumnInfo(name = GenerationResultContract.DEVICE_NAME)
    val deviceName: String? = null,
    @ColumnInfo(name = GenerationResultContract.OS_VERSION)
    val osVersion: String? = null,
    @ColumnInfo(name = GenerationResultContract.GENERATION_DURATION_MILLIS)
    val generationDurationMillis: Long? = null,
    @ColumnInfo(name = GenerationResultContract.CIVITAI_URL)
    val civitaiUrl: String? = null,
    @ColumnInfo(name = GenerationResultContract.UPSCALER_MODEL_NAME)
    val upscalerModelName: String? = null,
)
