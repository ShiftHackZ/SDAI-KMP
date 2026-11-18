package com.shifthackz.aisdv1.presentation.configuration.modal.download

import com.shifthackz.aisdv1.core.mvi.MviEffect

/**
 * Dismissal or confirmed mirror handoff emitted by model-download selection.
 *
 * @author Dmitriy Moroz
 */
sealed interface DownloadDialogEffect : MviEffect {

    /**
     * Removes the source-selection dialog without starting a download.
     *
     * @author Dmitriy Moroz
     */
    data object Close : DownloadDialogEffect

    /**
     * Hands the confirmed mirror URL to the owning setup/download workflow.
     *
     * @param url Selected catalog source URL for the model download.
     *
     * @author Dmitriy Moroz
     */
    data class StartDownload(val url: String) : DownloadDialogEffect
}
