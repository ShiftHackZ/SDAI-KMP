package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Atomic public image-post request.
 *
 * @param title Visible post title.
 * @param detail Escaped HTML attribution and generation details.
 * @param shouldPublish Whether to publish immediately.
 * @param images Ordered uploaded images and their metadata.
 * @author Dmitriy Moroz
 */
@Serializable
data class CivitaiPostRequest(
    val title: String,
    val detail: String,
    @SerialName("publish") val shouldPublish: Boolean,
    val images: List<CivitaiPostImage>,
)
