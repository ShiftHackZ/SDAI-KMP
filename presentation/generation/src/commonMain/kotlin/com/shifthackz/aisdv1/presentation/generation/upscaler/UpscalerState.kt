package com.shifthackz.aisdv1.presentation.generation.upscaler

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import kotlin.time.TimeMark

/**
 * Standalone image and captured upscaler inputs retained while setup is open.
 *
 * @param platform Current host for provider artwork.
 * @param isSupported Whether a local upscaler is available on this platform.
 * @param image Original encoded pixels retained unchanged until an upscale is confirmed.
 * @param width Actual original pixel width.
 * @param height Actual original pixel height.
 * @param outputSize Predicted read-only output dimensions after fitting and the selected scale.
 * @param hasDownscaleWarning Whether the oversized-input confirmation is visible.
 * @param configuration Dedicated model captured from the shared form.
 * @param isPickingImage Whether native acquisition or image validation is active.
 * @param isRunning Whether an upscale/save operation owns the screen.
 * @param generationStartedAt Monotonic start retained through local inference stages.
 * @param screenModal Shared generation progress, error or final-result dialog.
 * @author Dmitriy Moroz
 */
data class UpscalerState(
    val platform: Platform,
    val isSupported: Boolean,
    val image: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val configuration: UpscalerConfiguration? = null,
    val hasDownscaleWarning: Boolean = false,
    val outputSize: UpscalerImageSize? = null,
    val isPickingImage: Boolean = false,
    val isRunning: Boolean = false,
    val generationStartedAt: TimeMark? = null,
    val screenModal: GenerationModal = GenerationModal.None,
) : MviState
