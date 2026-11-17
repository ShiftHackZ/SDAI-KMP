package com.shifthackz.aisdv1.data.local

import java.io.File

/**
 * Owns MNN archive activation and leases independently of the native process.
 *
 * @author Dmitriy Moroz
 */
internal interface MnnModelFileStore {
    /**
     * Returns only an app-owned catalog model directory.
     *
     * @param id Safe MNN catalog identifier.
     * @param activationId Exact manifest identity for a custom snapshot; empty reads the selected identity.
     *
     * @author Dmitriy Moroz
     */
    fun directory(id: String, activationId: String = ""): File

    /**
     * Copies a validated custom bundle into a content-addressed root and returns its manifest SHA256.
     *
     * @param sourcePath External root selected through the standard custom-model picker.
     *
     * @author Dmitriy Moroz
     */
    suspend fun importCustom(sourcePath: String): String

    /**
     * Checks the completion marker; admission separately performs full integrity validation.
     *
     * @param id Catalog identity.
     *
     * @author Dmitriy Moroz
     */
    fun isDownloaded(id: String): Boolean

    /**
     * Keeps host ownership through validation and transport; child process locks independently exclude payload mutation.
     *
     * @param id Catalog identity; deleting/removed models are rejected.
     * @param activationId Captured immutable custom manifest identity, preventing queued model substitution.
     *
     * @author Dmitriy Moroz
     */
    fun acquire(id: String, activationId: String = ""): AutoCloseable

    /**
     * Prevents deletion from marking a bundle unavailable while selection validates and commits it.
     *
     * @param id Catalog or custom UI identity.
     * @param activationId Captured custom manifest identity; empty resolves the committed snapshot.
     *
     * @author Dmitriy Moroz
     */
    fun acquireSelection(id: String, activationId: String = ""): AutoCloseable

    /**
     * Removes idle payloads; active host or child readers leave deletion pending for lease cleanup or restart reconciliation.
     *
     * @param id Catalog identity.
     *
     * @author Dmitriy Moroz
     */
    fun delete(id: String)

    /**
     * Verifies and atomically publishes a safe root-layout ZIP from its private staging file.
     *
     * @param id Catalog identity.
     * @param archive Completed downloaded ZIP.
     *
     * @author Dmitriy Moroz
     */
    suspend fun activate(id: String, archive: File)
}
