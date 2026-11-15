package com.shifthackz.aisdv1.app.diagnostics

import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.GenerationFailureContext

/**
 * Durable token-free attempt history. Operations run on IO, never on Binder/main handlers.
 *
 * @author Dmitriy Moroz
 */
internal interface GenerationAttemptJournal {
    /**
     * Rejects a previously seen owner key while its entry remains in the bounded history.
     *
     * @param job New opaque attempt UUID.
     * @param engine Native engine identifier.
     * @param owner Stable worker-and-batch identity, or null for foreground attempts.
     * @author Dmitriy Moroz
     */
    fun begin(job: String, engine: NativeGenerationEngine, owner: String?)

    /**
     * Persists the validated connection before native entry.
     *
     * @param job Attempt UUID.
     * @param pid Child PID.
     * @param incarnation Random Binder instance token.
     * @author Dmitriy Moroz
     */
    fun connected(job: String, pid: Int, incarnation: String)

    /**
     * Updates only an unfinished entry; late writes cannot replace its terminal outcome.
     *
     * @param job Attempt UUID.
     * @param stage Sanitized native lifecycle stage.
     * @author Dmitriy Moroz
     */
    fun stage(job: String, stage: NativeGenerationStage)

    /**
     * Stores the terminal outcome and optional sanitized exit evidence for the matching retained attempt.
     *
     * @param job Owned UUID.
     * @param outcome Sanitized terminal category.
     * @param stage Last correlated child execution stage.
     * @param exit Best-effort correlated Android exit metadata, without trace contents.
     * @author Dmitriy Moroz
     */
    fun finish(
        job: String,
        outcome: String,
        stage: NativeGenerationStage,
        exit: GenerationFailureContext? = null
    )

    /**
     * Marks unfinished host attempts interrupted at startup without replaying them.
     *
     * @author Dmitriy Moroz
     */
    fun reconcile()
}
