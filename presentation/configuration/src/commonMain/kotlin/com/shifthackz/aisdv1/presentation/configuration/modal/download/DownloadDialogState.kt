package com.shifthackz.aisdv1.presentation.configuration.modal.download

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * Available catalog mirrors and current selection for one model download.
 *
 * @param sources Pairs of exact mirror URL and whether that mirror is selected.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class DownloadDialogState(
    val sources: List<Pair<String, Boolean>> = emptyList(),
) : MviState {

    val selectedUrl: String
        get() = sources.find { (_, selected) -> selected }?.first ?: ""
}
