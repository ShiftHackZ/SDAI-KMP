package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.Serializable

/**
 * Application identity displayed with the Civitai image.
 *
 * @param name SDAI application name.
 * @param homepage Canonical SDAI homepage.
 * @author Dmitriy Moroz
 */
@Serializable
data class CivitaiExternalSource(
    val name: String,
    val homepage: String,
)
