package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifthackz.aisdv1.storage.db.persistent.contract.CivitaiUploadContract

/**
 * Stores one publication receipt independently of gallery deletion or account sign-out.
 *
 * @param id Stable local receipt identity used by the paginated history viewport.
 * @param imageId Originating gallery identity.
 * @param accountId Civitai account owning the attempted publication.
 * @param url Confirmed public post URL, absent before success.
 * @param createdAt Publication time in epoch milliseconds.
 * @param imageCreatedAt Original gallery creation time, protecting against reused IDs.
 * @param imageRevision Original encoded-image content identity.
 * @param status Persisted domain upload-status name.
 * @author Dmitriy Moroz
 */
@Entity(
    tableName = CivitaiUploadContract.TABLE,
    indices = [
        Index(
            value = [CivitaiUploadContract.ACCOUNT_ID, CivitaiUploadContract.IMAGE_ID, CivitaiUploadContract.IMAGE_CREATED_AT, CivitaiUploadContract.IMAGE_REVISION],
            unique = true
        ),
        Index(value = [CivitaiUploadContract.IMAGE_ID, CivitaiUploadContract.IMAGE_CREATED_AT, CivitaiUploadContract.IMAGE_REVISION]),
        Index(value = [CivitaiUploadContract.CREATED_AT, CivitaiUploadContract.ID]),
    ],
)
data class CivitaiUploadEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = CivitaiUploadContract.ID)
    val id: Long = 0,
    @ColumnInfo(name = CivitaiUploadContract.IMAGE_ID)
    val imageId: Long,
    @ColumnInfo(name = CivitaiUploadContract.ACCOUNT_ID)
    val accountId: Long,
    @ColumnInfo(name = CivitaiUploadContract.URL)
    val url: String?,
    @ColumnInfo(name = CivitaiUploadContract.CREATED_AT)
    val createdAt: Long,
    @ColumnInfo(name = CivitaiUploadContract.IMAGE_CREATED_AT)
    val imageCreatedAt: Long,
    @ColumnInfo(name = CivitaiUploadContract.IMAGE_REVISION)
    val imageRevision: String,
    @ColumnInfo(name = CivitaiUploadContract.STATUS)
    val status: String,
)
