package com.shifthackz.aisdv1.presentation.generation.modal.history

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata

/**
 * Visible input-history parameters without encoded or decoded image payloads.
 *
 * @param generationResult Persisted metadata used for labels and revision-checked display requests.
 * @author Dmitriy Moroz
 */
@Immutable
data class InputHistoryItemUi(val generationResult: GalleryItemMetadata)
