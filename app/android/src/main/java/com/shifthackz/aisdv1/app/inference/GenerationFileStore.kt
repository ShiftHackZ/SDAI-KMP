package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest

/**
 * IO-only atomic exchange of immutable requests and completed PNG output for one opaque job.
 *
 * @author Dmitriy Moroz
 */
internal interface GenerationFileStore {
    /**
     * Atomically commits a versioned request JSON file within the 4 MiB request limit.
     *
     * @param job UUID owned by this attempt.
     * @param request Immutable runtime inputs; never copied into diagnostics.
     * @author Dmitriy Moroz
     */
    fun writeRequest(job: String, request: NativeGenerationRequest)

    /**
     * Reads and validates the captured request, including model readability and parameter limits.
     * Missing, malformed or unsupported request data fails instead of supplying defaults.
     *
     * @param job UUID sent in a validated control message.
     * @author Dmitriy Moroz
     */
    fun readRequest(job: String): NativeGenerationRequest

    /**
     * Validates the complete PNG against its captured request before an atomic bounded write.
     *
     * @param job UUID owned by the producer.
     * @param image Complete PNG bytes.
     * @author Dmitriy Moroz
     */
    fun writeResult(job: String, image: ByteArray)

    /**
     * Returns the completed PNG only after bounded reading and validation against the captured request.
     *
     * @param job UUID owned by the awaiting host.
     * @author Dmitriy Moroz
     */
    fun readResult(job: String): ByteArray

    /**
     * Removes only the UUID job directory and its exchange files; model and gallery files are unaffected.
     *
     * @param job Completed or abandoned UUID; never references model/gallery directories.
     * @author Dmitriy Moroz
     */
    fun delete(job: String)

    /**
     * Removes stale job exchange files before a new host-owned connection starts.
     *
     * @author Dmitriy Moroz
     */
    fun clearAbandoned()
}
