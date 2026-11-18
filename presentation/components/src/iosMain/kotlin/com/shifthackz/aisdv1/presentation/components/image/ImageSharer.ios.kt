package com.shifthackz.aisdv1.presentation.components.image

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSFileManager
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

/**
 * Creates a UIKit share-sheet adapter using a temporary file containing original image bytes.
 *
 * @author Dmitriy Moroz
 */
actual fun createPlatformImageSharer(): ImageSharer = IosImageSharer()

/**
 * Owns a temporary original image until native sharing completes or presentation cannot begin.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalForeignApi::class)
private class IosImageSharer : ImageSharer {

    /**
     * Stages PNG/JPEG/WebP bytes and presents a share sheet from the current UIKit presenter.
     * The temporary file is removed on failed presentation or the native share completion callback.
     *
     * @param base64 Original Base64 image with an optional data-URI header.
     * @return Sent once the sheet is presented, otherwise decode/file/presenter failure details.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun share(base64: String): ImageShareResult {
        val file = runCatching {
            val data = base64.decodeGeneratedImageData()
            val header =
                base64.substringAfter("base64,", base64).take(64).filterNot(Char::isWhitespace)
            val extension = when {
                header.startsWith("iVBORw0KGgo") -> "png"
                header.startsWith("/9j/") -> "jpg"
                header.startsWith("UklGR") -> "webp"
                else -> throw IllegalStateException()
            }
            val url =
                NSURL.fileURLWithPath(NSTemporaryDirectory() + "sdai_" + NSUUID().UUIDString + "." + extension)
            check(data.writeToURL(url, true))
            url
        }.getOrElse { t ->
            return ImageShareResult.Failed(t.message ?: "Unable to decode generated image")
        }

        return suspendCancellableCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                if (!continuation.isActive) {
                    NSFileManager.defaultManager.removeItemAtURL(file, null); return@dispatch_async
                }
                val presenter = UIApplication.sharedApplication
                    .rootViewController()
                    ?.topMostPresentedViewController()
                if (presenter == null) {
                    if (continuation.isActive) {
                        NSFileManager.defaultManager.removeItemAtURL(file, null)
                        continuation.resume(ImageShareResult.Failed("Unable to open share sheet"))
                    }
                    return@dispatch_async
                }

                val controller = UIActivityViewController(
                    activityItems = listOf(file),
                    applicationActivities = null,
                )
                controller.completionWithItemsHandler =
                    { _, _, _, _ -> NSFileManager.defaultManager.removeItemAtURL(file, null) }
                controller.modalPresentationStyle = UIModalPresentationFullScreen
                presenter.presentViewController(
                    viewControllerToPresent = controller,
                    animated = true,
                    completion = {
                        if (continuation.isActive) {
                            continuation.resume(ImageShareResult.Sent)
                        }
                    },
                )
            }
        }
    }
}


private fun UIApplication.rootViewController(): UIViewController? =
    keyWindow?.rootViewController
        ?: windows
            .filterIsInstance<UIWindow>()
            .firstOrNull(UIWindow::isKeyWindow)
            ?.rootViewController
        ?: windows
            .filterIsInstance<UIWindow>()
            .firstOrNull()
            ?.rootViewController


private fun UIViewController.topMostPresentedViewController(): UIViewController =
    presentedViewController?.topMostPresentedViewController() ?: this
