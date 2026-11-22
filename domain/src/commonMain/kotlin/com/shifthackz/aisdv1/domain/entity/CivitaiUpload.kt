package com.shifthackz.aisdv1.domain.entity

/**
 * Durable receipt for a publication initiated by SDAI, retained when a local image is deleted.
 *
 * @param imageId Local gallery identity.
 * @param accountId Civitai account that owns the publication.
 * @param url Public post URL returned after successful creation.
 * @param createdAt Publication timestamp in epoch milliseconds.
 * @param imageCreatedAt Original gallery creation timestamp, protecting against reused local IDs.
 * @param imageRevision Original image content identity, protecting against replacement.
 * @param status Persisted upload phase or terminal outcome.
 * @param id Stable local receipt identity, zero only before initial persistence or legacy conversion.
 * @author Dmitriy Moroz
 */
data class CivitaiUpload(
    val imageId: Long,
    val accountId: Long,
    val url: String?,
    val createdAt: Long,
    val imageCreatedAt: Long,
    val imageRevision: String,
    val status: CivitaiUploadStatus,
    val id: Long = 0,
)
