package com.shifthackz.aisdv1.feature.backup.archive

import com.shifthackz.aisdv1.domain.entity.backup.BackupExport
import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection

/**
 * Strict archive operations; restore merge and recovery policy belongs to use-case implementations.
 * @author Dmitriy Moroz
 */
internal interface BackupArchiveStore {
    /**
     * Resolves a retained staged source without persisting platform-specific private paths.
     * @param handle Generated app-owned staging directory.
     * @param filename Suggested export filename retained by the operation.
     * @author Dmitriy Moroz
     */
    fun preparedExport(handle: String, filename: String): BackupExport

    /**
     * Reads current selectable counts and portable fields without requiring protected credentials.
     * @author Dmitriy Moroz
     */
    suspend fun inventory(): BackupInspection

    /**
     * Captures selected portable settings and a paged gallery/statistics snapshot into private binary staging.
     * @param selection Available categories and provider children.
     * @param progress Nonsecret work-stage callback; failure never reports an exported file.
     * @author Dmitriy Moroz
     */
    suspend fun create(selection: BackupSelection, progress: (BackupProgress) -> Unit): BackupExport

    /**
     * Stages a selected native document and validates all known payloads before any app-data writes.
     * @param document Opaque native source reference; original source is never deleted.
     * @param progress Bounded staging and validation progress.
     * @author Dmitriy Moroz
     */
    suspend fun inspect(document: String, progress: (BackupProgress) -> Unit): BackupInspection

    /**
     * Revalidates the binary integrity of an existing private archive for retry or recovery.
     * @param handle Generated private archive identity, never an external path.
     * @author Dmitriy Moroz
     */
    suspend fun staged(handle: String): StagedBackup

    /**
     * Reads supported portable fields while ignoring removed providers/fields and retaining omitted values.
     * @param archive Integrity-validated staged archive.
     * @author Dmitriy Moroz
     */
    fun preferences(archive: StagedBackup): BackupPreferences

    /**
     * Describes readable sections and skipped data without exposing provider credentials.
     * @param archive Validated staged archive and its manifest.
     * @author Dmitriy Moroz
     */
    fun summary(archive: StagedBackup): BackupInspection

    /**
     * Streams complete staged bytes into the selected destination; write failure propagates.
     * @param document App-owned staged export reference.
     * @param destination Native user-selected output reference.
     * @author Dmitriy Moroz
     */
    suspend fun export(document: String, destination: String)

    /**
     * Deletes only generated app-owned staging; the caller must resolve pending journal intent first.
     * @param handle Generated staging identity or its private document reference.
     * @author Dmitriy Moroz
     */
    fun discard(handle: String)
}
