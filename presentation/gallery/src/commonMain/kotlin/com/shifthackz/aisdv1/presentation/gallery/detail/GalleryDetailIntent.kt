package com.shifthackz.aisdv1.presentation.gallery.detail

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Detail navigation, parameter reuse, export/sharing and confirmed deletion requests.
 *
 * @author Dmitriy Moroz
 */
sealed interface GalleryDetailIntent : MviIntent {

    data object NavigateBack : GalleryDetailIntent

    data object NavigatePrevious : GalleryDetailIntent

    data object NavigateNext : GalleryDetailIntent

    /**
     * Selects another generation in the current detail paging window.
     *
     * @param page Zero-based page index requested by the detail pager.
     *
     * @author Dmitriy Moroz
     */
    data class NavigateToPage(val page: Int) : GalleryDetailIntent

    /**
     * Selects output or source-image metadata in the detail renderer.
     *
     * @param tab Detail tab chosen by the user.
     *
     * @author Dmitriy Moroz
     */
    data class SelectTab(val tab: GalleryDetailTab) : GalleryDetailIntent

    /**
     * Copies a displayed metadata value through the native clipboard adapter.
     *
     * @param content Already formatted prompt or generation parameter text.
     *
     * @author Dmitriy Moroz
     */
    data class CopyToClipboard(val content: String) : GalleryDetailIntent

    /**
     * Restores the current generation into the chosen form through the shared form-update channel.
     *
     * @author Dmitriy Moroz
     */
    enum class SendTo : GalleryDetailIntent {
        Img2Img,
        Txt2Img,
    }

    /**
     * Exports the current original image or its formatted generation parameters.
     *
     * @author Dmitriy Moroz
     */
    enum class Export : GalleryDetailIntent {
        Image,
        Params,
    }

    /**
     * Shares the current original image or formatted parameters through the native adapter.
     *
     * @author Dmitriy Moroz
     */
    enum class Share : GalleryDetailIntent {
        Image,
        Params,
    }

    /**
     * Requests or confirms deletion of the current persistent generation.
     *
     * @author Dmitriy Moroz
     */
    enum class Delete : GalleryDetailIntent {
        Request,
        Confirm,
    }

    data object ToggleVisibility : GalleryDetailIntent

    data object ToggleLike : GalleryDetailIntent

    data object Report : GalleryDetailIntent

    data object DismissDialog : GalleryDetailIntent
}
