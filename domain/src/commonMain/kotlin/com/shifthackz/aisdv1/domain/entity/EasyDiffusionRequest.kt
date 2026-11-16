package com.shifthackz.aisdv1.domain.entity

/**
 * Immutable supported generation controls captured before submitting any task.
 * @param url Captured HTTP(S) root or reverse-proxy prefix of the owning instance.
 * @param sessionId Unique operation UUID, shared only by this sequential image batch.
 * @param model Complete checkpoint identifier including any folder separators.
 * @param prompt Positive prompt captured before submission.
 * @param negativePrompt Negative prompt captured before submission.
 * @param width Requested output width in pixels, validated as a positive multiple of eight.
 * @param height Requested output height in pixels, validated as a positive multiple of eight.
 * @param steps Requested sampling step count.
 * @param cfgScale Requested classifier-free guidance scale.
 * @param seed Resolved request seed within the unsigned 32-bit range.
 * @param batchCount Number of sequential single-output tasks under the shared generation deadline.
 * @param inputImage Original img2img input as a data URL; null denotes txt2img.
 * @param strength Img2img denoising strength from zero through one.
 * @param selectionRevision Durable source revision captured before dispatch.
 * @author Dmitriy Moroz
 */
data class EasyDiffusionRequest(
    val url: String,
    val sessionId: String,
    val model: String,
    val prompt: String,
    val negativePrompt: String,
    val width: Int,
    val height: Int,
    val steps: Int,
    val cfgScale: Float,
    val seed: Long,
    val batchCount: Int,
    val inputImage: String? = null,
    val strength: Float = 0f,
    val selectionRevision: Long,
)
