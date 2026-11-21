package com.shifthackz.aisdv1.presentation.components.widget.surface

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Keeps panel controls on an ordinary Material surface on Android.
 *
 * @param modifier Panel bounds and placement.
 * @param cornerRadius Radius clipping the surface and controls.
 * @param content Controls inside the surface.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformBlurredPanel(
    modifier: Modifier,
    cornerRadius: Dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surface
    ) {
        content()
    }
}
