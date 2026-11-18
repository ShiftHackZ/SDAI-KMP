package com.shifthackz.aisdv1.presentation.generation.modal.history

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * Metadata-only pages and read feedback for restoring generation input from history.
 *
 * @param loading Whether the initial/retry page is loading.
 * @param loadingNextPage Whether another metadata page is being appended.
 * @param items Already loaded history metadata projections.
 * @param nextPage Zero-based index of the next metadata page to request.
 * @param canLoadMore Whether pagination may request another page.
 * @param error Optional localized page/selection failure.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class InputHistoryState(
    val loading: Boolean = true,
    val loadingNextPage: Boolean = false,
    val items: List<InputHistoryItemUi> = emptyList(),
    val nextPage: Int = 0,
    val canLoadMore: Boolean = true,
    val error: String? = null,
) : MviState
