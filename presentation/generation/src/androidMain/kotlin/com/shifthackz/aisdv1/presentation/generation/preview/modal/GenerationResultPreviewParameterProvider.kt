package com.shifthackz.aisdv1.presentation.generation.preview.modal

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal

/**
 * Supplies saved/unsaved single results, reporting actions and mixed-visibility batches.
 *
 * @author Dmitriy Moroz
 */
internal class GenerationResultPreviewParameterProvider :
    PreviewParameterProvider<GenerationModalPreviewCase> {
    private val result = GenerationPreviewFixtures.result
    private val batch =
        List(6) { index -> result.copy(id = index.toLong() + 1, hidden = index % 2 == 1) }
    private val cases = listOf(
        GenerationModalPreviewCase(
            "Single · Save",
            GenerationModal.Image.Single(result, false, false)
        ),
        GenerationModalPreviewCase(
            "Single · Already saved",
            GenerationModal.Image.Single(result, true, false)
        ),
        GenerationModalPreviewCase(
            "Single · Save and report",
            GenerationModal.Image.Single(result, false, true)
        ),
        GenerationModalPreviewCase(
            "Single · Report only",
            GenerationModal.Image.Single(result, true, true)
        ),
        GenerationModalPreviewCase("Batch · Save", GenerationModal.Image.Batch(batch, false)),
        GenerationModalPreviewCase(
            "Batch · Already saved",
            GenerationModal.Image.Batch(batch, true)
        ),
    )

    /**
     * Fixed successful-result overlays with inert save/report/detail callbacks.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<GenerationModalPreviewCase> = cases.asSequence()

    /**
     * Identifies the result scope and available actions.
     *
     * @param index Position in the ordered result-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
