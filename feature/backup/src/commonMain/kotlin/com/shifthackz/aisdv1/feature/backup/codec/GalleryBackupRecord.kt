package com.shifthackz.aisdv1.feature.backup.codec

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Portable image metadata. Images are separate binary frames; local IDs, revisions and transient guards are never serialized.
 *
 * @param createdAt Original creation time in epoch milliseconds.
 * @param generationType Stable txt2img/img2img discriminator; future values are skipped.
 * @param prompt Original positive generation prompt.
 * @param negativePrompt Original negative generation prompt.
 * @param width Positive output width in pixels.
 * @param height Positive output height in pixels.
 * @param samplingSteps Recorded sampling steps, including zero for providers that do not expose a sampler.
 * @param cfgScale Finite guidance scale recorded by generation.
 * @param isRestoreFaces Whether face restoration was requested.
 * @param sampler Recorded sampler identifier; an empty value is preserved.
 * @param seed Original textual seed without numeric reinterpretation.
 * @param subSeed Original textual variation seed.
 * @param subSeedStrength Finite variation strength recorded by generation.
 * @param denoisingStrength Finite image-to-image denoising strength.
 * @param isHidden Whether the history item is hidden from the gallery.
 * @param isLiked Whether the history item is marked as a favorite.
 * @param modelName Original generation model name without requiring that model on the destination.
 * @param providerKey Captured provider catalog key; null for images created before provenance was recorded.
 * @param acceleration Execution mode used for this image; null when unknown, "None" for remote providers.
 * @param deviceName Originating device vendor and model, retained across transfers; null for legacy images.
 * @param osVersion Originating OS version, including Android API level; null for legacy images.
 * @param generationDurationMillis Elapsed generation milliseconds, including provider setup and queueing, excluding gallery/export writes; null for legacy images.
 * @author Dmitriy Moroz
 */
@Serializable
internal data class GalleryBackupRecord(
    val createdAt: Long = 0,
    val generationType: String = "txt2img",
    val prompt: String = "",
    val negativePrompt: String = "",
    val width: Int = 512,
    val height: Int = 512,
    val samplingSteps: Int = 20,
    val cfgScale: Float = 7f,
    @SerialName("restoreFaces") val isRestoreFaces: Boolean = false,
    val sampler: String = "",
    val seed: String = "",
    val subSeed: String = "",
    val subSeedStrength: Float = 0f,
    val denoisingStrength: Float = 0f,
    @SerialName("hidden") val isHidden: Boolean = false,
    @SerialName("liked") val isLiked: Boolean = false,
    val modelName: String = "",
    val providerKey: String? = null,
    val acceleration: String? = null,
    val deviceName: String? = null,
    val osVersion: String? = null,
    val generationDurationMillis: Long? = null,
)
