package com.shifthackz.aisdv1.presentation.screen.gallery.list

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.MediaStoreInfo

/**
 * Carries `GalleryState` data through the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class GalleryState(
    /**
     * Exposes the `loading` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val loading: Boolean = true,
    /**
     * Exposes the `items` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val items: List<GalleryGridItemUi> = emptyList(),
    /**
     * Stable global positions without originals.
     *
     * @author Dmitriy Moroz
     */
    val itemIds: List<Long> = emptyList(),
    /**
     * Selected flags survive metadata-window eviction.
     *
     * @author Dmitriy Moroz
     */
    val selectedFlags: List<GalleryGridItemUi> = emptyList(),
    /**
     * Exposes the `dialog` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val dialog: GalleryDialog = GalleryDialog.None,
    /**
     * Exposes the `mediaStoreInfo` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val mediaStoreInfo: MediaStoreInfo = MediaStoreInfo(),
    /**
     * Exposes the `dropdownMenuShow` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val dropdownMenuShow: Boolean = false,
    /**
     * Exposes the `selectionMode` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val selectionMode: Boolean = false,
    /**
     * Exposes the `selection` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val selection: List<Long> = emptyList(),
    /**
     * Exposes the `grid` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val grid: Grid = Grid.Fixed2,
) : MviState {
    /**
     * Exposes the `selectedItems` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val selectedItems: List<GalleryGridItemUi>
        get() = selectedFlags.filter { it.id in selection }

    /**
     * Exposes the `shouldHideSelection` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val shouldHideSelection: Boolean
        get() = selectedItems.any { !it.hidden }

    /**
     * Exposes the `shouldLikeSelection` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val shouldLikeSelection: Boolean
        get() = selectedItems.any { !it.liked }
}

/**
 * Defines the `GalleryDialog` contract for the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
@Immutable
sealed interface GalleryDialog {
    /**
     * Provides the `None` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object None : GalleryDialog

    /**
     * Provides the `DeleteAllConfirm` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object DeleteAllConfirm : GalleryDialog

    /**
     * Provides the `DeleteSelectionConfirm` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object DeleteSelectionConfirm : GalleryDialog

    /**
     * Carries `ConfirmExport` data through the SDAI presentation layer.
     *
     * @param exportAll export all value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class ConfirmExport(val exportAll: Boolean) : GalleryDialog

    /**
     * Provides the `ExportInProgress` singleton used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    data object ExportInProgress : GalleryDialog

    /**
     * Carries `Error` data through the SDAI presentation layer.
     *
     * @param message message value consumed by the API.
     * @author Dmitriy Moroz
     */
    data class Error(val message: String) : GalleryDialog
}

/**
 * Carries `GalleryGridItemUi` data through the SDAI presentation layer.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class GalleryGridItemUi(
    /**
     * Exposes the `id` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val id: Long,
    /**
     * Opaque image identity, independent of payload length.
     *
     * @author Dmitriy Moroz
     */
    val imageRevision: String = "",
    /**
     * Exposes the `hidden` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val hidden: Boolean,
    /**
     * Exposes the `liked` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    val liked: Boolean = false,
)
