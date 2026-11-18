package com.shifthackz.aisdv1.presentation.components.image

import android.content.Context
import android.util.Base64
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.sharing.shareFile
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.presentation.components.utils.OriginalImageFileExporter
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Returns an unsupported placeholder; Android application DI supplies the permission-aware media adapter.
 *
 * @author Dmitriy Moroz
 */
actual fun createPlatformImageSaver(): ImageSaver = UnsupportedAndroidImageSaver

/**
 * Returns an unsupported placeholder; Android application DI supplies the context/file-provider share adapter.
 *
 * @author Dmitriy Moroz
 */
actual fun createPlatformImageSharer(): ImageSharer = UnsupportedAndroidImageSharer


private object UnsupportedAndroidImageSaver : ImageSaver {

    override suspend fun save(base64: String): ImageSaveResult =
        ImageSaveResult.Unsupported
}


private object UnsupportedAndroidImageSharer : ImageSharer {

    override suspend fun share(base64: String): ImageShareResult =
        ImageShareResult.Unsupported
}

/**
 * Exports original encoded image bytes through Android media storage on the IO dispatcher.
 * Cancellation propagates; ordinary decoding/export failures become a save failure result.
 *
 * @param mediaStoreGateway Writes original bytes into the platform's configured export destination.
 * @param dispatchersProvider Supplies the IO dispatcher for decoding and filesystem/media-store work.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidImageSaver(

    private val mediaStoreGateway: MediaStoreGateway,

    private val dispatchersProvider: DispatchersProvider,
) : ImageSaver {

    /**
     * Decodes the original payload and exports it under a timestamp-based name.
     *
     * @param base64 Original Base64 image with an optional data-URI header.
     * @return Saved on completed export, or failure details; coroutine cancellation propagates.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun save(base64: String): ImageSaveResult =
        runSavingAction {
            withContext(dispatchersProvider.io) {
                mediaStoreGateway.exportToFile(
                    fileName = createImageFileName(),
                    content = base64.toImageBytes(),
                )
            }
        }
}

/**
 * Stages an original image file for Android sharing and cleans it if the native handoff fails.
 * Successfully handed-off files remain available for recipients through the app file provider.
 *
 * @param context Launches Android's native file-share flow.
 * @param fileProviderDescriptor Supplies the manifest authority granting access to the staged file.
 * @param dispatchersProvider Separates file staging from the immediate UI handoff.
 * @param originals Writes bounded original image bytes to a private share artifact.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidImageSharer(

    private val context: Context,

    private val fileProviderDescriptor: FileProviderDescriptor,

    private val dispatchersProvider: DispatchersProvider,

    private val originals: OriginalImageFileExporter,
) : ImageSharer {

    /**
     * Stages original bytes on IO, then launches the native share flow on the immediate UI dispatcher.
     *
     * @param base64 Original Base64 image with an optional data-URI header.
     * @return Sent after native handoff, or failure details; cancellation propagates.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun share(base64: String): ImageShareResult =
        runSharingAction {
            val file = withContext(dispatchersProvider.io) {
                originals.export(base64, UUID.randomUUID().toString())
            }
            var hasLaunchedShare = false
            try {
                withContext(dispatchersProvider.immediate) {
                    context.shareFile(
                        file = file,
                        fileProviderPath = fileProviderDescriptor.providerPath,
                        fileMimeType = when (file.extension) {
                            "png" -> "image/png"; "webp" -> "image/webp"; else -> "image/jpeg"
                        },
                    )
                    hasLaunchedShare = true
                }
            } finally {
                if (!hasLaunchedShare) file.delete()
            }
        }
}


private suspend inline fun runSavingAction(crossinline action: suspend () -> Unit): ImageSaveResult =
    try {
        action()
        ImageSaveResult.Saved
    } catch (e: CancellationException) {
        throw e
    } catch (t: Exception) {
        ImageSaveResult.Failed(t.message ?: "Unable to save image")
    }


private suspend inline fun runSharingAction(crossinline action: suspend () -> Unit): ImageShareResult =
    try {
        action()
        ImageShareResult.Sent
    } catch (e: CancellationException) {
        throw e
    } catch (t: Exception) {
        ImageShareResult.Failed(t.message ?: "Unable to share image")
    }


private fun String.toImageBytes(): ByteArray {
    if (length > 32 * 1024 * 1024) throw IllegalStateException()
    val raw = substringAfter("base64,", this)
    return Base64.decode(raw, Base64.DEFAULT)
}


@OptIn(ExperimentalTime::class)
private fun createImageFileName(): String =
    "sdai_${Clock.System.now().toEpochMilliseconds()}"
