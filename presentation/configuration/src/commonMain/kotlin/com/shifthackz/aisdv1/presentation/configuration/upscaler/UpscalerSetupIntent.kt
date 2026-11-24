package com.shifthackz.aisdv1.presentation.configuration.upscaler

import com.shifthackz.aisdv1.core.mvi.MviIntent
import com.shifthackz.aisdv1.domain.entity.UpscalerSource

/**
 * Actions owned by the independent upscaler configuration draft.
 *
 * @author Dmitriy Moroz
 */
internal sealed interface UpscalerSetupIntent : MviIntent {
    /**
     * Changes the draft provider without committing model preferences.
     *
     * @param source Provider selected from the host's available choices.
     * @author Dmitriy Moroz
     */
    data class SelectSource(val source: UpscalerSource) : UpscalerSetupIntent

    /**
     * Replaces the provider discovery draft independently of saved configuration.
     *
     * @param discovery Updated query, filters, ordering and sheet visibility.
     * @author Dmitriy Moroz
     */
    data class Discover(val discovery: UpscalerDiscoveryState) : UpscalerSetupIntent

    data object Back : UpscalerSetupIntent
    data object Next : UpscalerSetupIntent
    data object Save : UpscalerSetupIntent
    data object Retry : UpscalerSetupIntent
    data object Dismiss : UpscalerSetupIntent
    data object ConfirmDelete : UpscalerSetupIntent

    /**
     * Captures the selected input for the owning screen action.
     *
     * @param id Dedicated catalog identity.
     * @author Dmitriy Moroz
     */
    data class Select(val id: String) : UpscalerSetupIntent

    /**
     * Captures the selected input for the owning screen action.
     *
     * @param id Dedicated model whose download, cancellation or removal is requested.
     * @author Dmitriy Moroz
     */
    data class ModelAction(val id: String) : UpscalerSetupIntent
}
