package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.MediaStoreInfo

/**
 * Metadata-window gallery state; full image payloads belong to loaders/export operations.
 * Stable identities and selected flags remain available when their rows leave the metadata window.
 *
 * @param loading Whether initial gallery metadata is loading.
 * @param items Metadata entries currently inside the bounded viewport window.
 * @param itemIds Ordered persistent identities spanning the full gallery.
 * @param selectedFlags Hidden/liked flags retained for selected identities outside the visible window.
 * @param dialog Active export/delete confirmation, progress or error overlay.
 * @param mediaStoreInfo Current media-export destination and saved-media summary.
 * @param dropdownMenuShow Whether the gallery action menu is open.
 * @param selectionMode Whether grid taps edit a multi-selection.
 * @param selection Persistent identities currently selected.
 * @param grid Saved grid density used by the renderer.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class GalleryState(
    val loading: Boolean = true,
    val items: List<GalleryGridItemUi> = emptyList(),
    val itemIds: List<Long> = emptyList(),
    val selectedFlags: List<GalleryGridItemUi> = emptyList(),
    val dialog: GalleryDialog = GalleryDialog.None,
    val mediaStoreInfo: MediaStoreInfo = MediaStoreInfo(),
    val dropdownMenuShow: Boolean = false,
    val selectionMode: Boolean = false,
    val selection: List<Long> = emptyList(),
    val grid: Grid = Grid.Fixed2,
) : MviState {
    /**
     * Selected entries with current hidden/liked flags, including those outside the metadata window.
     *
     * @author Dmitriy Moroz
     */
    val selectedItems: List<GalleryGridItemUi>
        get() = selectedFlags.filter { it.id in selection }

    /**
     * Whether at least one selected entry is visible, so the next group action hides the selection.
     *
     * @author Dmitriy Moroz
     */
    val shouldHideSelection: Boolean
        get() = selectedItems.any { !it.hidden }

    /**
     * Whether at least one selected entry is unliked, so the next group action likes the selection.
     *
     * @author Dmitriy Moroz
     */
    val shouldLikeSelection: Boolean
        get() = selectedItems.any { !it.liked }
}

/**
 * Gallery overlays for destructive/export confirmation, archive progress or public failures.
 *
 * @author Dmitriy Moroz
 */
@Immutable
sealed interface GalleryDialog {

    data object None : GalleryDialog


    data object DeleteAllConfirm : GalleryDialog


    data object DeleteSelectionConfirm : GalleryDialog

    /**
     * Asks whether to create an archive for the requested gallery scope.
     *
     * @param exportAll True exports the full gallery; false exports the current selection.
     *
     * @author Dmitriy Moroz
     */
    data class ConfirmExport(val exportAll: Boolean) : GalleryDialog

    /**
     * Archive creation is active; the renderer displays progress.
     *
     * @author Dmitriy Moroz
     */
    data object ExportInProgress : GalleryDialog

    /**
     * An operation failed with public details for the gallery dialog.
     *
     * @param message User-visible failure explanation.
     *
     * @author Dmitriy Moroz
     */
    data class Error(val message: String) : GalleryDialog
}

/**
 * Image identity and flags required by a sampled gallery cell.
 *
 * @param id Persistent generation identity.
 * @param imageRevision Opaque content identity invalidating sampled image caches after replacement.
 * @param hidden Whether the preview should be obscured.
 * @param liked Whether the generation is marked as a favorite.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class GalleryGridItemUi(

    val id: Long,

    val imageRevision: String = "",

    val hidden: Boolean,

    val liked: Boolean = false,
)
