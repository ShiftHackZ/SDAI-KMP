package com.shifthackz.aisdv1.presentation.components.platform

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLayoutDirection
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility

/**
 * Keeps native child compositions synchronized with the app theme and current route content.
 * The child retains its own UIKit, density and rendering owners rather than inheriting parent owners.
 *
 * @param content Shared controls hosted by a transparent native child composition.
 * @return Stable content lambda reading the latest theme, visibility, direction and controls.
 * @author Dmitriy Moroz
 */
@Composable
internal fun rememberThemedNativeContent(content: @Composable () -> Unit): @Composable () -> Unit {
    val colors = rememberUpdatedState(MaterialTheme.colorScheme)
    val shapes = rememberUpdatedState(MaterialTheme.shapes)
    val typography = rememberUpdatedState(MaterialTheme.typography)
    val direction = rememberUpdatedState(LocalLayoutDirection.current)
    val visibility = rememberUpdatedState(LocalMviVisibility.current)
    val currentContent = rememberUpdatedState(content)
    return remember {
        {
            CompositionLocalProvider(
                LocalLayoutDirection provides direction.value,
                LocalMviVisibility provides visibility.value,
            ) {
                MaterialTheme(
                    colorScheme = colors.value,
                    shapes = shapes.value,
                    typography = typography.value,
                ) { currentContent.value() }
            }
        }
    }
}
