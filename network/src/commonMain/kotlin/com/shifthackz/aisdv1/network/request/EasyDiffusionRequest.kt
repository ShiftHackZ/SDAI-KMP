package com.shifthackz.aisdv1.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supported v3 render request. Explicit PNG/DDIM and empty filters prevent server defaults from
 * silently enabling unsupported postprocessing; one output is submitted per sequential batch item.
 * @param prompt Positive prompt captured before submission.
 * @param negativePrompt Negative prompt captured before submission.
 * @param width Requested output width in pixels, validated as a positive multiple of eight.
 * @param height Requested output height in pixels, validated as a positive multiple of eight.
 * @param seed Resolved request seed within the unsigned 32-bit range.
 * @param steps Requested sampling step count.
 * @param cfgScale Requested classifier-free guidance scale.
 * @param model Complete checkpoint identifier including any folder separators.
 * @param sessionId Unique operation UUID, shared only by this sequential image batch.
 * @param initImage Data-URL encoded img2img input or null for txt2img.
 * @param strength Img2img denoising strength from zero through one.
 * @param outputs One output per task; larger batches are submitted sequentially.
 * @param sampler Explicit fixed DDIM sampler identifier.
 * @param format Explicit PNG output encoding.
 * @param shouldStreamImages Whether upstream emits preview images; always false in the supported request.
 * @param shouldShowFilteredOnly Avoids duplicate original-plus-filtered image arrays while filters remain empty.
 * @param savePath Null disables server-side image saving; SDAI owns its normal export path.
 * @param filters Empty postprocessing list; unsupported filters are never requested.
 * @author Dmitriy Moroz
 */
@Serializable
data class EasyDiffusionRequest(
    val prompt: String,
    @SerialName("negative_prompt") val negativePrompt: String,
    val width: Int,
    val height: Int,
    val seed: Long,
    @SerialName("num_inference_steps") val steps: Int,
    @SerialName("guidance_scale") val cfgScale: Float,
    @SerialName("use_stable_diffusion_model") val model: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("init_image") val initImage: String? = null,
    @SerialName("prompt_strength") val strength: Float = 0f,
    @SerialName("num_outputs") val outputs: Int = 1,
    @SerialName("sampler_name") val sampler: String = "ddim",
    @SerialName("output_format") val format: String = "png",
    @SerialName("stream_image_progress") val shouldStreamImages: Boolean = false,
    @SerialName("show_only_filtered_image") val shouldShowFilteredOnly: Boolean = true,
    @SerialName("save_to_disk_path") val savePath: String? = null,
    val filters: List<String> = emptyList(),
)
