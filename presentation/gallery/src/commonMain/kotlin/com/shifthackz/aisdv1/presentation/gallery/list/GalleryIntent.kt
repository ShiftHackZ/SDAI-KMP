package com.shifthackz.aisdv1.presentation.gallery.list

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Gallery metadata paging, selection, export/deletion confirmation and navigation requests.
 *
 * @author Dmitriy Moroz
 */
sealed interface GalleryIntent : MviIntent {
    /**
     * Requests metadata around visible positions.
     *
     * @param first First visible index.
     * @author Dmitriy Moroz
     */
    data class Viewport(val first: Int) : GalleryIntent

    /**
     * Two-stage archive creation requests requiring explicit confirmation.
     *
     * @author Dmitriy Moroz
     */
    sealed interface Export : GalleryIntent {
        /**
         * Requests or confirms exporting the entire gallery.
         *
         * @author Dmitriy Moroz
         */
        enum class All : Export {
            Request, Confirm;
        }

        /**
         * Requests or confirms exporting the current selection.
         *
         * @author Dmitriy Moroz
         */
        enum class Selection : Export {
            Request, Confirm;
        }
    }

    /**
     * Two-stage deletion requests requiring explicit confirmation.
     *
     * @author Dmitriy Moroz
     */
    sealed interface Delete : GalleryIntent {
        /**
         * Requests or confirms deleting the entire gallery.
         *
         * @author Dmitriy Moroz
         */
        enum class All : Delete {
            Request, Confirm;
        }

        /**
         * Requests or confirms deleting the current selection.
         *
         * @author Dmitriy Moroz
         */
        enum class Selection : Delete {
            Request, Confirm;
        }
    }

    /**
     * Visibility requests for the gallery action dropdown.
     *
     * @author Dmitriy Moroz
     */
    enum class Dropdown : GalleryIntent {
        Toggle, Show, Close;
    }


    data object DismissDialog : GalleryIntent


    data object OpenDrawer : GalleryIntent


    data object OpenMediaStoreFolder : GalleryIntent

    /**
     * Opens one saved generation in gallery detail.
     *
     * @param id Persistent generation identity selected from the grid.
     *
     * @author Dmitriy Moroz
     */
    data class OpenItem(val id: Long) : GalleryIntent

    /**
     * Enters or leaves multi-selection mode.
     *
     * @param flag Whether selection controls should be active.
     *
     * @author Dmitriy Moroz
     */
    data class ChangeSelectionMode(val flag: Boolean) : GalleryIntent


    data object UnselectAll : GalleryIntent


    data object ToggleSelectionVisibility : GalleryIntent


    data object ToggleSelectionLike : GalleryIntent

    /**
     * Toggles one generation in the current selection.
     *
     * @param id Persistent generation identity whose selected state should change.
     *
     * @author Dmitriy Moroz
     */
    data class ToggleItemSelection(val id: Long) : GalleryIntent
}
