package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Preserves the provider search field's Material background and rounded outline on Android.
 *
 * @param modifier Search field bounds and placement.
 * @param content Existing shared search controls.
 * @author Dmitriy Moroz
 */
@Composable
internal actual fun PlatformSearchFieldContainer(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        content()
    }
}
