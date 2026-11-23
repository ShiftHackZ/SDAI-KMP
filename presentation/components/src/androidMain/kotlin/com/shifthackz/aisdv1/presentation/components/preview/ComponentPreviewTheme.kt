package com.shifthackz.aisdv1.presentation.components.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState

/**
 * Applies the app palette without system-bar effects and makes preview link activation inert.
 *
 * @param content Fixed component content, without native acquisition or live state binding.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ComponentPreviewTheme(content: @Composable () -> Unit) {
    val uriHandler = remember {
        object : UriHandler {
            override fun openUri(uri: String) = Unit
        }
    }
    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        AiSdAppTheme(
            state = AiSdAppThemeState(
                shouldUseSystemDarkTheme = false,
                isDarkTheme = isSystemInDarkTheme(),
                shouldUseSystemColorPalette = false,
            ),
            applySystemBars = false,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                content()
            }
        }
    }
}

/**
 * Remembers fixed checkerboard pixels without requesting native image acquisition or asynchronous decoding.
 *
 * @param width Sample width in pixels.
 * @param height Sample height in pixels.
 * @author Dmitriy Moroz
 */
@Composable
internal fun rememberComponentPreviewBitmap(width: Int = 128, height: Int = 128): ImageBitmap =
    remember(width, height) {
        val bytes = Base64.decode(ComponentPreviewImageBase64, Base64.DEFAULT)
        val image = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        Bitmap.createScaledBitmap(image, width, height, false).asImageBitmap()
    }

/**
 * Bundled checkerboard fixture for deterministic image slots.
 *
 * @author Dmitriy Moroz
 */
private const val ComponentPreviewImageBase64 =
    "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAQklEQVR4nGO4c+IrVpQXugorIlU9w6gFoxYMAQuoZRAu9aMWjFowFCyglkG41I9aMGrBULCAWgbhUj9qwagFQ8ACAP32DHmKhrEjAAAAAElFTkSuQmCC"
