package com.shifthackz.aisdv1.presentation.blog.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState

/**
 * Applies the app palette and supplies predictable cover results for design-time rendering.
 *
 * @param content Blog screen or component rendered without service or network setup.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun BlogPreviewTheme(content: @Composable () -> Unit) {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        val releaseColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
        val engineeringColor = MaterialTheme.colorScheme.tertiaryContainer.toArgb()
        val coverHandler = remember(releaseColor, engineeringColor) {
            val available = AsyncImagePreviewHandler { request: ImageRequest ->
                val color = if (request.data == BlogPreviewFixtures.releasePost.coverUrl) {
                    releaseColor
                } else {
                    engineeringColor
                }
                ColorImage(color = color, width = 640, height = 360)
            }
            AsyncImagePreviewHandler { loader, request ->
                if (request.data == BlogPreviewFixtures.unavailableCoverUrl) {
                    AsyncImagePainter.State.Error(
                        painter = null,
                        result = ErrorResult(
                            null,
                            request,
                            IllegalStateException("Preview cover unavailable")
                        ),
                    )
                } else {
                    available.handle(loader, request)
                }
            }
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides coverHandler) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                content = content,
            )
        }
    }
}
