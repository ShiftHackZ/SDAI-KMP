package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.Serializable

/**
 * Generation fields accepted by Civitai image metadata.
 *
 * @param prompt Positive prompt.
 * @param negativePrompt Negative prompt.
 * @param steps Sampling steps.
 * @param cfgScale Guidance scale.
 * @param sampler Sampling algorithm.
 * @param seed Resolved numeric seed, absent when unknown or nonnumeric.
 * @param external SDAI source and captured provider/device details.
 * @author Dmitriy Moroz
 */
@Serializable
data class CivitaiImageMetadata(
    val prompt: String,
    val negativePrompt: String,
    val steps: Int,
    val cfgScale: Float,
    val sampler: String,
    val seed: Long? = null,
    val external: CivitaiExternalMetadata,
)
