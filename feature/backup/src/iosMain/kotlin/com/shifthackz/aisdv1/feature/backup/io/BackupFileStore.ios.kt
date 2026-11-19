package com.shifthackz.aisdv1.feature.backup.io

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Stages archives in Application Support; imported picker documents are copied by UIKit into the sandbox.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosBackupFileStore : BackupFileStore {
    override val root: Path get() = (NSHomeDirectory() + "/Library/Application Support/backup").toPath()

    override fun documentReference(path: Path): String =
        NSURL.fileURLWithPath(path.toString()).absoluteString!!

    override fun documentPath(reference: String): Path {
        val url = checkNotNull(NSURL.URLWithString(reference))
        require(url.scheme == "file") { "Not a private backup document" }
        return checkNotNull(url.path).toPath()
    }

    override fun documentName(document: String): String =
        NSURL.URLWithString(document)?.lastPathComponent ?: "SDAI.sdaibackup"

    override fun documentLocation(document: String): String =
        documentPath(document).toString()

    override fun newId(): String = NSUUID().UUIDString

    override fun openDocument(document: String) =
        FileSystem.SYSTEM.source(checkNotNull(NSURL.URLWithString(document)?.path).toPath())
            .buffer()

    override fun writeDocument(document: String) =
        FileSystem.SYSTEM.sink(checkNotNull(NSURL.URLWithString(document)?.path).toPath()).buffer()

    override fun availableBytes(): Long =
        checkNotNull(
            NSFileManager.defaultManager.attributesOfFileSystemForPath(
                NSHomeDirectory(),
                null
            )?.get(NSFileSystemFreeSize) as? NSNumber
        ) { "Could not inspect private storage" }.longLongValue
}
