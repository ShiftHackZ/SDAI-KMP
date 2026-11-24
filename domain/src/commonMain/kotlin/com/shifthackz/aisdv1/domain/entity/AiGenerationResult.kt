package com.shifthackz.aisdv1.domain.entity

/**
 * Generated image, metadata and transient guards used by gallery persistence.
 *
 * @param id Persistent gallery identity, or zero before insertion.
 * @param image Original encoded output image as Base64.
 * @param inputImage Optional encoded source image as Base64, empty for text generation.
 * @param createdAt Creation timestamp in epoch milliseconds.
 * @param type Text-to-image, image-to-image or standalone upscale operation.
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
 * @param openAiOAuthIdentity Original OAuth account/revision for an unsaved result; not persisted in gallery rows.
 * @param providerSelectionRevision Source-selection revision captured during OAuth generation; null for other providers.
 * @param imageRevision Content identity used to invalidate cached gallery previews.
 * @param providerKey Captured provider catalog key; null for images created before provenance was recorded.
 * @param acceleration Execution mode used for this image; null when unknown, "None" for remote providers.
 * @param deviceName Originating device vendor and model, retained across transfers; null for legacy images.
 * @param osVersion Originating OS version, including Android API level; null for legacy images.
 * @param generationDurationMillis Elapsed generation milliseconds, including provider setup and queueing, excluding gallery/export writes; null for legacy images.
 * @param civitaiUrl Public Civitai post URL after publication, absent for unpublished images.
 * @param upscalerModelName Dedicated super-resolution model used after generation, absent for original output.
 * @author Dmitriy Moroz
 */
data class AiGenerationResult(
    val id: Long,
    val image: String,
    val inputImage: String,
    val createdAt: Long,
    val type: Type,
    val prompt: String,
    val negativePrompt: String,
    val width: Int,
    val height: Int,
    val samplingSteps: Int,
    val cfgScale: Float,
    val restoreFaces: Boolean,
    val sampler: String,
    val seed: String,
    val subSeed: String,
    val subSeedStrength: Float,
    val denoisingStrength: Float,
    val hidden: Boolean,
    val liked: Boolean = false,
    val modelName: String = "",
    val openAiOAuthIdentity: OpenAiOAuthIdentity? = null,
    val providerSelectionRevision: Long? = null,
    val imageRevision: String = "",
    val providerKey: String? = null,
    val acceleration: String? = null,
    val deviceName: String? = null,
    val osVersion: String? = null,
    val generationDurationMillis: Long? = null,
    val civitaiUrl: String? = null,
    val upscalerModelName: String? = null,
) {
    /**
     * Stable gallery operation discriminator.
     *
     * @param key Portable operation identifier.
     * @author Dmitriy Moroz
     */
    enum class Type(val key: String) {
        UPSCALE("upscale"),
        TEXT_TO_IMAGE("txt2img"),
        IMAGE_TO_IMAGE("img2img");

        /**
         * Resolves stored operation identifiers for older gallery rows.
         *
         * @author Dmitriy Moroz
         */
        companion object {
            /**
             * Resolves a stored operation key, using text-to-image for legacy unknown values.
             *
             * @param input Persisted operation identifier, possibly absent in old records.
             * @author Dmitriy Moroz
             */
            fun parse(input: String?) = entries
                .find { it.key == input } ?: TEXT_TO_IMAGE
        }
    }
}
