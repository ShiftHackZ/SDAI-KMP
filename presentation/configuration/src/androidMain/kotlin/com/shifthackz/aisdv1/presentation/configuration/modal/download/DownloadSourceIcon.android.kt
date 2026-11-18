package com.shifthackz.aisdv1.presentation.configuration.modal.download

import com.shifthackz.aisdv1.presentation.components.widget.image.sdaiLogoPainter

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.shifthackz.aisdv1.presentation.configuration.R

/**
 * Draws GitHub/SDAI branding for known model mirrors or a generic link icon otherwise.
 *
 * @param host Source host identity used to select bundled branding.
 * @param modifier Size and placement of the mirror icon.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun DownloadSourceIcon(
    host: String,
    modifier: Modifier,
) {
    when (host) {
        GITHUB_WEB_RESOURCE -> Icon(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_github),
            contentDescription = null,
        )

        SDAI_WEB_RESOURCE -> Image(
            modifier = modifier,
            painter = sdaiLogoPainter(),
            contentDescription = null,
        )

        else -> Icon(
            modifier = modifier,
            imageVector = Icons.Default.Link,
            contentDescription = null,
        )
    }
}
