package com.shifthackz.aisdv1.presentation.generation.preview.editor

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.presentation.components.model.ExtraType
import com.shifthackz.aisdv1.presentation.generation.modal.embedding.EmbeddingItemUi
import com.shifthackz.aisdv1.presentation.generation.modal.embedding.EmbeddingState
import com.shifthackz.aisdv1.presentation.generation.modal.extras.ExtraItemUi
import com.shifthackz.aisdv1.presentation.generation.modal.extras.ExtrasState
import com.shifthackz.aisdv1.presentation.generation.modal.tag.EditTagState

/**
 * Supplies loading/empty/error catalogs, independent embedding selections and weighted-tag editors.
 *
 * @author Dmitriy Moroz
 */
internal class GenerationEditorPreviewParameterProvider :
    PreviewParameterProvider<GenerationEditorPreviewCase> {
    private val embeddings = EmbeddingState(
        loading = false, embeddings = listOf(
            EmbeddingItemUi("sample-style", true, false),
            EmbeddingItemUi("sample-quality", false, true),
            EmbeddingItemUi("a-long-sample-embedding-name-that-wraps-across-lines", false, false),
        )
    )
    private val extras = ExtrasState(
        loading = false, loras = listOf(
            ExtraItemUi(ExtraType.Lora, "1", "Sample watercolor", "watercolor-style", true, "0.8"),
            ExtraItemUi(
                ExtraType.Lora,
                "2",
                "A long sample LoRA catalog name with extra descriptive text",
                null,
                false
            ),
        )
    )
    private val cases: List<GenerationEditorPreviewCase> = listOf(
        GenerationEditorPreviewCase.Embeddings("Embeddings · Loading", EmbeddingState()),
        GenerationEditorPreviewCase.Embeddings(
            "Embeddings · Empty A1111",
            EmbeddingState(loading = false)
        ),
        GenerationEditorPreviewCase.Embeddings(
            "Embeddings · Empty Swarm",
            EmbeddingState(loading = false, source = ServerSource.SWARM_UI)
        ),
        GenerationEditorPreviewCase.Embeddings(
            "Embeddings · Generic error",
            EmbeddingState(loading = false, error = ErrorState.Generic)
        ),
        GenerationEditorPreviewCase.Embeddings(
            "Embeddings · Detailed error",
            EmbeddingState(
                loading = false,
                error = ErrorState.WithMessage("The embedding catalog could not be loaded.".asUiText())
            )
        ),
        GenerationEditorPreviewCase.Embeddings("Embeddings · Positive prompt", embeddings),
        GenerationEditorPreviewCase.Embeddings(
            "Embeddings · Negative prompt",
            embeddings.copy(selector = false)
        ),
        GenerationEditorPreviewCase.Extras("LoRA · Loading", ExtrasState()),
        GenerationEditorPreviewCase.Extras("LoRA · Empty A1111", ExtrasState(loading = false)),
        GenerationEditorPreviewCase.Extras(
            "LoRA · Empty Swarm",
            ExtrasState(loading = false, source = ServerSource.SWARM_UI)
        ),
        GenerationEditorPreviewCase.Extras("LoRA · Selected and unselected", extras),
        GenerationEditorPreviewCase.Extras(
            "LoRA · Generic error",
            ExtrasState(loading = false, error = ErrorState.Generic)
        ),
        GenerationEditorPreviewCase.Extras(
            "LoRA · Detailed error",
            ExtrasState(
                loading = false,
                error = ErrorState.WithMessage("The provider could not return its extra catalog.".asUiText())
            )
        ),
        GenerationEditorPreviewCase.Extras(
            "Hypernetwork · Loading",
            ExtrasState(type = ExtraType.HyperNet)
        ),
        GenerationEditorPreviewCase.Extras(
            "Hypernetwork · Empty",
            ExtrasState(loading = false, type = ExtraType.HyperNet)
        ),
        GenerationEditorPreviewCase.Extras(
            "Hypernetwork · Selected",
            extras.copy(
                type = ExtraType.HyperNet,
                loras = extras.loras.map { it.copy(type = ExtraType.HyperNet) })
        ),
        GenerationEditorPreviewCase.Tag(
            "Tag · Plain",
            EditTagState(originalTag = "watercolor", currentTag = "watercolor")
        ),
        GenerationEditorPreviewCase.Tag("Tag · Empty", EditTagState()),
        GenerationEditorPreviewCase.Tag(
            "Tag · Long text",
            EditTagState(currentTag = "a very long descriptive tag with several words and detailed styling instructions")
        ),
        GenerationEditorPreviewCase.Tag(
            "Tag · LoRA weight",
            EditTagState(extraType = ExtraType.Lora, currentTag = "<lora:sample:0.8>")
        ),
        GenerationEditorPreviewCase.Tag(
            "Tag · Hypernetwork weight",
            EditTagState(extraType = ExtraType.HyperNet, currentTag = "<hypernet:sample:1.25>")
        ),
        GenerationEditorPreviewCase.Tag(
            "Tag · Minimum weight",
            EditTagState(extraType = ExtraType.Lora, currentTag = "<lora:sample:-10>")
        ),
        GenerationEditorPreviewCase.Tag(
            "Tag · Maximum weight",
            EditTagState(extraType = ExtraType.Lora, currentTag = "<lora:sample:10>")
        ),
    )

    /**
     * Fixed prompt-editor snapshots with inert catalog selection, apply and dismissal intents.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<GenerationEditorPreviewCase> = cases.asSequence()

    /**
     * Identifies the editor category and catalog or tag condition.
     *
     * @param index Position in the ordered editor-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
