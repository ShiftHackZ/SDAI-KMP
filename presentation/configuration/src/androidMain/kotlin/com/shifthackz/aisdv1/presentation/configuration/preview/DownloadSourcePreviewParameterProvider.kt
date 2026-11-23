package com.shifthackz.aisdv1.presentation.configuration.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.configuration.modal.download.DownloadDialogState

/**
 * Supplies mirror-list shapes using fixed URLs without resolving a model or starting a transfer.
 *
 * @author Dmitriy Moroz
 */
internal class DownloadSourcePreviewParameterProvider :
    PreviewParameterProvider<Pair<String, DownloadDialogState>> {
    private val cases = listOf(
        "No sources" to DownloadDialogState(),
        "Single mirror" to DownloadDialogState(listOf("https://huggingface.co/example/model/resolve/main/model.zip" to true)),
        "Multiple mirrors" to DownloadDialogState(
            listOf(
                "https://huggingface.co/example/model/resolve/main/model.zip" to false,
                "https://github.com/example/model/releases/download/sample/model.zip" to true,
                "https://models.example/model.zip" to false,
            )
        ),
        "No selection" to DownloadDialogState(listOf("https://models.example/model.zip" to false)),
        "Long host" to DownloadDialogState(listOf("https://very-long-model-download-mirror.example/model.zip" to true)),
    )

    /**
     * Named empty, single, selected and long-host dialog states.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<Pair<String, DownloadDialogState>> = cases.asSequence()

    /**
     * Identifies the mirror-list shape above the preview.
     *
     * @param index Position in the ordered source-list scenarios.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].first
}
