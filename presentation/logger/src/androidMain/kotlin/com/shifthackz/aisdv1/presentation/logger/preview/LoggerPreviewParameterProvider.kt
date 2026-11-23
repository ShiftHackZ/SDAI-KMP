package com.shifthackz.aisdv1.presentation.logger.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.logger.LoggerState

/**
 * Supplies loading, blank and populated snapshots without reading the application's log cache.
 * Read failures share the empty presentation and have no separate error dialog.
 *
 * @author Dmitriy Moroz
 */
internal class LoggerPreviewParameterProvider : PreviewParameterProvider<LoggerPreviewCase> {
    private val cases = listOf(
        LoggerPreviewCase("Loading", LoggerState()),
        LoggerPreviewCase("Empty or failed read", LoggerState(loading = false)),
        LoggerPreviewCase("Whitespace only", LoggerState(loading = false, text = " \n\t ")),
        LoggerPreviewCase(
            "Single line",
            LoggerState(loading = false, text = LoggerPreviewFixtures.ShortLog)
        ),
        LoggerPreviewCase(
            "Mixed levels",
            LoggerState(loading = false, text = LoggerPreviewFixtures.mixedLog)
        ),
        LoggerPreviewCase(
            "Vertical overflow",
            LoggerState(loading = false, text = LoggerPreviewFixtures.longLog)
        ),
        LoggerPreviewCase(
            "Horizontal overflow",
            LoggerState(loading = false, text = LoggerPreviewFixtures.wideLog)
        ),
        LoggerPreviewCase(
            "Stack trace",
            LoggerState(loading = false, text = LoggerPreviewFixtures.stackTrace)
        ),
    )

    /**
     * Fixed snapshots rendered by each palette annotation.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<LoggerPreviewCase> = cases.asSequence()

    /**
     * Identifies the text shape or read status above its preview.
     *
     * @param index Position in the ordered snapshot list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
