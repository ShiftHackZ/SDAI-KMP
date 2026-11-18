package com.shifthackz.aisdv1.presentation.components.image

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Creates a Photos-library adapter; the first save requests add-only permission when necessary.
 *
 * @author Dmitriy Moroz
 */
actual fun createPlatformImageSaver(): ImageSaver = IosImageSaver()

/**
 * Adds original encoded bytes to Photos without recompressing image pixels.
 *
 * @author Dmitriy Moroz
 */
private class IosImageSaver : ImageSaver {

    /**
     * Decodes a bounded original payload, requests add-only Photos access and waits for its write callback.
     * Cancellation stops coroutine delivery but does not retract a Photos transaction already started.
     *
     * @param base64 Original Base64 image with an optional data-URI header.
     * @return Saved after a successful Photos transaction, otherwise permission/decode/write failure details.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun save(base64: String): ImageSaveResult {
        val data = runCatching { base64.decodeGeneratedImageData() }.getOrElse { t ->
            return ImageSaveResult.Failed(t.message ?: "Unable to decode generated image")
        }

        return suspendCancellableCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                if (!continuation.isActive) return@dispatch_async
                PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
                    if (status != PHAuthorizationStatusAuthorized && status != PHAuthorizationStatusLimited) {
                        if (continuation.isActive) {
                            continuation.resume(ImageSaveResult.Failed("Photos permission was not granted"))
                        }
                        return@requestAuthorizationForAccessLevel
                    }

                    PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                        changeBlock = {
                            PHAssetCreationRequest.creationRequestForAsset()
                                .addResourceWithType(PHAssetResourceTypePhoto, data, options = null)
                        },
                        completionHandler = { success, error ->
                            val result = if (success) {
                                ImageSaveResult.Saved
                            } else {
                                ImageSaveResult.Failed(
                                    error?.localizedDescription ?: "Unable to save image to Photos",
                                )
                            }
                            if (continuation.isActive) {
                                continuation.resume(result)
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * Decodes original image bytes for Photos or sharing, accepting an optional data-URI header.
 * Whitespace is removed; encoded input is limited to 32 MiB characters and decoded content to 24 MiB.
 *
 * @receiver Original Base64-encoded media bytes.
 * @return Nonempty immutable native data containing the original encoded image.
 * @throws IllegalArgumentException If content is empty, malformed or exceeds admission limits.
 *
 * @author Dmitriy Moroz
 */
@OptIn(BetaInteropApi::class, ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
internal fun String.decodeGeneratedImageData(): NSData {
    require(length <= 32 * 1024 * 1024)
    val raw = substringAfter("base64,", this).filterNot(Char::isWhitespace)
    val bytes = Base64.Default.decode(raw)
    require(bytes.isNotEmpty() && bytes.size <= 24 * 1024 * 1024)
    return bytes.usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = bytes.size.toULong(),
        )
    }
}
