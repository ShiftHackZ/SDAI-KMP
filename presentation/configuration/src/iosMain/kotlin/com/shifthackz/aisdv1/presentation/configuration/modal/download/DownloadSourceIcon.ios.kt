package com.shifthackz.aisdv1.presentation.configuration.modal.download

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Draws the generic link icon for an iOS model-download mirror.
 *
 * @param host Source host accepted for platform API parity.
 * @param modifier Size and placement of the mirror icon.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun DownloadSourceIcon(
    host: String,
    modifier: Modifier,
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Default.Link,
        contentDescription = null,
    )
}
