package com.shifthackz.aisdv1.presentation.generation.modal.embedding

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.ErrorState

/**
 * Provider embedding catalog and a proposed prompt pair before the parent form confirms replacement.
 *
 * @param loading Whether the embedding catalog is being loaded.
 * @param source Provider whose embedding capabilities/catalog are shown.
 * @param error Catalog-load error displayed by the editor.
 * @param prompt Proposed positive prompt including current embedding toggles.
 * @param negativePrompt Proposed negative prompt including current embedding toggles.
 * @param embeddings Catalog entries with independent positive/negative selection flags.
 * @param selector True edits the positive prompt; false edits the negative prompt.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class EmbeddingState(
    val loading: Boolean = true,
    val source: ServerSource = ServerSource.AUTOMATIC1111,
    val error: ErrorState = ErrorState.None,
    val prompt: String = "",
    val negativePrompt: String = "",
    val embeddings: List<EmbeddingItemUi> = emptyList(),
    val selector: Boolean = true,
) : MviState

/**
 * One textual-inversion keyword with its occurrence in each proposed prompt.
 *
 * @param keyword Provider keyword inserted or removed when the item is toggled.
 * @param isInPrompt Whether the keyword is selected in the positive prompt.
 * @param isInNegativePrompt Whether the keyword is selected in the negative prompt.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class EmbeddingItemUi(
    val keyword: String,
    val isInPrompt: Boolean,
    val isInNegativePrompt: Boolean,
)
