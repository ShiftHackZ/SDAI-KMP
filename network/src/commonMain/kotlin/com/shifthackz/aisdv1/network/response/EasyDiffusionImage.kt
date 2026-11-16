package com.shifthackz.aisdv1.network.response

import kotlinx.serialization.Serializable

/** Original encoded image data and the actual seed reported by the generation engine.
 * @param data Original encoded terminal image bytes, optionally data-URL prefixed.
 * @param seed Actual output seed reported by the server, within the unsigned 32-bit range.
 * @author Dmitriy Moroz
 */
@Serializable
data class EasyDiffusionImage(val data: String, val seed: Long)
