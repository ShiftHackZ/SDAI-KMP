package com.shifthackz.aisdv1.presentation.gallery.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.sharing.shareFile
import com.shifthackz.aisdv1.core.sharing.shareText
import com.shifthackz.aisdv1.presentation.components.image.ImageSaveResult
import com.shifthackz.aisdv1.presentation.components.image.ImageSaver
import com.shifthackz.aisdv1.presentation.components.utils.OriginalImageFileExporter
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Performs Android image export, native sharing and clipboard operations for gallery detail.
 * Original image files are staged on IO; UI handoffs run on the immediate dispatcher.
 * Cancellation propagates while ordinary platform failures become explicit action failures.
 *
 * @param context Provides clipboard access and launches native text/file sharing.
 * @param fileProviderDescriptor Supplies the manifest authority for staged original images.
 * @param dispatchersProvider Separates image-file IO from immediate platform UI operations.
 * @param originals Writes bounded original PNG/JPEG/WebP bytes to a private share artifact.
 * @param imageSaver Saves original bytes through the normal media-export adapter.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidGalleryDetailPlatformActions(
    private val context: Context,
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val dispatchersProvider: DispatchersProvider,
    private val originals: OriginalImageFileExporter,
    private val imageSaver: ImageSaver,
) : GalleryDetailPlatformActions {

    /**
     * Saves original image bytes through the native media library adapter.
     *
     * @param base64 Original encoded image, optionally prefixed by a Base64 data URI.
     *
     * @return Native completion/support/failure status; cancellation is not converted into success.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun saveImage(base64: String): GalleryDetailActionResult =
        when (val result = imageSaver.save(base64)) {
            ImageSaveResult.Saved -> GalleryDetailActionResult.Done
            ImageSaveResult.Unsupported -> GalleryDetailActionResult.Unsupported
            is ImageSaveResult.Failed -> GalleryDetailActionResult.Failed(result.message)
        }

    /**
     * Stages original image bytes and presents native sharing.
     *
     * @param base64 Original encoded image, optionally prefixed by a Base64 data URI.
     *
     * @return Done after native handoff, which does not establish recipient delivery.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun shareImage(base64: String): GalleryDetailActionResult =
        runPlatformAction {
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

    /**
     * Presents native text sharing for generation parameters or prompts.
     *
     * @param text Already formatted user-visible text to share.
     *
     * @return Native handoff/support/failure status.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun shareText(text: String): GalleryDetailActionResult =
        runPlatformAction {
            withContext(dispatchersProvider.immediate) {
                context.shareText(text)
            }
        }

    /**
     * Copies formatted generation text to the platform clipboard.
     *
     * @param text Prompt or metadata text to copy.
     *
     * @return Clipboard operation/support/failure status.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun copyText(text: String): GalleryDetailActionResult =
        runPlatformAction {
            withContext(dispatchersProvider.immediate) {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, text))
            }
        }

    private suspend inline fun runPlatformAction(crossinline action: suspend () -> Unit): GalleryDetailActionResult =
        try {
            action()
            GalleryDetailActionResult.Done
        } catch (e: CancellationException) {
            throw e
        } catch (t: Exception) {
            GalleryDetailActionResult.Failed(t.message ?: "Unable to complete action")
        }

    private companion object {
        const val CLIP_LABEL = "SDAI gallery detail"
    }
}
