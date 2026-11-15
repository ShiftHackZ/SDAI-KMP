package com.shifthackz.aisdv1.presentation.modal.history

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * Carries `InputHistoryState` data through the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class InputHistoryState(
    /**
     * Exposes the `loading` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val loading: Boolean = true,
    /**
     * Exposes the `loadingNextPage` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val loadingNextPage: Boolean = false,
    /**
     * Exposes the `items` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val items: List<InputHistoryItemUi> = emptyList(),
    /**
     * Exposes the `nextPage` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val nextPage: Int = 0,
    /**
     * Exposes the `canLoadMore` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val canLoadMore: Boolean = true,
    /**
     * Exposes the `error` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val error: String? = null,
) : MviState
