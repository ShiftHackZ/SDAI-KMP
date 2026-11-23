package com.shifthackz.aisdv1.presentation.generation.preview.editor

import com.shifthackz.aisdv1.presentation.generation.modal.embedding.EmbeddingState
import com.shifthackz.aisdv1.presentation.generation.modal.extras.ExtrasState
import com.shifthackz.aisdv1.presentation.generation.modal.tag.EditTagState

/**
 * Captured prompt-editor variants rendered without their catalog-loading ViewModels.
 *
 * @author Dmitriy Moroz
 */
internal sealed interface GenerationEditorPreviewCase {
    val name: String

    /**
     * Captured textual-inversion catalog and active prompt field.
     *
     * @param name Preview picker label.
     * @param state Catalog entries, prompt membership and loading/error state.
     * @author Dmitriy Moroz
     */
    data class Embeddings(override val name: String, val state: EmbeddingState) :
        GenerationEditorPreviewCase

    /**
     * Captured LoRA/hypernetwork catalog and application flags.
     *
     * @param name Preview picker label.
     * @param state Catalog entries, selected category and loading/error state.
     * @author Dmitriy Moroz
     */
    data class Extras(override val name: String, val state: ExtrasState) :
        GenerationEditorPreviewCase

    /**
     * Captured plain or weighted tag editor.
     *
     * @param name Preview picker label.
     * @param state Tag text, extra category and numeric weight.
     * @author Dmitriy Moroz
     */
    data class Tag(override val name: String, val state: EditTagState) : GenerationEditorPreviewCase
}
