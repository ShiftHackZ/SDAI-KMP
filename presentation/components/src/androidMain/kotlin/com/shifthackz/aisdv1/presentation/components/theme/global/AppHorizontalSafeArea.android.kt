package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Protects the Android viewport from side navigation bars and display cutouts, consuming their space.
 *
 * @return Application placement padded by remaining horizontal safe drawing insets.
 * @author Dmitriy Moroz
 */
@Composable
actual fun Modifier.appHorizontalSafeArea(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
