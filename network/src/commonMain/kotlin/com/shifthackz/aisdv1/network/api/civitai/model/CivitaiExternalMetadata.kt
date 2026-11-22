package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.Serializable

/**
 * External application attribution supported by Civitai.
 *
 * @param source Name and canonical app homepage.
 * @param details At most ten known captured generation details.
 * @author Dmitriy Moroz
 */
@Serializable
data class CivitaiExternalMetadata(
    val source: CivitaiExternalSource,
    val details: CivitaiExternalDetails,
)
