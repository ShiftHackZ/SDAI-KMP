package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets

/**
 * Provides the shared bottom surface and native control backing for generation and upscale actions.
 * Consumes any remaining bottom system inset when a parent has not already reserved it.
 *
 * @param modifier Bounds supplied by the screen's bottom-bar slot.
 * @param content Optional shortcuts followed by the primary action with its own standard margins.
 * @author Dmitriy Moroz
 */
@Composable
fun GenerationActionBar(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val color = MaterialTheme.colorScheme.surface
    CompositionLocalProvider(LocalPlatformControlBackgroundColor provides color) {
        Column(
            modifier = modifier.fillMaxWidth().background(color)
                .windowInsetsPadding(persistentBottomBarWindowInsets()).padding(top = 8.dp),
            content = content,
        )
    }
}
