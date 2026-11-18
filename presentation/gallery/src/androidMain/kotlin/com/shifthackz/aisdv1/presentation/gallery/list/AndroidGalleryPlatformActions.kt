package com.shifthackz.aisdv1.presentation.gallery.list

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.sharing.shareFile
import java.io.File

/**
 * Opens Android media destinations and shares completed ZIP exports through the app file provider.
 *
 * @param context Starts native document-folder and file-share activities.
 * @param fileProviderDescriptor Supplies the manifest authority granting recipient access to exported ZIP files.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidGalleryPlatformActions(
    private val context: Context,
    private val fileProviderDescriptor: FileProviderDescriptor,
) : GalleryPlatformActions {

    /**
     * Opens the destination URI as a document directory with read access.
     *
     * @param folderUri Native content/document URI of the configured export destination.
     *
     * @author Dmitriy Moroz
     */
    override fun openMediaStoreFolder(folderUri: String) {
        with(Intent(Intent.ACTION_VIEW)) {
            setDataAndType(Uri.parse(folderUri), DocumentsContract.Document.MIME_TYPE_DIR)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(this)
        }
    }

    /**
     * Launches Android sharing with the gallery ZIP MIME type.
     *
     * @param filePath Absolute path to the completed app-owned gallery ZIP.
     *
     * @author Dmitriy Moroz
     */
    override fun shareExport(filePath: String) {
        context.shareFile(
            file = File(filePath),
            fileProviderPath = fileProviderDescriptor.providerPath,
            fileMimeType = MIME_TYPE_ZIP,
        )
    }


    private companion object {

        const val MIME_TYPE_ZIP = "application/zip"
    }
}
