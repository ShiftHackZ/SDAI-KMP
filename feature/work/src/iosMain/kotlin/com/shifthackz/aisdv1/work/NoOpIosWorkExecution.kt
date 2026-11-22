package com.shifthackz.aisdv1.work

/**
 * Allows CPU/network work without a system grant in hosts without native support and rejects GPU work.
 *
 * @author Dmitriy Moroz
 */
internal class NoOpIosWorkExecution : IosWorkExecution {
    /**
     * Releases CPU/network work immediately; GPU work cannot start without a native grant.
     *
     * @param ownerId Work identity retained by the caller.
     * @param title Localized heading unused without a system presentation.
     * @param subtitle Localized stage unused without a system presentation.
     * @param requiresGpu Whether native background GPU admission is required.
     * @param onExpiration Unused because no execution grant is acquired.
     * @param onReady Starts the caller's CPU/network operation.
     * @param onUnavailable Rejects GPU work because this host cannot acquire its execution grant.
     * @author Dmitriy Moroz
     */
    override fun begin(
        ownerId: String,
        title: String,
        subtitle: String,
        requiresGpu: Boolean,
        onExpiration: () -> Unit,
        onReady: () -> Unit,
        onUnavailable: () -> Unit
    ) = if (requiresGpu) onUnavailable() else onReady()

    /**
     * Ignores native presentation updates in hosts without execution support.
     *
     * @param ownerId Work identity.
     * @param title Localized heading.
     * @param subtitle Localized stage.
     * @param percentage Measured completion when available.
     * @author Dmitriy Moroz
     */
    override fun update(ownerId: String, title: String, subtitle: String, percentage: Int?) = Unit

    /**
     * Requires no cleanup because this implementation acquires no execution grant.
     *
     * @param ownerId Completed work identity.
     * @param isSuccess Whether the caller persisted or installed its results.
     * @param title Localized final heading unused without a system presentation.
     * @param subtitle Localized final detail unused without a system presentation.
     * @author Dmitriy Moroz
     */
    override fun end(ownerId: String, isSuccess: Boolean, title: String, subtitle: String) = Unit
}
