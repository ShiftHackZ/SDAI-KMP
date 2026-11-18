package com.shifthackz.aisdv1.presentation.configuration.modal.download

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Mirror selection and confirmation requests for downloading a catalog-managed local model.
 *
 * @author Dmitriy Moroz
 */
sealed interface DownloadDialogIntent : MviIntent {

    /**
     * Selects one of the catalog model's available download mirrors.
     *
     * @param url Exact source URL selected from the model catalog.
     *
     * @author Dmitriy Moroz
     */
    data class SelectSource(val url: String) : DownloadDialogIntent

    /**
     * Closes mirror selection without starting a download.
     *
     * @author Dmitriy Moroz
     */
    data object Close : DownloadDialogIntent

    /**
     * Confirms downloading from the currently selected source URL.
     *
     * @author Dmitriy Moroz
     */
    data object StartDownload : DownloadDialogIntent
}
