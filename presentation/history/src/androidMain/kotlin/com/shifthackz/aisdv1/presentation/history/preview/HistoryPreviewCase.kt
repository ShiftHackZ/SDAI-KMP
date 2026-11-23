package com.shifthackz.aisdv1.presentation.history.preview

import com.shifthackz.aisdv1.presentation.history.HistoryState

/**
 * Names a fixed history snapshot and identifies thumbnails that should show the shared placeholder.
 *
 * @param name Label shown in the Android preview list.
 * @param state Loading status, metadata rows or read failure to display.
 * @param unavailableImageIds Entry identities whose thumbnails are loading or unavailable.
 * @author Dmitriy Moroz
 */
internal data class HistoryPreviewCase(
    val name: String,
    val state: HistoryState,
    val unavailableImageIds: Set<Long> = emptySet(),
)
