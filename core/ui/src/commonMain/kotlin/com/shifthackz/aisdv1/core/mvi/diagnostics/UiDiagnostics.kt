package com.shifthackz.aisdv1.core.mvi.diagnostics

import kotlinx.coroutines.flow.StateFlow

/**
 * Opt-in non-observable event accumulation, injected through the application composition root.
 * No state values, prompts, credentials or arbitrary labels may enter this boundary.
 *
 * @author Dmitriy Moroz
 */
interface UiDiagnostics {
    /**
     * Whether this build supports capture.
     *
     * @author Dmitriy Moroz
     */
    val isSupported: Boolean

    /**
     * Changes only for explicit capture actions, never for incoming counts.
     *
     * @author Dmitriy Moroz
     */
    val captureState: StateFlow<Boolean>

    /**
     * Allocates a bounded fixed-label screen identity.
     *
     * @param screen Code-owned label.
     * @author Dmitriy Moroz
     */
    fun register(screen: UiDiagnosticScreen): Long

    /**
     * Releases an owner when its ViewModel is cleared.
     *
     * @param owner Previously allocated process-local identity.
     * @author Dmitriy Moroz
     */
    fun unregister(owner: Long)

    /**
     * Records one signal without changing application-observed state.
     *
     * @param owner Previously allocated process-local screen identity.
     * @param type Fixed diagnostic signal.
     * @param subtree Fixed aggregate label, defaulting to the owning screen.
     * @author Dmitriy Moroz
     */
    fun record(
        owner: Long,
        type: UiDiagnosticEventType,
        subtree: UiDiagnosticSubtree = UiDiagnosticSubtree.SCREEN
    )

    /**
     * Begins synchronous work only during explicit capture; inactive calls allocate no span.
     * Never span coroutine suspension or pass application state as metadata.
     *
     * @param owner Registered process-local screen identity.
     * @param subtree Fixed aggregate subtree label.
     * @param operation Fixed synchronous operation.
     * @return Opaque span, or null when capture is inactive or the owner was evicted.
     * @author Dmitriy Moroz
     */
    fun beginTrace(
        owner: Long,
        subtree: UiDiagnosticSubtree,
        operation: UiDiagnosticOperation
    ): UiDiagnosticSpan?

    /**
     * Balances the platform span even after stop/reset, then records a same-capture completion.
     *
     * @param span Token returned by beginTrace; null is an allocation-free inactive path.
     * @author Dmitriy Moroz
     */
    fun finishTrace(span: UiDiagnosticSpan?)

    /**
     * Starts a new capture with cleared counters.
     *
     * @author Dmitriy Moroz
     */
    fun startCapture()

    /**
     * Stops recording without discarding the current snapshot.
     *
     * @author Dmitriy Moroz
     */
    fun stopCapture()

    /**
     * Clears counts and events without changing the capture switch.
     *
     * @author Dmitriy Moroz
     */
    fun reset()

    /**
     * Copies bounded data; callers format/share it off main after explicit user action.
     *
     * @author Dmitriy Moroz
     */
    fun snapshot(): UiDiagnosticsSnapshot
}
