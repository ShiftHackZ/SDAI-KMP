package com.shifthackz.aisdv1.presentation.components.widget.sheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Keeps Android sheets on their original Material renderer.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun isPlatformSheetBlurAvailable(): Boolean = false

/**
 * Adds no native backdrop on Android; the shared Material surface draws its background.
 *
 * @param color Native tint unused by this platform.
 * @param modifier Native backdrop bounds unused by this platform.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun PlatformSheetBackground(color: Color, modifier: Modifier) = Unit
