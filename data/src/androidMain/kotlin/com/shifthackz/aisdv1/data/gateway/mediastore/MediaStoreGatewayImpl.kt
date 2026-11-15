package com.shifthackz.aisdv1.data.gateway.mediastore

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.shifthackz.aisdv1.core.common.extensions.uriFromFile
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.data.export.MediaStoreImageFormatResolver
import com.shifthackz.aisdv1.domain.entity.MediaStoreInfo
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import java.io.File
import java.io.IOException
import kotlin.jvm.Synchronized

/**
 * Writes original image bytes to Downloads/SDAI through MediaStore on Android 12L and later.
 * Filename suffix and MIME type come from the encoded image, preserving upstream metadata.
 *
 * @param context Application context supplying the media content resolver.
 * @param fileProviderDescriptor FileProvider authority used to expose the folder returned by [getInfo].
 * @param formatResolver Recognizes supported encoded headers before any destination is opened.
 * @author Dmitriy Moroz
 */
internal class MediaStoreGatewayImpl(
    private val context: Context,
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val formatResolver: MediaStoreImageFormatResolver,
) : MediaStoreGateway {

    /**
     * Saves the unmodified image under its detected extension and MIME type.
     * Reuses only a matching entry in Downloads/SDAI. Failed writes remove newly created entries;
     * a pre-existing destination is retained, and cleanup failures are attached to the write failure.
     * Serializes this gateway's exports so failure cleanup cannot delete a concurrent call's output.
     *
     * @param fileName Destination filename stem without an extension.
     * @param content Original encoded PNG, JPEG or WebP image bytes.
     * @throws IllegalArgumentException When the image header is unsupported.
     * @throws IOException When the media provider cannot open or write the destination.
     * @author Dmitriy Moroz
     */
    @Synchronized
    override fun exportToFile(fileName: String, content: ByteArray) {
        val format = formatResolver.resolve(content)
        val displayName = "$fileName.${format.extension}"
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/SDAI/"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }
        val resolver = context.contentResolver
        val volumeUri = MediaStore.Files.getContentUri("external")
        val existingFileUri = (resolver.query(
            volumeUri,
            arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND " +
                "${MediaStore.MediaColumns.MIME_TYPE} = ? AND " +
                "${MediaStore.MediaColumns.RELATIVE_PATH} = ?",
            arrayOf(displayName, format.mimeType, relativePath),
            null,
        ) ?: throw IOException("Unable to query media export destination")).use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                ContentUris.withAppendedId(volumeUri, id)
            } else {
                null
            }
        }
        val fileUri = existingFileUri ?: resolver.insert(volumeUri, contentValues)
            ?: throw IOException("Unable to create media export destination")
        try {
            (resolver.openOutputStream(fileUri, "wt")
                ?: throw IOException("Unable to open media export destination")).use { output ->
                output.write(content)
            }
        } catch (failure: Throwable) {
            if (existingFileUri == null) {
                try {
                    if (resolver.delete(fileUri, null, null) == 0) {
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
     * Resolves a filesystem folder from the first available MediaStore file for legacy folder UI.
     * Returns empty information when no usable folder is available or the provider rejects access.
     *
     * @return Folder URI and child count, or empty information when the folder cannot be resolved.
     * @author Dmitriy Moroz
     */
    override fun getInfo(): MediaStoreInfo {
        try {
            val extVolumeUri: Uri = MediaStore.Files.getContentUri("external")
            val cursor = context.contentResolver.query(
                extVolumeUri,
                arrayOf(MediaStore.MediaColumns.DATA),
                null,
                null,
                null,
            )
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                val path = cursor.getString(0)
                val file = File(path)
                cursor.close()
                return file.parentFile
                    ?.takeIf(File::exists)
                    ?.takeIf(File::isDirectory)
                    ?.let { dir ->
                        val uri = context.uriFromFile(dir, fileProviderDescriptor.providerPath)
                        MediaStoreInfo(dir.listFiles()?.size ?: 0, uri.toString())
                    }
                    ?: MediaStoreInfo()

            }
            return MediaStoreInfo()
        } catch (e: Exception) {
            return MediaStoreInfo()
        }
    }
}
