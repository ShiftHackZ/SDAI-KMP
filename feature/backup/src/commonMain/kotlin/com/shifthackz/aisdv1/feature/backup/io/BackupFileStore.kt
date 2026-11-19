package com.shifthackz.aisdv1.feature.backup.io

import okio.Path
import okio.BufferedSource
import okio.BufferedSink

/**
 * Private durable archive staging and native document transport. User-selected sources are never deleted.
 * @author Dmitriy Moroz
 */
internal interface BackupFileStore {
    val root: Path

    /**
     * Creates an escaped native file URL for a private completed archive.
     * @param path App-owned private archive path.
     * @author Dmitriy Moroz
     */
    fun documentReference(path: Path): String

    /**
     * Resolves a private native file URL without treating its encoded path as display text.
     * @param reference Native file reference; non-file schemes are rejected.
     * @author Dmitriy Moroz
     */
    fun documentPath(reference: String): Path

    /**
     * Reads only the display filename of a selected source, never its full storage path.
     * @param document Native user-selected source reference.
     * @author Dmitriy Moroz
     */
    fun documentName(document: String): String

    /**
     * Describes the selected source for display without inventing a filesystem path for cloud providers.
     * @param document Read-granted native source reference selected by the user.
     * @return Available native path or a document-provider reference with query/fragment removed.
     * @author Dmitriy Moroz
     */
    fun documentLocation(document: String): String

    /**
     * Returns a fresh UUID for app-owned staging, sessions or imported image revisions.
     * @author Dmitriy Moroz
     */
    fun newId(): String

    /**
     * Opens a read-granted native document stream; caller closes it after private staging.
     * @param document Native URI or sandbox-copy URL selected by the user.
     * @author Dmitriy Moroz
     */
    fun openDocument(document: String): BufferedSource

    /**
     * Opens the user-selected destination for complete export; failures propagate.
     * @param document Native write-granted document reference.
     * @author Dmitriy Moroz
     */
    fun writeDocument(document: String): BufferedSink

    /**
     * Returns current private-filesystem free bytes for bounded staging admission.
     * @author Dmitriy Moroz
     */
    fun availableBytes(): Long
}
