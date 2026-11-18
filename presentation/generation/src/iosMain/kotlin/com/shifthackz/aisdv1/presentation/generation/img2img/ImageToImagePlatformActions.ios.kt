package com.shifthackz.aisdv1.presentation.generation.img2img

import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget

import com.shifthackz.aisdv1.core.localization.Localization
import kotlinx.cinterop.useContents
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Creates the remembered native picker for explicit image-to-image input selection.
 * Conversion and normalization use background execution; cancellation and unsupported picker
 * outcomes remain distinct from invalid or rejected image content.
 *
 * @param budget Encoded/pixel admission for native picker conversion.
 *
 * @return Native input actions using the composition root's admission budget.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberImageToImagePlatformActions(budget: OriginalImageBudget): ImageToImagePlatformActions {
    val picker = remember { IosImagePicker() }
    return remember(picker, budget) {
        object : ImageToImagePlatformActions {
            override suspend fun pickImage(source: ImageToImagePickSource): ImageToImagePickResult =
                when (val outcome = picker.pick(source)) {
                    IosImagePickOutcome.Cancelled -> ImageToImagePickResult.Cancelled
                    is IosImagePickOutcome.Failed -> ImageToImagePickResult.Failed(outcome.message)
                    IosImagePickOutcome.Unsupported -> ImageToImagePickResult.Unsupported
                    is IosImagePickOutcome.Selected -> withContext(Dispatchers.Default) {
                        if (!outcome.image.fitsPixelBudget(budget.maxPixels)) return@withContext ImageToImagePickResult.Failed(
                            Localization.string("error_invalid")
                        )
                        outcome.image
                            .base64Jpeg()
                            ?.let { cropBase64ImageToSquare(it, budget) }
                            ?.let(ImageToImagePickResult::Selected)
                            ?: ImageToImagePickResult.Failed("Unable to read selected image")
                    }
                }
        }
    }
}

private sealed interface IosImagePickOutcome {

    data class Selected(val image: UIImage) : IosImagePickOutcome

    data object Cancelled : IosImagePickOutcome

    data object Unsupported : IosImagePickOutcome

    data class Failed(val message: String) : IosImagePickOutcome
}

/**
 * Owns one UIKit image-picker delegate and waiting continuation; overlapping acquisition is rejected.
 *
 * @author Dmitriy Moroz
 */
private class IosImagePicker : NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {

    private var continuation: CancellableContinuation<IosImagePickOutcome>? = null

    /**
     * Presents camera/photo selection on the main queue and waits for its delegate result.
     * Coroutine cancellation detaches the waiting consumer; native dismissal remains delegate-owned.
     *
     * @param source Camera capture or photo-library selection requested by the form.
     *
     * @return Selected UIKit image, cancellation, unsupported source or presentation failure.
     *
     * @author Dmitriy Moroz
     */
    suspend fun pick(source: ImageToImagePickSource): IosImagePickOutcome =
        suspendCancellableCoroutine { nextContinuation ->
            if (continuation != null) {
                nextContinuation.resume(
                    IosImagePickOutcome.Failed("Another image picker is already active"),
                )
                return@suspendCancellableCoroutine
            }
            continuation = nextContinuation
            nextContinuation.invokeOnCancellation {
                if (continuation == nextContinuation) {
                    continuation = null
                }
            }
            dispatch_async(dispatch_get_main_queue()) {
                present(source)
            }
        }


    private fun present(source: ImageToImagePickSource) {
        val sourceType = when (source) {
            ImageToImagePickSource.Camera -> UI_IMAGE_PICKER_SOURCE_TYPE_CAMERA
            ImageToImagePickSource.Gallery -> UI_IMAGE_PICKER_SOURCE_TYPE_PHOTO_LIBRARY
        }
        if (!UIImagePickerController.isSourceTypeAvailable(sourceType)) {
            resume(IosImagePickOutcome.Unsupported)
            return
        }

        val presenter = UIApplication.sharedApplication
            .rootViewController()
            ?.topMostPresentedViewController()
        if (presenter == null) {
            resume(IosImagePickOutcome.Failed("Unable to open image picker"))
            return
        }

        val controller = UIImagePickerController()
        controller.sourceType = sourceType
        controller.delegate = this
        controller.modalPresentationStyle = UIModalPresentationFullScreen
        presenter.presentViewController(
            viewControllerToPresent = controller,
            animated = true,
            completion = null,
        )
    }

    /**
     * Dismisses the native picker and reports its original UIImage to the waiting consumer.
     *
     * @param picker UIKit picker that completed selection.
     * @param didFinishPickingMediaWithInfo Native result map containing the original image when available.
     *
     * @author Dmitriy Moroz
     */
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val result = image
            ?.let(IosImagePickOutcome::Selected)
            ?: IosImagePickOutcome.Failed("Unable to read selected image")
        picker.dismissViewControllerAnimated(true, completion = null)
        resume(result)
    }

    /**
     * Dismisses the native picker and reports explicit user cancellation.
     *
     * @param picker UIKit picker dismissed by the user.
     *
     * @author Dmitriy Moroz
     */
    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        resume(IosImagePickOutcome.Cancelled)
    }


    private fun resume(result: IosImagePickOutcome) {
        val current = continuation ?: return
        continuation = null
        current.resume(result)
    }
}


@OptIn(ExperimentalForeignApi::class)
private fun UIImage.base64Jpeg(): String? {
    val data = UIImageJPEGRepresentation(this, compressionQuality = 0.95) ?: return null
    val length = data.length.toInt()
    if (length <= 0) return null
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    return Base64.encode(bytes)
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


private val UI_IMAGE_PICKER_SOURCE_TYPE_PHOTO_LIBRARY =
    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary


private val UI_IMAGE_PICKER_SOURCE_TYPE_CAMERA =
    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera

/**
 * Checks native pixel coordinates before JPEG conversion can rasterize the selected UIImage.
 *
 * @param maxPixels Full-resolution conversion limit supplied by composition.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.fitsPixelBudget(maxPixels: Long): Boolean = size.useContents {
    val pixelWidth = width * scale
    val pixelHeight = height * scale
    pixelWidth.isFinite() && pixelHeight.isFinite() && pixelWidth > 0 && pixelHeight > 0 && pixelWidth * pixelHeight <= maxPixels
}
