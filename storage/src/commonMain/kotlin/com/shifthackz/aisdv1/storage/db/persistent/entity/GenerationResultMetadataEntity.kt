package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.ColumnInfo

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
 * @author Dmitriy Moroz
 */
data class GenerationResultMetadataEntity(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "image_revision") val imageRevision: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "generation_type") val generationType: String,
    @ColumnInfo(name = "prompt") val prompt: String,
    @ColumnInfo(name = "negative_prompt") val negativePrompt: String,
    @ColumnInfo(name = "width") val width: Int,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "sampling_steps") val samplingSteps: Int,
    @ColumnInfo(name = "cfg_scale") val cfgScale: Float,
    @ColumnInfo(name = "restore_faces") val shouldRestoreFaces: Boolean,
    @ColumnInfo(name = "sampler") val sampler: String,
    @ColumnInfo(name = "seed") val seed: String,
    @ColumnInfo(name = "sub_seed") val subSeed: String,
    @ColumnInfo(name = "sub_seed_strength") val subSeedStrength: Float,
    @ColumnInfo(name = "denoising_strength") val denoisingStrength: Float,
    @ColumnInfo(name = "hidden") val isHidden: Boolean,
    @ColumnInfo(name = "liked") val isLiked: Boolean,
    @ColumnInfo(name = "model_name") val modelName: String,
    @ColumnInfo(name = "provider_key") val providerKey: String? = null,
    @ColumnInfo(name = "acceleration") val acceleration: String? = null,
    @ColumnInfo(name = "device_name") val deviceName: String? = null,
    @ColumnInfo(name = "os_version") val osVersion: String? = null,
    @ColumnInfo(name = "generation_duration_millis") val generationDurationMillis: Long? = null,
)
