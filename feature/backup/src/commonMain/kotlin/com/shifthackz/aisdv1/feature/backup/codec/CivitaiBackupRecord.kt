package com.shifthackz.aisdv1.feature.backup.codec

import kotlinx.serialization.Serializable

/**
 * Portable publication receipt containing public history metadata only, with no local receipt ID or session data.
 * Original image identity is retained for history deduplication; restored gallery images receive fresh identities.
 *
 * @param imageId Source gallery identity, never interpreted as a destination gallery reference.
 * @param accountId Public Civitai account identity associated with the receipt.
 * @param url Confirmed public post URL or null for an unsuccessful or ambiguous attempt.
 * @param createdAt Attempt time in epoch milliseconds.
 * @param imageCreatedAt Original image time guarding source identity reuse.
 * @param imageRevision Source image revision guarding content replacement.
 * @param status Stable publication status name; future values are skipped by older readers.
 * @author Dmitriy Moroz
 */
@Serializable
internal data class CivitaiBackupRecord(
    val imageId: Long,
    val accountId: Long,
    val url: String?,
    val createdAt: Long,
    val imageCreatedAt: Long,
    val imageRevision: String,
    val status: String,
)
