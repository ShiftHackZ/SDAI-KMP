package com.shifthackz.aisdv1.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Returned task metadata used to record the actual checkpoint; auxiliary engine fields are ignored.
 * @param model Complete checkpoint identifiers reported as either one string or an ordered string list.
 * @author Dmitriy Moroz */
@Serializable
data class EasyDiffusionTaskMetadata(
    @Serializable(with = EasyDiffusionCheckpointListSerializer::class)
    @SerialName("use_stable_diffusion_model") val model: List<String>? = null,
)
