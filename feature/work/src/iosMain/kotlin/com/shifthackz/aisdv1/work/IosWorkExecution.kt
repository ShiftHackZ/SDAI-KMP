package com.shifthackz.aisdv1.work

/**
 * Owns native execution grants and system progress for captured iOS background work.
 * Callbacks may arrive on main; every operation is scoped to the immutable work UUID.
 *
 * @author Dmitriy Moroz
 */
interface IosWorkExecution {
    /**
     * Requests execution before work starts. CPU/network work may use a finite fallback;
     * GPU work starts only after the system grants background GPU execution.
     *
     * @param ownerId Immutable work UUID shared by progress, cancellation and cleanup.
     * @param title Localized operation heading.
     * @param subtitle Localized initial stage.
     * @param requiresGpu Whether the provider may submit GPU work.
     * @param onExpiration Cancels only this owner's operation and its cleanup.
     * @param onReady Releases the operation after native admission.
     * @param onUnavailable Rejects GPU work when the host cannot grant background GPU execution.
     * @author Dmitriy Moroz
     */
    fun begin(
        ownerId: String,
        title: String,
        subtitle: String,
        requiresGpu: Boolean,
        onExpiration: () -> Unit,
        onReady: () -> Unit,
        onUnavailable: () -> Unit
    )

    /**
     * Updates the owner's system presentation without synthesizing progress.
     *
     * @param ownerId Immutable work UUID.
     * @param title Localized operation heading.
     * @param subtitle Localized current stage.
     * @param percentage Measured completion, or null for indeterminate work.
     * @author Dmitriy Moroz
     */
    fun update(ownerId: String, title: String, subtitle: String, percentage: Int?)

    /**
     * Releases this owner's grant, including pending admission, exactly once.
     *
     * @param ownerId Immutable work UUID.
     * @param isSuccess Whether the operation completed and its results were persisted or installed.
     * @param title Localized final outcome heading.
     * @param subtitle Localized final outcome detail.
     * @author Dmitriy Moroz
     */
    fun end(ownerId: String, isSuccess: Boolean, title: String, subtitle: String)
}
