package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets

/**
 * Gives every setup provider identical horizontal margins, action spacing and bottom safe-area padding.
 * @param modifier Bounds applied to the pinned action column.
 * @param verticalArrangement Spacing between actions; animated actions may own their collapsing gap.
 * @param content Primary and secondary actions, in display order.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SetupActionBar(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(persistentBottomBarWindowInsets())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

/**
 * Uses the same full-width primary button geometry for Next, Connect, Setup and OAuth actions.
 * @param onClick Dispatches the selected action.
 * @param modifier Layout modifiers applied before the common button dimensions.
 * @param isEnabled Whether this action is currently available.
 * @param content Visible label and optional decorative icon.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SetupPrimaryAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        enabled = isEnabled,
        content = content
    )
}

/**
 * Matches primary action dimensions while retaining the outlined treatment for secondary operations.
 * @param onClick Dispatches the selected action.
 * @param modifier Layout modifiers applied before the common button dimensions.
 * @param isEnabled Whether this action is currently available.
 * @param content Visible label and optional decorative icon.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SetupSecondaryAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        enabled = isEnabled,
        content = content
    )
}
