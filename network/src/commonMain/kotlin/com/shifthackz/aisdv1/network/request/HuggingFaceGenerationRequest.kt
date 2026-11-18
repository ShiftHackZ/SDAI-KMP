package com.shifthackz.aisdv1.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON inference payload with text or base64 image input and task-specific optional parameters.
 * Null parameter values are omitted by the network JSON codec.
 *
 * @param inputs Prompt for text-to-image or base64 image data for image-to-image inference.
 * @param parameters Generation controls supported by the selected inference task and model.
 *
 * @author Dmitriy Moroz
 */
@Serializable
data class HuggingFaceGenerationRequest(
    @SerialName("inputs")
    val inputs: String,
    @SerialName("parameters")
    val parameters: Parameters = Parameters(),
) {
    /**
     * Optional inference controls; the image prompt is encoded as `prompt` and the seed as an integer.
     *
     * @param width Requested output width where supported by the model.
     * @param height Requested output height where supported by the model.
     * @param prompt Text guidance for image-to-image inference; omitted for text-to-image.
     * @param negativePrompt Describes content the model should avoid.
     * @param seed Nonnegative numeric seed, or null to let the provider choose one.
     * @param numInferenceSteps Number of denoising steps requested from the model.
     * @param guidanceScale Strength of the model's adherence to the prompt.
     *
     * @author Dmitriy Moroz
     */
    @Serializable
    data class Parameters(
        @SerialName("width")
        val width: Int? = null,
        @SerialName("height")
        val height: Int? = null,
        @SerialName("prompt")
        val prompt: String? = null,
        @SerialName("negative_prompt")
        val negativePrompt: String? = null,
        @SerialName("seed")
        val seed: Long? = null,
        @SerialName("num_inference_steps")
        val numInferenceSteps: Int? = null,
        @SerialName("guidance_scale")
        val guidanceScale: Float? = null,
    )
}
