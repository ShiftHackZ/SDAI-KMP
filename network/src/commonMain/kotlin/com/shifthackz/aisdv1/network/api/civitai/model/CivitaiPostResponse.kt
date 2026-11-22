package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.Serializable

/**
 * Identity returned by the atomic create-with-images mutation.
 *
 * @param id Created Civitai post identity.
 * @author Dmitriy Moroz
 */
@Serializable
data class CivitaiPostResponse(
    val id: Long,
)
