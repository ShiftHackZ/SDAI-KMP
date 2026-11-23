package com.shifthackz.aisdv1.presentation.generation.preview.txt2img

import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionState
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageState

/**
 * Captured text-generation form and its initial disclosure, model catalog and lazy-list anchor.
 *
 * @param name Identifies the provider or field condition in the preview picker.
 * @param state Fixed text-generation inputs and validation/status feedback.
 * @param anchor Logical base/advanced field key to bring into the viewport.
 * @param isFormExpanded Initial generation-options disclosure state.
 * @param engineState Optional explicit checkpoint discovery/selection snapshot.
 * @author Dmitriy Moroz
 */
internal data class TextGenerationPreviewCase(
    val name: String,
    val state: TextToImageState,
    val anchor: String = "",
    val isFormExpanded: Boolean = true,
    val engineState: EngineSelectionState? = null,
)
