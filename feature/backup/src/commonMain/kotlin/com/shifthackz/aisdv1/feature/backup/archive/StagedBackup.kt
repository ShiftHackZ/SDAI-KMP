package com.shifthackz.aisdv1.feature.backup.archive

import com.shifthackz.aisdv1.feature.backup.codec.BackupManifest
import okio.Path

/**
 * Verified immutable archive reference owned by private staging.
 *
 * @param handle Private generated directory name.
 * @param path Private binary archive path.
 * @param manifest Verified framing and integrity summary.
 * @author Dmitriy Moroz
 */
internal data class StagedBackup(val handle: String, val path: Path, val manifest: BackupManifest)
