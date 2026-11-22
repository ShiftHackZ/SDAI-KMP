package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Known SDAI external metadata, limited to ten documented properties.
 *
 * @param provider Recorded generation provider.
 * @param model Recorded model, absent when unknown.
 * @param device Originating device, absent for legacy images.
 * @param platform Originating platform and OS, absent for legacy images.
 * @param acceleration Captured execution mode.
 * @param durationMillis Recorded elapsed generation milliseconds.
 * @param type Generation operation discriminator.
 * @param shouldRestoreFaces Whether face restoration was requested.
 * @param variationSeed Recorded variation seed.
 * @param denoisingStrength Recorded image-to-image denoising strength.
 * @author Dmitriy Moroz
 */
@Serializable
data class CivitaiExternalDetails(
    @SerialName("Provider") val provider: String? = null,
    @SerialName("Model") val model: String? = null,
    @SerialName("Device") val device: String? = null,
    @SerialName("Platform / OS") val platform: String? = null,
    @SerialName("Acceleration") val acceleration: String? = null,
    @SerialName("Duration (ms)") val durationMillis: Long? = null,
    @SerialName("Type") val type: String,
    @SerialName("Restore faces") val shouldRestoreFaces: Boolean,
    @SerialName("Variation seed") val variationSeed: String,
    @SerialName("Denoising strength") val denoisingStrength: Float,
)
