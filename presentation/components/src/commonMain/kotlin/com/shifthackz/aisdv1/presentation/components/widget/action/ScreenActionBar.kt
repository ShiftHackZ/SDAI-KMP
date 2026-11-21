package com.shifthackz.aisdv1.presentation.components.widget.action

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor

/**
 * Gives every setup provider identical horizontal margins, action spacing and bottom safe-area padding.
 * Native controls inherit the resolved container color so their transparent corners blend with it.
 * @param modifier Bounds applied to the pinned action column.
 * @param containerColor Screen surface extended behind the actions and bottom safe area.
 * @param verticalArrangement Spacing between actions; animated actions may own their collapsing gap.
 * @param content Primary and secondary actions, in display order.
 * @author Dmitriy Moroz
 */
@Composable
fun ScreenActionBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalPlatformControlBackgroundColor provides containerColor) {
        Column(
            modifier = modifier.fillMaxWidth()
                .background(containerColor)
                .windowInsetsPadding(persistentBottomBarWindowInsets())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
