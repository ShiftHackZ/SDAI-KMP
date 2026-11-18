package com.shifthackz.aisdv1.presentation.donate

import com.shifthackz.aisdv1.presentation.components.widget.image.sdaiLogoPainter

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Draws the donation illustration using the current platform branding.
 *
 * @param modifier Size and placement within the donation page.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun DonateBrandIcon(modifier: Modifier) {
    Image(
        modifier = modifier.size(36.dp),
        painter = sdaiLogoPainter(),
        contentDescription = "SDAI Android Branding",
    )
}
