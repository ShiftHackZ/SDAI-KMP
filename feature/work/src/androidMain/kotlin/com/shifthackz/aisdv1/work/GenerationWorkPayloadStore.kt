package com.shifthackz.aisdv1.work

/**
 * IO-only work payload persistence; immutable UUID files prevent replacement from changing inputs.
 *
 * @author Dmitriy Moroz
 */
interface GenerationWorkPayloadStore {
    /**
     * Atomically writes the per-work snapshot, then updates the separate explicit-retry slot.
     * Each serialized payload must fit the 64 MiB limit; these two writes are not a shared transaction.
     *
     * @param id Work UUID.
     * @param latestKey Existing retry slot identifying txt2img or img2img.
     * @param bytes Serialized normal application payload.
     * @author Dmitriy Moroz
     */
    fun write(id: String, latestKey: String, bytes: ByteArray)

    /**
     * Reads the required UUID snapshot for new work, or the legacy retry slot for pre-upgrade work.
     * Missing, empty or oversized content fails; a missing new snapshot never falls back to the retry slot.
     *
     * @param id Work UUID.
     * @param legacyKey Retry slot used only for pre-upgrade queued jobs without immutable files.
     * @param hasImmutablePayload Whether this work request requires its own UUID file.
     * @author Dmitriy Moroz
     */
    fun read(id: String, legacyKey: String, hasImmutablePayload: Boolean): ByteArray

    /**
     * Reads the bounded payload retained for an explicit user retry; missing, empty or oversized files fail.
     * The caller validates the serialized payload contents.
     *
     * @param key Existing explicit retry slot.
     * @author Dmitriy Moroz
     */
    fun latest(key: String): ByteArray

    /**
     * Removes abandoned snapshots without deleting queued or running requests.
     *
     * @param activeIds UUIDs still owned by unfinished WorkManager requests.
     * @author Dmitriy Moroz
     */
    fun prune(activeIds: Set<String>)

    /**
     * Deletes only the terminal worker's UUID snapshot, preserving the latest explicit-retry slot.
     *
     * @param id Terminal work UUID.
     * @author Dmitriy Moroz
     */
    fun delete(id: String)
}
