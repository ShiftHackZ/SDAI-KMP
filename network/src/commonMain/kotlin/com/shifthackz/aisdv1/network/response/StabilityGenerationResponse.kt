package com.shifthackz.aisdv1.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON generation response containing the provider's completed or filtered image artifacts.
 *
 * @param artifacts Returned images and completion metadata, absent when the response has no artifacts.
 *
 * @author Dmitriy Moroz
 */
@Serializable
data class StabilityGenerationResponse(
    @SerialName("artifacts")
    val artifacts: List<Artifact>? = null,
) {

    /**
     * Generated image bytes and provider completion metadata.
     *
     * @param base64 Image content encoded as base64 when generation produced an image.
     * @param finishReason Provider status such as success, error or content filtering.
     * @param seed Numeric seed metadata returned by the provider's number-typed response contract.
     *
     * @author Dmitriy Moroz
     */
    @Serializable
    data class Artifact(
        @SerialName("base64")
        val base64: String? = null,
        @SerialName("finishReason")
        val finishReason: String? = null,
        @SerialName("seed")
        val seed: Double? = null,
    )
}
