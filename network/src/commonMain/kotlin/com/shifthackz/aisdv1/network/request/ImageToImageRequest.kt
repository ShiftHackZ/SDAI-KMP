package com.shifthackz.aisdv1.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * AUTOMATIC1111 image transformation parameters, including a client identity for progress correlation.
 *
 * @author Dmitriy Moroz
 */
@Serializable
data class ImageToImageRequest(
    /**
     * Client task identity echoed by request-correlated progress on supported servers.
     * @author Dmitriy Moroz
     */
    @SerialName("force_task_id") val forceTaskId: String? = null,
    /**
     * Base64 input images for the image-to-image request.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("init_images")
    val initImages: List<String>,
    /**
     * Whether the response includes the original input images.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("include_init_images")
    val includeInitImages: Boolean,
    /**
     * Optional base64 inpainting mask.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("mask")
    val mask: String?,
    /**
     * Mask inversion mode interpreted by the server.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("inpainting_mask_invert")
    val inPaintingMaskInvert: Int?,
    /**
     * Extra pixels surrounding the selected inpainting region.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("inpaint_full_res_padding")
    val inPaintFullResPadding: Int?,
    /**
     * Server inpainting fill mode.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("inpainting_fill")
    val inPaintingFill: Int?,
    /**
     * Whether inpainting uses the full-resolution masked region.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("inpaint_full_res")
    val inPaintFullRes: Boolean?,
    /**
     * Blur radius applied to the inpainting mask.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("mask_blur")
    val maskBlur: Int?,
    /**
     * Amount of input image noise replaced during image-to-image sampling.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("denoising_strength")
    val denoisingStrength: Float,
    /**
     * Positive generation prompt sent to the provider.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("prompt")
    val prompt: String,
    /**
     * Negative prompt describing content to avoid.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("negative_prompt")
    val negativePrompt: String,
    /**
     * Requested denoising iteration count.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("steps")
    val steps: Int,
    /**
     * Classifier-free guidance scale.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("cfg_scale")
    val cfgScale: Float,
    /**
     * Output image width in pixels.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("width")
    val width: Int,
    /**
     * Output image height in pixels.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("height")
    val height: Int,
    /**
     * Number of images generated concurrently in the server batch.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("batch_size")
    val batchSize: Int,
    /**
     * Whether the server applies face restoration after sampling.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("restore_faces")
    val restoreFaces: Boolean,
    /**
     * Primary random seed in the provider request format.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("seed")
    val seed: String?,
    /**
     * Variation seed in the provider request format.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("subseed")
    val subSeed: String?,
    /**
     * Blend strength between the primary and variation seeds.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("subseed_strength")
    val subSeedStrength: Float?,
    /**
     * Server sampler identifier retained for compatibility with older AUTOMATIC1111 versions.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("sampler_index")
    val samplerIndex: String,
    /**
     * Server noise scheduler identifier.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("scheduler")
    val scheduler: String? = null,
    /**
     * Extension inputs keyed by server script identifier.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("alwayson_scripts")
    val alwaysOnScripts: JsonObject? = null,
)
