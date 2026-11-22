package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Presigned object-upload destination returned by the Civitai site API.
 *
 * @param id Uploaded object UUID used by post creation.
 * @param uploadUrl Short-lived HTTPS object upload URL.
 * @author Dmitriy Moroz
 */
@Serializable
data class CivitaiUploadResponse(
    val id: String,
    @SerialName("uploadURL") val uploadUrl: String,
)
