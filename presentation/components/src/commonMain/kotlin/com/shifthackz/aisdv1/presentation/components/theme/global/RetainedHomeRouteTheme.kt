package com.shifthackz.aisdv1.presentation.components.theme.global

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

/**
 * Keeps a hidden home tab on its last committed palette while preserving the tab's composition,
 * remembered editing state and ViewModels. Revealing the tab reads the current root appearance
 * immediately. A hidden branch reads no root color, typography or shape values after capture.
 * This is a local Material boundary; it does not initialize application services or system bars.
 *
 * @param isVisible Whether this home tab is currently presented to the user.
 * @param content Existing tab content whose remembered state remains alive while hidden.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun RetainedHomeRouteTheme(
    isVisible: Boolean,
    content: @Composable () -> Unit,
) {
    val retainedState = remember { HomeRouteThemeState() }
    val committedSnapshot = retainedState.snapshot
    val snapshot = if (isVisible || committedSnapshot == null) {
        HomeRouteThemeSnapshot(
            colorScheme = MaterialTheme.colorScheme.copy(),
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes,
            contentColor = LocalContentColor.current,
        )
    } else committedSnapshot
    if (isVisible || committedSnapshot == null) {
        SideEffect { retainedState.snapshot = snapshot }
    }
    MaterialTheme(
        colorScheme = snapshot.colorScheme,
        typography = snapshot.typography,
        shapes = snapshot.shapes,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides snapshot.contentColor,
            content = content
        )
    }
}
