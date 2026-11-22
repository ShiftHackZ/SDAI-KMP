package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.Serializable

/**
 * An uploaded object attached to one post.
 *
 * @param url Upload UUID, not an HTTP URL.
 * @param index Zero-based image position.
 * @param type Media discriminator, image for SDAI output.
 * @param width Recorded output width in pixels.
 * @param height Recorded output height in pixels.
 * @param meta Generation parameters and external app attribution.
 * @author Dmitriy Moroz
 */
@Serializable
data class CivitaiPostImage(
    val url: String,
    val index: Int,
    val type: String,
    val width: Int,
    val height: Int,
    val meta: CivitaiImageMetadata,
)
