package com.shifthackz.aisdv1.feature.backup.codec

import kotlinx.serialization.Serializable

/**
 * Versioned archive metadata independent of Room entities and application versions.
 * @param archiveId Unique exported snapshot identity.
 * @param createdAt UTC epoch milliseconds.
 * @param appVersion Display-only source version.
 * @param sourcePlatform Source platform label.
 * @param sections Ordered length-delimited section descriptors.
 * @param schemaVersion Manifest schema version.
 * @author Dmitriy Moroz
 */
@Serializable
internal data class BackupManifest(
    val archiveId: String,
    val createdAt: Long,
    val appVersion: String,
    val sourcePlatform: String,
    val sections: List<BackupSection>,
    val schemaVersion: Int = 1,
)
