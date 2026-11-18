package com.shifthackz.aisdv1.presentation.history

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata

/**
 * Metadata snapshot rendered by generation history without retaining original image bytes.
 *
 * @param loading Whether a persistent-history read is in progress.
 * @param items Ordered generation metadata returned by the last successful read.
 * @param error Optional user-visible load failure shown with a refresh action.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class HistoryState(
    val loading: Boolean = true,
    val items: List<GalleryItemMetadata> = emptyList(),
    val error: String? = null,
) : MviState
