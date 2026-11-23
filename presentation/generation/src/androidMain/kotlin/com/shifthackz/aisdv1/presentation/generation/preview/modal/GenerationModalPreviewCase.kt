package com.shifthackz.aisdv1.presentation.generation.preview.modal

import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal

/**
 * Captured decision/progress/result overlay with a fixed clock and optional resource panel.
 *
 * @param name Identifies the provider stage, decision or result condition.
 * @param modal Actual generation modal state rendered with inert operations.
 * @param elapsedText Fixed formatted time replacing the live monotonic ticker.
 * @param shouldShowPerformance Whether the progress dialog includes fixed CPU/RAM readings.
 * @author Dmitriy Moroz
 */
internal data class GenerationModalPreviewCase(
    val name: String,
    val modal: GenerationModal,
    val elapsedText: String? = null,
    val shouldShowPerformance: Boolean = false,
)
