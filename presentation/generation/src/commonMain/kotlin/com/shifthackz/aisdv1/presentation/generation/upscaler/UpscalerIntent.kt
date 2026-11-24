package com.shifthackz.aisdv1.presentation.generation.upscaler

import com.shifthackz.aisdv1.core.mvi.MviIntent
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImagePickSource

/**
 * User actions submitted to the standalone upscaler.
 *
 * @author Dmitriy Moroz
 */
sealed interface UpscalerIntent : MviIntent {
    data object DismissModal : UpscalerIntent
    data object OpenDrawer : UpscalerIntent
    data object Configure : UpscalerIntent
    data object Run : UpscalerIntent

    /**
     * Accepts proportional downscaling of the currently selected oversized image before inference.
     *
     * @author Dmitriy Moroz
     */
    data object ConfirmDownscale : UpscalerIntent
    data object Cancel : UpscalerIntent
    data object RandomImage : UpscalerIntent
    data object ClearImage : UpscalerIntent

    /**
     * Requests device photo selection or a new camera capture.
     *
     * @param source Requested native image acquisition route.
     * @author Dmitriy Moroz
     */
    data class PickImage(val source: ImageToImagePickSource) : UpscalerIntent

    /**
     * Uses original image bytes selected from the in-app Gallery as upscale input.
     *
     * @param image Original encoded image selected from saved generations.
     * @author Dmitriy Moroz
     */
    data class SelectImage(val image: String) : UpscalerIntent

    /**
     * Changes the standalone model selection independently of the optional generation stages.
     *
     * @param configuration Captured dedicated model selection.
     * @author Dmitriy Moroz
     */
    data class ConfigureModel(val configuration: UpscalerConfiguration?) : UpscalerIntent

    /**
     * Persists selected completed images using the normal manual-save flow.
     *
     * @param results Complete images offered by the shared result dialog.
     * @author Dmitriy Moroz
     */
    data class SaveResults(val results: List<AiGenerationResult>) : UpscalerIntent

    /**
     * Opens the shared preview, retaining unsaved pixels in the normal temporary cache.
     *
     * @param result Completed result selected in the shared modal.
     * @author Dmitriy Moroz
     */
    data class ViewResult(val result: AiGenerationResult) : UpscalerIntent

    /**
     * Opens the existing reporting flow for a completed result.
     *
     * @param result Completed result selected in the shared modal.
     * @author Dmitriy Moroz
     */
    data class ReportResult(val result: AiGenerationResult) : UpscalerIntent
}
