package com.shifthackz.aisdv1.storage.db.persistent.contract

/**
 * Stable table and column names for durable public Civitai upload receipts.
 *
 * @author Dmitriy Moroz
 */
internal object CivitaiUploadContract {
    /**
     * Public publication receipts retained independently of gallery deletion and sign-out.
     *
     * @author Dmitriy Moroz
     */
    const val TABLE = "civitai_uploads"

    /**
     * Stable local receipt identity used by paginated history.
     *
     * @author Dmitriy Moroz
     */
    const val ID = "id"

    /**
     * Originating gallery identity.
     *
     * @author Dmitriy Moroz
     */
    const val IMAGE_ID = "image_id"

    /**
     * Account that owns the publication attempt.
     *
     * @author Dmitriy Moroz
     */
    const val ACCOUNT_ID = "account_id"

    /**
     * Confirmed public post URL, absent until publication succeeds.
     *
     * @author Dmitriy Moroz
     */
    const val URL = "url"

    /**
     * Publication attempt time in epoch milliseconds.
     *
     * @author Dmitriy Moroz
     */
    const val CREATED_AT = "created_at"

    /**
     * Original gallery creation time guarding against reused image IDs.
     *
     * @author Dmitriy Moroz
     */
    const val IMAGE_CREATED_AT = "image_created_at"

    /**
     * Original content identity guarding against replaced images.
     *
     * @author Dmitriy Moroz
     */
    const val IMAGE_REVISION = "image_revision"

    /**
     * Persisted publication phase or terminal outcome.
     *
     * @author Dmitriy Moroz
     */
    const val STATUS = "status"
}
