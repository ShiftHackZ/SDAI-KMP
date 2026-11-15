package com.shifthackz.aisdv1.data.gateway.mediastore

import android.net.Uri
import android.os.Environment
import com.shifthackz.aisdv1.data.export.MediaStoreImageFormatResolver
import com.shifthackz.aisdv1.domain.entity.MediaStoreInfo
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import java.io.File
import java.io.IOException
import kotlin.jvm.Synchronized

/**
 * Writes original encoded images to the public Downloads/SDAI folder on Android 12 and earlier.
 * Keeps the existing external-storage permission behavior while selecting the suffix from the bytes.
 *
 * @param formatResolver Recognizes supported encoded headers before opening the destination.
 * @author Dmitriy Moroz
 */
@Deprecated("Deprecated since Android 12, it is here to support old devices.")
internal class MediaStoreGatewayOldImpl(
    private val formatResolver: MediaStoreImageFormatResolver,
) : MediaStoreGateway {

    /**
     * Writes the bytes unchanged under their detected extension, retaining pre-existing files
     * when a write fails. Newly created partial files are removed and cleanup errors are attached
     * to the original failure.
     * Serializes this gateway's exports so failure cleanup cannot delete a concurrent call's output.
     *
     * @param fileName Destination filename stem without an extension.
     * @param content Original encoded PNG, JPEG or WebP image bytes.
     * @throws IllegalArgumentException When the image header is unsupported.
     * @throws IOException When the directory or file cannot be created or written.
     * @author Dmitriy Moroz
     */
    @Synchronized
    override fun exportToFile(fileName: String, content: ByteArray) {
        val format = formatResolver.resolve(content)
        val dir = File(Environment.getExternalStorageDirectory().path + DIR_PATH)
        if (!dir.isDirectory && !dir.mkdirs() && !dir.isDirectory) {
            throw IOException("Unable to create media export directory")
        }
        val file = File(dir, "$fileName.${format.extension}")
        val hasCreatedFile = file.createNewFile()
        try {
            file.writeBytes(content)
        } catch (failure: Throwable) {
            if (hasCreatedFile) {
                try {
                    if (!file.delete() && file.exists()) {
                        throw IOException("Unable to remove incomplete media export")
                    }
                } catch (cleanupFailure: Throwable) {
                    if (cleanupFailure !== failure) failure.addSuppressed(cleanupFailure)
                }
            }
            throw failure
        }
    }

    /**
     * Reports the existing Downloads/SDAI directory without creating it.
     *
     * @return Child count and directory URI, or empty information when the directory is absent.
     * @author Dmitriy Moroz
     */
    override fun getInfo(): MediaStoreInfo {
        val dirPath = Environment.getExternalStorageDirectory().path + DIR_PATH
        val dir = File(dirPath)
        if (dir.exists() && dir.isDirectory) {
            return MediaStoreInfo(
                count = dir.listFiles()?.size ?: 0,
                folderUri = Uri.fromFile(dir).toString(),
            )
        }
        return MediaStoreInfo()
    }

    private companion object {
        /**
         * Public export folder relative to the external-storage root.
         *
         * @author Dmitriy Moroz
         */
        private const val DIR_PATH = "/Download/SDAI"
    }
}
