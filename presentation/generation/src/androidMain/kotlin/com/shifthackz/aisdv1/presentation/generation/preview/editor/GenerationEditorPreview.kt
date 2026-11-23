package com.shifthackz.aisdv1.presentation.generation.preview.editor

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewTheme
import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.presentation.generation.modal.embedding.EmbeddingScreenContent
import com.shifthackz.aisdv1.presentation.generation.modal.extras.ExtrasScreenContent
import com.shifthackz.aisdv1.presentation.generation.modal.tag.EditTagScreenContent

/**
 * Displays the actual prompt-editor dialog without its ViewModel, provider requests or prompt writes.
 *
 * @param scenario Captured catalog, selection or tag state.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationEditorPreview(scenario: GenerationEditorPreviewCase) {
    GenerationPreviewTheme {
        when (scenario) {
            is GenerationEditorPreviewCase.Embeddings -> EmbeddingScreenContent(
                state = scenario.state,
                processIntent = {})

            is GenerationEditorPreviewCase.Extras -> ExtrasScreenContent(
                state = scenario.state,
                processIntent = {})

            is GenerationEditorPreviewCase.Tag -> EditTagScreenContent(
                state = scenario.state,
                processIntent = {})
        }
    }
}
