package com.shifthackz.aisdv1.feature.backup.io

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.util.UUID
import java.io.File
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import okio.sink

/**
 * Stages archives in app-private durable files and streams Storage Access Framework documents.
 * @param context Application context used only for private files and document resolver access.
 * @author Dmitriy Moroz
 */
internal class AndroidBackupFileStore(private val context: Context) : BackupFileStore {
    override val root: Path get() = context.filesDir.resolve("backup").absolutePath.toPath()
    override fun documentReference(path: Path): String =
        Uri.fromFile(File(path.toString())).toString()

    override fun documentPath(reference: String): Path {
        val uri = Uri.parse(reference)
        require(uri.scheme == "file") { "Not a private backup document" }
        return checkNotNull(uri.path).toPath()
    }

    override fun documentName(document: String): String {
        val uri = Uri.parse(document)
        return context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: "SDAI.sdaibackup"
    }

    override fun documentLocation(document: String): String {
        val uri = Uri.parse(document)
        if (uri.scheme == "file") return uri.path.orEmpty()
        if (uri.authority == "com.android.externalstorage.documents" && DocumentsContract.isDocumentUri(
                context,
                uri
            )
        ) {
            val id = DocumentsContract.getDocumentId(uri)
            if (id.startsWith("primary:")) {
                return Environment.getExternalStorageDirectory().absolutePath + "/" + id.substringAfter(
                    ':'
                )
            }
        }
        return Uri.decode(uri.buildUpon().clearQuery().fragment(null).build().toString())
    }

    override fun newId(): String = UUID.randomUUID().toString()
    override fun openDocument(document: String) =
        checkNotNull(context.contentResolver.openInputStream(Uri.parse(document))).source().buffer()

    override fun writeDocument(document: String) =
        checkNotNull(context.contentResolver.openOutputStream(Uri.parse(document), "wt")).sink()
            .buffer()

    override fun availableBytes(): Long = StatFs(context.filesDir.absolutePath).availableBytes
}
