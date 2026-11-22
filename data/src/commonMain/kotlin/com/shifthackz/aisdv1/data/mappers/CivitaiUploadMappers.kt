package com.shifthackz.aisdv1.data.mappers

import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity

/**
 * Converts a persisted receipt without changing its original image or account identity.
 *
 * @receiver Complete stored receipt, containing metadata only.
 * @author Dmitriy Moroz
 */
internal fun CivitaiUploadEntity.toDomain(): CivitaiUpload = CivitaiUpload(
    imageId = imageId,
    accountId = accountId,
    url = url,
    createdAt = createdAt,
    imageCreatedAt = imageCreatedAt,
    imageRevision = imageRevision,
    status = CivitaiUploadStatus.valueOf(status),
    id = id,
)

/**
 * Converts the complete publication phase for atomic local persistence.
 *
 * @receiver Attempted or confirmed publication metadata.
 * @param id Existing receipt identity, or zero when inserting a new publication.
 * @author Dmitriy Moroz
 */
internal fun CivitaiUpload.toEntity(id: Long): CivitaiUploadEntity = CivitaiUploadEntity(
    id = id,
    imageId = imageId,
    accountId = accountId,
    url = url,
    createdAt = createdAt,
    imageCreatedAt = imageCreatedAt,
    imageRevision = imageRevision,
    status = status.name,
)
