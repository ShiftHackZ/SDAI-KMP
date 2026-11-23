package com.shifthackz.aisdv1.presentation.home

import com.shifthackz.aisdv1.presentation.components.widget.image.sdaiLogoPainter

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Draws the SDAI brand mark in the drawer header.
 *
 * @param modifier Header-controlled size and placement of the logo.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun DrawerBrandLogo(modifier: Modifier) {
    Image(
        modifier = modifier,
        painter = sdaiLogoPainter(),
        contentDescription = "SDAI Android Branding",
    )
}

/**
 * Draws the current host platform icon beside the app name.
 *
 * @param modifier Header-controlled size and placement of the platform icon.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun DrawerPlatformIcon(modifier: Modifier) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Default.Android,
        contentDescription = "Android",
    )
}
