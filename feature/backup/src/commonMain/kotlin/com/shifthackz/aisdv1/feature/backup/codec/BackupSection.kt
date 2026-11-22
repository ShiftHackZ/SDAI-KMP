package com.shifthackz.aisdv1.feature.backup.codec

import kotlinx.serialization.Serializable

/**
 * Length-delimited manifest descriptor; digest covers the exact stored payload bytes.
 *
 * @param id Stable section key.
 * @param schemaVersion Writer section schema.
 * @param requiredReaderVersion Minimum compatible section reader.
 * @param codec Payload encoding.
 * @param storedBytes Exact payload byte length.
 * @param decodedBytes Decoded byte length; identical for stored payloads.
 * @param sha256 Lowercase SHA-256 integrity digest.
 * @param itemCount Number of represented records.
 * @author Dmitriy Moroz
 */
@Serializable
internal data class BackupSection(
    val id: String,
    val schemaVersion: Int = 1,
    val requiredReaderVersion: Int = 1,
    val codec: String = "stored",
    val storedBytes: Long,
    val decodedBytes: Long,
    val sha256: String,
    val itemCount: Long,
)
