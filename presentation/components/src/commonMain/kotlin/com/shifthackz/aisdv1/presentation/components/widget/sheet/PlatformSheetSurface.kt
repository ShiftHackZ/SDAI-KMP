@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.components.widget.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
 * Supplies a Material sheet surface on Android and the drawer's tinted system blur on iOS.
 * Content determines its height and stays in the caller's composition, retaining state and owners.
 * The surface can wrap modal sheets, scaffold sheet content or another independently hosted panel.
 *
 * @param modifier Surface measurement, placement and outside spacing.
 * @param shape Clipping shared by the backdrop and foreground controls.
 * @param containerColor Material background or native blur tint, including its alpha.
 * @param contentColor Foreground inherited by sheet labels and icons.
 * @param tonalElevation Material tonal elevation, accumulated with enclosing surfaces.
 * @param content Sheet controls rendered once above the platform backdrop.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun PlatformSheetSurface(
    modifier: Modifier = Modifier,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val shouldUseNativeBlur = isPlatformSheetBlurAvailable()
    val parentColor =
        LocalPlatformControlBackgroundColor.current ?: MaterialTheme.colorScheme.background
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (shouldUseNativeBlur) Color.Transparent else containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
    ) {
        val resolvedColor = if (containerColor == MaterialTheme.colorScheme.surface) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(LocalAbsoluteTonalElevation.current)
        } else containerColor
        Box(propagateMinConstraints = true) {
            if (shouldUseNativeBlur) {
                PlatformSheetBackground(
                    color = resolvedColor,
                    modifier = Modifier.matchParentSize()
                )
            }
            CompositionLocalProvider(
                LocalPlatformControlBackgroundColor provides if (shouldUseNativeBlur) Color.Transparent
                else resolvedColor.compositeOver(parentColor),
            ) {
                content()
            }
        }
    }
}

/**
 * Reports whether native sheet blur can render in the current platform, preview and retained route.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun isPlatformSheetBlurAvailable(): Boolean

/**
 * Draws the drawer-style native backdrop beneath controls in the same composition.
 *
 * @param color App tint mixed over the system material.
 * @param modifier Bounds measured from the enclosing sheet surface.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun PlatformSheetBackground(color: Color, modifier: Modifier = Modifier)
