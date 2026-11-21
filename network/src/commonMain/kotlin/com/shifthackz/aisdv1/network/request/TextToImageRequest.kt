package com.shifthackz.aisdv1.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * AUTOMATIC1111 text generation parameters, including a client identity for progress correlation.
 *
 * @author Dmitriy Moroz
 */
@Serializable
data class TextToImageRequest(
    /**
     * Client task identity echoed by request-correlated progress on supported servers.
     * @author Dmitriy Moroz
     */
    @SerialName("force_task_id") val forceTaskId: String? = null,
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
    /**
     * Whether the server performs a second high-resolution sampling pass.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("enable_hr")
    val enableHr: Boolean? = null,
    /**
     * Server upscaler identifier for the high-resolution pass.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("hr_upscaler")
    val hrUpscaler: String? = null,
    /**
     * Scale factor for the high-resolution pass.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("hr_scale")
    val hrScale: Float? = null,
    /**
     * Iteration count for the high-resolution sampling pass.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("hr_second_pass_steps")
    val hrSecondPassSteps: Int? = null,
    /**
     * Guidance scale for the high-resolution sampling pass.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("hr_cfg")
    val hrCfg: Float? = null,
    /**
     * Distilled guidance scale for the high-resolution sampling pass.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("hr_distilled_cfg")
    val hrDistilledCfg: Float? = null,
    /**
     * Amount of input image noise replaced during image-to-image sampling.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("denoising_strength")
    val denoisingStrength: Float? = null,
    /**
     * Per-request server settings applied for this generation.
     *
     * @author Dmitriy Moroz
     */
    @SerialName("override_settings")
    val overrideSettings: OverrideSettings? = null,
)
