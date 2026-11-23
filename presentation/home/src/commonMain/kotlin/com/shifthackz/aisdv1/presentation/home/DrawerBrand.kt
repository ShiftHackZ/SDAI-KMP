package com.shifthackz.aisdv1.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Draws the SDAI brand mark in the drawer header.
 *
 * @param modifier Header-controlled size and placement of the logo.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun DrawerBrandLogo(modifier: Modifier = Modifier)

/**
 * Draws the current host platform icon beside the app name.
 *
 * @param modifier Header-controlled size and placement of the platform icon.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun DrawerPlatformIcon(modifier: Modifier = Modifier)
