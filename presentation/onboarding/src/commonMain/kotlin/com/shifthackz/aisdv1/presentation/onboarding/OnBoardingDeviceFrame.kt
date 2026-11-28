package com.shifthackz.aisdv1.presentation.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.frame.PhoneFrame

/**
 * Adapts the current window to a scaled, noninteractive phone or foldable illustration.
 * Display proportions stay within 9:16 and 16:9 so elongated phone windows retain a natural frame.
 * Resizing and rotation update its shape, and the bundled screen reflows inside the illustrated display.
 * The miniature uses the host font scale while scaling density to fit its measured frame.
 *
 * @param modifier Space available to the illustration inside its onboarding page.
 * @param content Bundled screen receiving a modifier that fills the illustrated display.
 * @author Dmitriy Moroz
 */
@Composable
internal fun OnBoardingDeviceFrame(
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    val windowSize = LocalWindowInfo.current.containerSize
    val hostDensity = LocalDensity.current
    BoxWithConstraints(modifier.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        val viewportWidth = if (windowSize.width > 0) {
            windowSize.width / hostDensity.density
        } else maxWidth.value.coerceAtLeast(1f)
        val viewportHeight = if (windowSize.height > 0) {
            windowSize.height / hostDensity.density
        } else maxHeight.value.coerceAtLeast(1f)
        val aspectRatio = (viewportWidth / viewportHeight).coerceIn(9f / 16f, 16f / 9f)
        PhoneFrame(isLandscape = aspectRatio > 1f) {
            BoxWithConstraints(Modifier.aspectRatio(aspectRatio)) {
                if (constraints.maxWidth > 0 && constraints.maxHeight > 0) {
                    val previewDensity = Density(
                        density = constraints.maxWidth / viewportWidth,
                        fontScale = hostDensity.fontScale,
                    )
                    CompositionLocalProvider(LocalDensity provides previewDensity) {
                        Box(Modifier.fillMaxSize()) {
                            content(Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
