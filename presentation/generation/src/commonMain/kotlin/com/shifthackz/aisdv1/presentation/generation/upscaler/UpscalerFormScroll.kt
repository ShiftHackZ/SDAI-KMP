package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.withFrameNanos

/**
 * Follows the expanding final item frame by frame, including trailing content padding.
 * Scrolling uses the disclosure's own motion and stops when a user gesture takes control.
 * The upscaler must still be visible; leaving it preserves the user's new position.
 *
 * @param isExpanding Whether the disclosure is still moving toward its enabled size.
 * @author Dmitriy Moroz
 */
internal suspend fun LazyListState.revealUpscalerControls(isExpanding: () -> Boolean) {
    scroll {
        var hasExpansionFinished = false
        do {
            withFrameNanos { }
            val layout = layoutInfo
            val lastItem = layout.visibleItemsInfo.lastOrNull() ?: return@scroll
            if (lastItem.index != layout.totalItemsCount - 1) return@scroll
            val remaining =
                lastItem.offset + lastItem.size + layout.afterContentPadding - layout.viewportEndOffset
            if (remaining > 0) scrollBy(remaining.toFloat())
            if (hasExpansionFinished) break
            hasExpansionFinished = !isExpanding()
        } while (true)
    }
}
