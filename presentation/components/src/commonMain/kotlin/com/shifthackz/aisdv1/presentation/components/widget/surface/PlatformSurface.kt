package com.shifthackz.aisdv1.presentation.components.widget.surface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor

/**
 * Draws a themed Material surface on Android and tinted native glass on supported iOS versions.
 * Content stays in its original composition, retaining focus, state, gestures and accessibility.
 * Native child controls inherit clear backing over glass or the resolved flat surface color.
 *
 * @param modifier Surface sizing, placement and outer spacing; content determines unconstrained size.
 * @param shape Outer clipping and corner radii, including asymmetric rounded card segments.
 * @param color Background tint, retaining alpha over the enclosing surface.
 * @param contentColor Default foreground color inherited by child text and icons.
 * @param border Optional outline drawn above the background and content.
 * @param tonalElevation Material tonal elevation, accumulated with enclosing surfaces.
 * @param shadowElevation Material shadow elevation, independent of tonal elevation.
 * @param glassInset Space retaining the native glass rim inside the clipping bounds, in logical units.
 * @param isTopEdgeVisible Whether glass draws its top rim; false joins a preceding lazy card segment.
 * @param isBottomEdgeVisible Whether glass draws its bottom rim; false joins a following lazy card segment.
 * @param onClick Optional surface action; null leaves input handling to child content.
 * @param isEnabled Whether the optional surface action can run.
 * @param content Controls drawn above the platform background.
 * @author Dmitriy Moroz
 */
@Composable
fun PlatformSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    border: BorderStroke? = null,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    glassInset: Dp = 2.dp,
    isTopEdgeVisible: Boolean = true,
    isBottomEdgeVisible: Boolean = true,
    onClick: (() -> Unit)? = null,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    require(glassInset >= 0.dp) { "Glass inset must be nonnegative" }
    val shouldShowGlass = isPlatformSurfaceGlassAvailable() && color.alpha > 0f
    val parentColor =
        LocalPlatformControlBackgroundColor.current ?: MaterialTheme.colorScheme.background
    val surfaceContent: @Composable () -> Unit = {
        val resolvedColor = if (color == MaterialTheme.colorScheme.surface) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(LocalAbsoluteTonalElevation.current)
        } else color
        Box(propagateMinConstraints = true) {
            if (shouldShowGlass) {
                PlatformSurfaceBackground(
                    modifier = Modifier.matchParentSize(),
                    color = resolvedColor,
                    backingColor = parentColor,
                    shape = shape,
                    inset = glassInset,
                    isTopEdgeVisible = isTopEdgeVisible,
                    isBottomEdgeVisible = isBottomEdgeVisible,
                )
            }
            CompositionLocalProvider(
                LocalPlatformControlBackgroundColor provides if (shouldShowGlass) Color.Transparent
                else resolvedColor.compositeOver(parentColor),
            ) {
                content()
            }
        }
    }
    if (onClick == null) {
        Surface(
            modifier = modifier, shape = shape,
            color = if (shouldShowGlass) Color.Transparent else color,
            contentColor = contentColor, border = border,
            tonalElevation = tonalElevation, shadowElevation = shadowElevation,
            content = surfaceContent,
        )
    } else {
        Surface(
            onClick = onClick, enabled = isEnabled,
            modifier = modifier, shape = shape,
            color = if (shouldShowGlass) Color.Transparent else color,
            contentColor = contentColor, border = border,
            tonalElevation = tonalElevation, shadowElevation = shadowElevation,
            content = surfaceContent,
        )
    }
}

/**
 * Reports whether the current platform, route visibility and preview support a native glass backdrop.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun isPlatformSurfaceGlassAvailable(): Boolean

/**
 * Draws only the native decorative backdrop; shared content retains ownership of input.
 *
 * @param modifier Bounds measured from the enclosing surface content.
 * @param color Resolved themed tint, including tonal elevation.
 * @param backingColor Parent canvas filling the native interop cutout.
 * @param shape Surface corners resolved in the current density and layout direction.
 * @param inset Space retaining the glass rim inside its bounds.
 * @param isTopEdgeVisible Whether this segment starts a glass surface.
 * @param isBottomEdgeVisible Whether this segment ends a glass surface.
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun PlatformSurfaceBackground(
    color: Color,
    backingColor: Color,
    shape: Shape,
    inset: Dp,
    isTopEdgeVisible: Boolean,
    isBottomEdgeVisible: Boolean,
    modifier: Modifier = Modifier,
)
