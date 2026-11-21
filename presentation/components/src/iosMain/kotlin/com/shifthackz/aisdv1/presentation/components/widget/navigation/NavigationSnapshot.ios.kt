package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy
import org.jetbrains.skia.Image as SkiaImage

/**
 * Freezes the current UIKit hierarchy into Compose pixels so native overlays cannot cover the
 * moving foreground screen. The existing live hierarchy keeps its accessibility and input owners.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberNavigationSnapshotCapture(): () -> ImageBitmap? {
    val controller = LocalUIViewController.current
    return remember(controller) {
        {
            val view = controller.view
            val renderer = UIGraphicsImageRenderer(bounds = view.bounds)
            val image =
                renderer.imageWithActions { view.drawViewHierarchyInRect(view.bounds, false) }
            val data = UIImagePNGRepresentation(image)
            if (data == null || data.length == 0uL) null else {
                val bytes = ByteArray(data.length.toInt())
                bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
                SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
            }
        }
    }
}
