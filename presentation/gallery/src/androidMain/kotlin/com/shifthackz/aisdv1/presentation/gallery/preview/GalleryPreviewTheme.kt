package com.shifthackz.aisdv1.presentation.gallery.preview

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
 * Applies the app palette without system-bar effects and keeps metadata link activation inert.
 *
 * @param content Fixed gallery scaffold, detail surface or modal content to display.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryPreviewTheme(content: @Composable () -> Unit) {
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
 * Remembers a tiny checkerboard at a fixed aspect ratio without asynchronous image-cache requests.
 *
 * @param width Requested sample width in pixels.
 * @param height Requested sample height in pixels.
 * @author Dmitriy Moroz
 */
@Composable
internal fun rememberGalleryPreviewBitmap(width: Int = 32, height: Int = 32): ImageBitmap =
    remember(width, height) {
        val bytes = Base64.decode(GalleryPreviewFixtures.ImageBase64, Base64.DEFAULT)
        val bitmap = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        Bitmap.createScaledBitmap(bitmap, width, height, false).asImageBitmap()
    }
