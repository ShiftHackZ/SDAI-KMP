package com.shifthackz.aisdv1.network.model

import kotlinx.serialization.Serializable

/** Server-defined model identifier; folder separators must survive mapping unchanged.
 * @param model Complete checkpoint identifier including any folder separators.
 * @param name Human-readable model label; the exact identifier is also shown.
 * @param tags Server model classifications, including stable-diffusion for checkpoints.
 * @author Dmitriy Moroz */
@Serializable
data class EasyDiffusionModelRaw(val model: String, val name: String, val tags: List<String>)
