package com.shifthackz.aisdv1.presentation.components.widget.help

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface
import com.shifthackz.aisdv1.presentation.components.widget.surface.isPlatformSurfaceGlassAvailable

/**
 * Keeps Material tooltip sizing and typography with a translucent native glass backdrop on
 * supported iOS versions. Android and platform previews retain the Material surface and shadow.
 * The floating native backing stays clear so its material samples the application underneath it.
 *
 * @param title Heading and dismissal controls displayed above the explanation.
 * @param modifier Measurement and placement of the complete tooltip.
 * @param content Localized explanation rendered in its original popup composition.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TooltipScope.PlatformRichTooltip(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shouldShowGlass = isPlatformSurfaceGlassAvailable()
    val colors = MaterialTheme.colorScheme
    val tooltipColors = TooltipDefaults.richTooltipColors(
        containerColor = if (shouldShowGlass) Color.Transparent else colors.surfaceVariant,
        contentColor = colors.onSurfaceVariant,
        titleContentColor = colors.onSurface,
        actionContentColor = colors.onSurface,
    )
    if (shouldShowGlass) {
        CompositionLocalProvider(LocalPlatformControlBackgroundColor provides Color.Transparent) {
            PlatformSurface(
                modifier = modifier,
                shape = TooltipDefaults.richTooltipContainerShape,
                color = colors.surfaceVariant.copy(alpha = 0.55f),
                shouldClipBackground = true,
            ) {
                RichTooltip(
                    colors = tooltipColors,
                    title = title,
                    shadowElevation = 0.dp,
                    text = content,
                )
            }
        }
    } else {
        RichTooltip(
            modifier = modifier,
            colors = tooltipColors,
            title = title,
            text = content,
        )
    }
}
