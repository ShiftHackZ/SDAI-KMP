package com.shifthackz.aisdv1.presentation.logger.preview

import com.shifthackz.aisdv1.presentation.logger.LoggerState

/**
 * Names a fixed log snapshot for the Android preview list.
 *
 * @param name Label identifying the loading status or log shape.
 * @param state Snapshot passed through the same content mapper as the live screen.
 * @author Dmitriy Moroz
 */
internal data class LoggerPreviewCase(
    val name: String,
    val state: LoggerState,
)
