package com.shifthackz.aisdv1.domain.entity.backup


/**
 * Validated archive summary; handle refers only to an app-owned staged copy.
 * @param handle Opaque private archive identity; never a user-managed file path.
 * @param createdAt Creation timestamp in UTC epoch milliseconds.
 * @param appVersion Originating application version for display only.
 * @param sourcePlatform Originating platform label.
 * @param entries Compatible and unavailable known categories.
 * @param providers Supported provider children present in the archive.
 * @param skippedCount Number of unsupported section, provider or field entries.
 * @param filename User-facing original filename, excluding its storage path.
 * @author Dmitriy Moroz
 */
data class BackupInspection(
    val handle: String,
    val createdAt: Long,
    val appVersion: String,
    val sourcePlatform: String,
    val entries: List<BackupEntry>,
    val providers: Set<String>,
    val skippedCount: Int = 0,
    val filename: String = "",
)
